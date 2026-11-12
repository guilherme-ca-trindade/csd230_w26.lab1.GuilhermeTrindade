import { useState } from 'react';

export default function MagazineForm({ onMagazineAdded }) {
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
      const response = await fetch('/api/rest/magazines', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, price: Number(price),
          copies: Number(copies), orderQty: Number(orderQty), currentIssue })
      });
      if (!response.ok) throw new Error(`Save failed (${response.status})`);
      onMagazineAdded(await response.json());
      setTitle(''); setPrice(''); setCopies(1); setOrderQty(1); setCurrentIssue('');
    } catch (err) { setError(err.message); }
    finally { setSaving(false); }
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
