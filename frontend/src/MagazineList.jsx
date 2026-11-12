import { useState, useEffect } from 'react';
import MagazineForm from './MagazineForm.jsx';
import Magazine from './Magazine.jsx';

export default function MagazineList() {
  const [magazines, setMagazines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    fetch('/api/rest/magazines')
      .then(res => {
        if (!res.ok) throw new Error(`Load failed (${res.status})`);
        return res.json();
      })
      .then(data => { if (active) setMagazines(data); })
      .catch(err => { if (active) setError(err.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  async function handleDeleteMagazine(id) {
    if (!window.confirm('Delete this magazine?')) return;
    setError('');
    try {
      const res = await fetch(`/api/rest/magazines/${id}`, { method: 'DELETE' });
      if (!res.ok) throw new Error(`Delete failed (${res.status})`);
      setMagazines(previous => previous.filter(magazine => magazine.id !== id));
    } catch (err) { setError(err.message); }
  }

  async function handleUpdateMagazine(id, data) {
    const res = await fetch(`/api/rest/magazines/${id}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(`Update failed (${res.status})`);
    const saved = await res.json();
    setMagazines(previous => previous.map(magazine => magazine.id === id ? saved : magazine));
  }

  return <section>
    <h1>Magazines</h1>
    <MagazineForm onMagazineAdded={saved => setMagazines(previous => [...previous, saved])} />
    {loading && <p>Loading magazines...</p>}
    {error && <p role="alert">{error}</p>}
    {!loading && !error && magazines.length === 0 && <p>No magazines yet.</p>}
    <div className="magazine-list">
      {magazines.map(magazine => <Magazine key={magazine.id} {...magazine}
        onUpdate={handleUpdateMagazine} onDelete={handleDeleteMagazine} />)}
    </div>
  </section>;
}
