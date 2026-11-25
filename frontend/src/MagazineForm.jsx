import { useState } from 'react';
import useAuth from './useAuth.js';
import api from './api.js';

export default function MagazineForm({ onMagazineAdded }) {
  const { token } = useAuth();
  const isCurrent = () => localStorage.getItem('token') === token;
  const [title, setTitle] = useState('');
  const [price, setPrice] = useState('');
  const [copies, setCopies] = useState(1);
  const [orderQty, setOrderQty] = useState(1);
  const [currentIssue, setCurrentIssue] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault(); setError(''); setSaving(true);
    try {
      const response = await api.post('/magazines', {
        title, price: Number(price), copies: Number(copies),
        orderQty: Number(orderQty), currentIssue
      });
      if (!isCurrent()) return;
      onMagazineAdded(response.data);
      setTitle(''); setPrice(''); setCopies(1); setOrderQty(1); setCurrentIssue('');
    } catch (err) { if (isCurrent()) setError(err.message); }
    finally { if (isCurrent()) setSaving(false); }
  }

  return <form onSubmit={handleSubmit}>
    <h2>Add New Magazine</h2>
    <label>Title<input value={title} onChange={e => setTitle(e.target.value)} required /></label>
    <label>Price<input type="number" min="0" step="0.01" value={price} onChange={e => setPrice(e.target.value)} required /></label>
    <label>Copies<input type="number" min="0" step="1" value={copies} onChange={e => setCopies(e.target.value)} required /></label>
    <label>Order Quantity<input type="number" min="0" step="1" value={orderQty} onChange={e => setOrderQty(e.target.value)} required /></label>
    <label>Current Issue<input type="datetime-local" step="1" value={currentIssue} onChange={e => setCurrentIssue(e.target.value)} required /></label>
    <button disabled={saving}>Save Magazine</button>
    {error && <p role="alert">{error}</p>}
  </form>;
}
