import { useState } from 'react';

// Lab 5: one saved board game - a display row that can swap itself for an inline editor.
// It receives the saved record and the two mutation callbacks as props and never writes to
// them: beginEdit copies the props into a local draft, and only an accepted PUT closes the
// editor, so a rejected save keeps whatever the user typed.
export default function BoardGame({ boardGame, onDelete, onUpdate }) {
  const [isEditing, setIsEditing] = useState(false);
  const [draft, setDraft] = useState(boardGame);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  function beginEdit() {
    setDraft(boardGame);
    setError(''); setIsEditing(true);
  }

  function handleChange(event) {
    const { name, value, type, checked } = event.target;
    // a checkbox reports .checked, every other input reports .value
    setDraft(previous => ({ ...previous, [name]: type === 'checkbox' ? checked : value }));
  }

  async function handleSave(event) {
    event.preventDefault(); setError(''); setSaving(true);
    try {
      // the spread carries productId and any other field this row does not edit;
      // releaseDate stays the plain yyyy-MM-dd string a LocalDate expects
      await onUpdate(boardGame.id, {
        ...draft,
        price: Number(draft.price),
        copies: Number(draft.copies),
        minPlayers: Number(draft.minPlayers),
        maxPlayers: Number(draft.maxPlayers),
        playTimeMinutes: Number(draft.playTimeMinutes),
        cooperative: Boolean(draft.cooperative)
      });
      setIsEditing(false);
    } catch (err) { setError(err.message); }
    finally { setSaving(false); }
  }

  if (isEditing) return <form className="boardgame-row editing" onSubmit={handleSave}>
    <label>Title<input name="title" value={draft.title} onChange={handleChange} required /></label>
    <label>Designer<input name="designer" value={draft.designer} onChange={handleChange} required /></label>
    <label>Price<input name="price" type="number" min="0" step="0.01" value={draft.price} onChange={handleChange} required /></label>
    <label>Copies<input name="copies" type="number" min="0" step="1" value={draft.copies} onChange={handleChange} required /></label>
    <label>Min Players<input name="minPlayers" type="number" min="1" step="1" value={draft.minPlayers} onChange={handleChange} required /></label>
    <label>Max Players<input name="maxPlayers" type="number" min="1" step="1" value={draft.maxPlayers} onChange={handleChange} required /></label>
    <label>Play Time (min)<input name="playTimeMinutes" type="number" min="1" step="1" value={draft.playTimeMinutes} onChange={handleChange} required /></label>
    <label>Release Date<input name="releaseDate" type="date" value={draft.releaseDate} onChange={handleChange} required /></label>
    <label className="checkbox">Cooperative<input name="cooperative" type="checkbox" checked={Boolean(draft.cooperative)} onChange={handleChange} /></label>
    <button disabled={saving}>Save</button>
    <button type="button" disabled={saving} onClick={() => setIsEditing(false)}>Cancel</button>
    {error && <p role="alert">{error}</p>}
  </form>;

  return <article className="boardgame-row">
    <h3>{boardGame.title} <span className="muted">#{boardGame.id}</span></h3>
    <p>{boardGame.designer} | ${Number(boardGame.price).toFixed(2)} | Copies: {boardGame.copies}</p>
    <p>
      Players: {boardGame.minPlayers}&ndash;{boardGame.maxPlayers} |
      Play time: {boardGame.playTimeMinutes} min |
      Released: {boardGame.releaseDate}
    </p>
    <p>{boardGame.cooperative ? 'Cooperative' : 'Competitive'}</p>
    <button onClick={beginEdit}>Edit</button>
    <button onClick={() => onDelete(boardGame.id)}>Delete</button>
  </article>;
}
