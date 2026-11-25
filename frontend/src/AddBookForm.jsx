import { useState } from 'react';
import useAuth from './useAuth.js';
import api from './api.js';

export default function AddBookForm({ onBookAdded }) {
  const { token } = useAuth();
  const isCurrent = () => localStorage.getItem('token') === token;
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [price, setPrice] = useState('');
  const [copies, setCopies] = useState(10);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(''); setSaving(true);
    try {
      // Axios serializes the object and rejects any non-2xx status, so the catch below
      // covers a failed HTTP response as well as a network failure.
      const response = await api.post('/books', {
        title, author, price: Number(price), copies: Number(copies)
      });
      if (!isCurrent()) return;
      const savedBook = response.data;
      onBookAdded(savedBook);
      setTitle(''); setAuthor(''); setPrice(''); setCopies(10);
    } catch (err) { if (isCurrent()) setError(err.message); }
    finally { if (isCurrent()) setSaving(false); }
  }

  return <form onSubmit={handleSubmit}>
    <h2>Add New Book</h2>
    <label>Title<input value={title} onChange={e => setTitle(e.target.value)} required /></label>
    <label>Author<input value={author} onChange={e => setAuthor(e.target.value)} required /></label>
    <label>Price<input type="number" min="0" step="0.01" value={price} onChange={e => setPrice(e.target.value)} required /></label>
    <label>Copies<input type="number" min="0" step="1" value={copies} onChange={e => setCopies(e.target.value)} required /></label>
    <button disabled={saving}>Save Book</button>
    {error && <p role="alert">{error}</p>}
  </form>;
}
