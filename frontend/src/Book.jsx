import { useState } from 'react';

export default function Book({ book, onDelete, onUpdate }) {
  const [isEditing, setIsEditing] = useState(false);
  const [draft, setDraft] = useState(book);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  function beginEdit() {
    setDraft(book);
    setError(''); setIsEditing(true);
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setDraft(previous => ({ ...previous, [name]: value }));
  }

  async function handleSave(event) {
    event.preventDefault(); setError(''); setSaving(true);
    try {
      // the spread keeps fields this row does not edit, such as isbn
      await onUpdate(book.id, { ...draft, price: Number(draft.price), copies: Number(draft.copies) });
      setIsEditing(false);
    } catch (err) { setError(err.message); }
    finally { setSaving(false); }
  }

  if (isEditing) return <form className="book-row editing" onSubmit={handleSave}>
    <label>Title<input name="title" value={draft.title} onChange={handleChange} required /></label>
    <label>Author<input name="author" value={draft.author} onChange={handleChange} required /></label>
    <label>Price<input name="price" type="number" min="0" step="0.01" value={draft.price} onChange={handleChange} required /></label>
    <label>Copies<input name="copies" type="number" min="0" step="1" value={draft.copies} onChange={handleChange} required /></label>
    <button disabled={saving}>Save</button>
    <button type="button" disabled={saving} onClick={() => setIsEditing(false)}>Cancel</button>
    {error && <p role="alert">{error}</p>}
  </form>;

  return <article className="book-row">
    <h3>{book.title}</h3>
    <p>{book.author} | ${Number(book.price).toFixed(2)} | Copies: {book.copies}</p>
    <button onClick={beginEdit}>Edit</button>
    <button onClick={() => onDelete(book.id)}>Delete</button>
  </article>;
}
