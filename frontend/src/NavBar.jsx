import { Link, useNavigate } from 'react-router';
import useAuth from './useAuth.js';

export default function NavBar({ cartCount }) {
  const { logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  // Hiding a link tidies the interface; the server still refuses the write either way.
  return <nav>
    <Link to="/">Home</Link>
    <Link to="/inventory">Books</Link>
    {isAdmin && <Link to="/add">Add Book</Link>}
    <Link to="/magazines">Magazines</Link>
    <Link to="/boardgames">Board Games</Link>
    {isAdmin && <Link to="/boardgames/add">Add Board Game</Link>}
    <Link to="/cart">Cart ({cartCount})</Link>
    <button onClick={() => { logout(); navigate('/login', { replace: true }); }}>Logout</button>
  </nav>;
}
