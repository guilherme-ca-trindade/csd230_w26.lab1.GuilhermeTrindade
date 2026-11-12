import { useState, useEffect } from 'react';
import { Routes, Route, useNavigate } from 'react-router';
import NavBar from './NavBar.jsx';
import Home from './Home.jsx';
import MagazineList from './MagazineList.jsx';
import AddBookForm from './AddBookForm.jsx';
import BookList from './BookList.jsx';
import AddBoardGameForm from './AddBoardGameForm.jsx';
import BoardGameList from './BoardGameList.jsx';
import './App.css';

export default function App() {
  const navigate = useNavigate();

  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Lab 5: the board game collection lives here for the same reason the book one does -
  // its list and its add form are on two different routes, so their nearest common parent
  // has to own the data. MagazineList still owns its own, because its form and rows share
  // a single route.
  const [boardGames, setBoardGames] = useState([]);
  const [gamesLoading, setGamesLoading] = useState(true);
  const [gamesError, setGamesError] = useState('');

  useEffect(() => {
    let active = true;
    fetch('/api/rest/books')
      .then(res => {
        if (!res.ok) throw new Error(`Load failed (${res.status})`);
        return res.json();
      })
      .then(data => { if (active) setBooks(data); })
      .catch(err => { if (active) setError(err.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;
    fetch('/api/rest/boardgames')
      .then(res => {
        if (!res.ok) throw new Error(`Load failed (${res.status})`);
        return res.json();
      })
      .then(data => { if (active) setBoardGames(data); })
      .catch(err => { if (active) setGamesError(err.message); })
      .finally(() => { if (active) setGamesLoading(false); });
    return () => { active = false; };
  }, []);

  function handleBookAdded(savedBook) {
    setBooks(previous => [...previous, savedBook]);
    navigate('/inventory');
  }

  async function handleDeleteBook(id) {
    if (!window.confirm('Delete this book?')) return;
    setError('');
    try {
      const response = await fetch(`/api/rest/books/${id}`, { method: 'DELETE' });
      if (!response.ok) throw new Error(`Delete failed (${response.status})`);
      setBooks(previous => previous.filter(book => book.id !== id));
    } catch (err) { setError(err.message); }
  }

  async function handleUpdateBook(id, data) {
    const response = await fetch(`/api/rest/books/${id}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error(`Update failed (${response.status})`);
    const savedBook = await response.json();
    setBooks(previous => previous.map(book => book.id === id ? savedBook : book));
  }

  // Lab 5: the form has already done the POST, so this only appends what came back and
  // moves to the list. A new array, not push, so React sees a changed reference.
  function handleBoardGameAdded(savedBoardGame) {
    setBoardGames(previous => [...previous, savedBoardGame]);
    navigate('/boardgames');
  }

  async function handleDeleteBoardGame(id) {
    if (!window.confirm('Delete this board game?')) return;
    setGamesError('');
    try {
      const response = await fetch(`/api/rest/boardgames/${id}`, { method: 'DELETE' });
      if (!response.ok) throw new Error(`Delete failed (${response.status})`);
      // a successful DELETE has no body, so nothing is parsed here
      setBoardGames(previous => previous.filter(boardGame => boardGame.id !== id));
    } catch (err) { setGamesError(err.message); }
  }

  // deliberately not caught here: the row awaits this, so a rejection reaches its editor
  // and keeps the draft on screen instead of pretending the save worked
  async function handleUpdateBoardGame(id, data) {
    const response = await fetch(`/api/rest/boardgames/${id}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error(`Update failed (${response.status})`);
    const savedBoardGame = await response.json();
    setBoardGames(previous => previous.map(boardGame =>
      boardGame.id === id ? savedBoardGame : boardGame));
  }

  return <main className="app-container">
    <h1>My Bookstore Frontend</h1>
    <NavBar />
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/inventory" element={<BookList books={books} loading={loading} error={error}
        onDelete={handleDeleteBook} onUpdate={handleUpdateBook} />} />
      <Route path="/add" element={<AddBookForm onBookAdded={handleBookAdded} />} />
      <Route path="/magazines" element={<MagazineList />} />
      <Route path="/boardgames" element={<BoardGameList boardGames={boardGames}
        loading={gamesLoading} error={gamesError}
        onDelete={handleDeleteBoardGame} onUpdate={handleUpdateBoardGame} />} />
      <Route path="/boardgames/add" element={
        <AddBoardGameForm onBoardGameAdded={handleBoardGameAdded} />} />
      <Route path="*" element={<h2>Page not found</h2>} />
    </Routes>
  </main>;
}
