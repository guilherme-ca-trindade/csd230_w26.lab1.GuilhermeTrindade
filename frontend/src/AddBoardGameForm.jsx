import { useState } from 'react';
import useAuth from './useAuth.js';
import api from './api.js';

// Lab 5: the controlled create form, following AddBookForm / MagazineForm.
// Every input is driven by state, the submit handler stops the browser reload, converts
// the string inputs to the JSON types BoardGameEntity declares, and only clears the
// draft once the server has accepted it - a failed save keeps everything typed.
export default function AddBoardGameForm({ onBoardGameAdded }) {
  const { token } = useAuth();
  const isCurrent = () => localStorage.getItem('token') === token;
  const [title, setTitle] = useState('');
  const [designer, setDesigner] = useState('');
  const [price, setPrice] = useState('');
  const [copies, setCopies] = useState(1);
  const [minPlayers, setMinPlayers] = useState(1);
  const [maxPlayers, setMaxPlayers] = useState(4);
  const [playTimeMinutes, setPlayTimeMinutes] = useState(60);
  const [cooperative, setCooperative] = useState(false);
  const [releaseDate, setReleaseDate] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault(); setError(''); setSaving(true);
    try {
      const response = await api.post('/boardgames', {
        title, designer,
        price: Number(price),
        copies: Number(copies),
        minPlayers: Number(minPlayers),
        maxPlayers: Number(maxPlayers),
        playTimeMinutes: Number(playTimeMinutes),
        cooperative,                 // already a boolean from the checkbox
        releaseDate                  // yyyy-MM-dd, exactly what LocalDate parses
      });
      if (!isCurrent()) return;
      onBoardGameAdded(response.data);
      setTitle(''); setDesigner(''); setPrice(''); setCopies(1);
      setMinPlayers(1); setMaxPlayers(4); setPlayTimeMinutes(60);
      setCooperative(false); setReleaseDate('');
    } catch (err) { if (isCurrent()) setError(err.message); }
    finally { if (isCurrent()) setSaving(false); }
  }

  return <form onSubmit={handleSubmit}>
    <h2>Add New Board Game</h2>
    <label>Title<input value={title} onChange={e => setTitle(e.target.value)} required /></label>
    <label>Designer<input value={designer} onChange={e => setDesigner(e.target.value)} required /></label>
    <label>Price<input type="number" min="0" step="0.01" value={price} onChange={e => setPrice(e.target.value)} required /></label>
    <label>Copies<input type="number" min="0" step="1" value={copies} onChange={e => setCopies(e.target.value)} required /></label>
    <label>Min Players<input type="number" min="1" step="1" value={minPlayers} onChange={e => setMinPlayers(e.target.value)} required /></label>
    <label>Max Players<input type="number" min="1" step="1" value={maxPlayers} onChange={e => setMaxPlayers(e.target.value)} required /></label>
    <label>Play Time (min)<input type="number" min="1" step="1" value={playTimeMinutes} onChange={e => setPlayTimeMinutes(e.target.value)} required /></label>
    <label>Release Date<input type="date" value={releaseDate} onChange={e => setReleaseDate(e.target.value)} required /></label>
    <label className="checkbox">Cooperative<input type="checkbox" checked={cooperative} onChange={e => setCooperative(e.target.checked)} /></label>
    <button disabled={saving}>Save Board Game</button>
    {error && <p role="alert">{error}</p>}
  </form>;
}
