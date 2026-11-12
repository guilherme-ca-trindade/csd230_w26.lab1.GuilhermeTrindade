import { useState } from 'react';

export default function Magazine({ id, title, price, copies, orderQty, currentIssue, onUpdate, onDelete }) {
  const [isEditing, setIsEditing] = useState(false);
  const [draft, setDraft] = useState({ title, price, copies, orderQty, currentIssue });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  function beginEdit() {
    setDraft({ title, price, copies, orderQty, currentIssue });
    setError(''); setIsEditing(true);
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setDraft(previous => ({ ...previous, [name]: value }));
  }

  async function handleSave(event) {
    event.preventDefault(); setError(''); setSaving(true);
    try {
      // currentIssue stays the plain local date-time string: LocalDateTime carries no zone
      await onUpdate(id, { ...draft, price: Number(draft.price),
        copies: Number(draft.copies), orderQty: Number(draft.orderQty) });
      setIsEditing(false);
    } catch (err) { setError(err.message); }
    finally { setSaving(false); }
  }

  if (isEditing) return <form className="magazine-row editing" onSubmit={handleSave}>
    <label>Title<input name="title" value={draft.title} onChange={handleChange} required /></label>
    <label>Price<input name="price" type="number" min="0" step="0.01" value={draft.price} onChange={handleChange} required /></label>
    <label>Copies<input name="copies" type="number" min="0" step="1" value={draft.copies} onChange={handleChange} required /></label>
    <label>Order Quantity<input name="orderQty" type="number" min="0" step="1" value={draft.orderQty} onChange={handleChange} required /></label>
    <label>Current Issue<input name="currentIssue" type="datetime-local" step="1" value={draft.currentIssue} onChange={handleChange} required /></label>
    <button disabled={saving}>Save</button>
    <button type="button" disabled={saving} onClick={() => setIsEditing(false)}>Cancel</button>
    {error && <p role="alert">{error}</p>}
  </form>;

  return <article className="magazine-row">
    <h3>{title}</h3>
    <p>Price: ${Number(price).toFixed(2)} | Copies: {copies} | Order Quantity: {orderQty}</p>
    <p>Current Issue: {currentIssue}</p>
    <button onClick={beginEdit}>Edit</button>
    <button onClick={() => onDelete(id)}>Delete</button>
  </article>;
}
