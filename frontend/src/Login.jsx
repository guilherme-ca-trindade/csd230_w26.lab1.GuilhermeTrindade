import { useState } from 'react';
import { Navigate, useNavigate, useSearchParams } from 'react-router';
import api from './api.js';
import useAuth from './useAuth.js';

export default function Login() {
  const { token, login } = useAuth();
  const navigate = useNavigate();
  const [search] = useSearchParams();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [pending, setPending] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault(); setError(''); setPending(true);
    try {
      const response = await api.post('/auth/login', { username, password });
      login(response.data.token); // Storage is synchronized before navigating.
      navigate('/inventory', { replace: true });
    } catch (err) { setError(err.response?.data?.message || err.message); }
    finally { setPending(false); }
  }

  if (token) return <Navigate to="/inventory" replace />;

  return <form onSubmit={handleSubmit}>
    <h2>Sign in</h2>
    {search.get('reauth') === 'true' && <p role="status">Your session has expired or is invalid. Please sign in again.</p>}
    <label>Username<input autoComplete="username" value={username} onChange={e => setUsername(e.target.value)} required /></label>
    <label>Password<input type="password" autoComplete="current-password" value={password} onChange={e => setPassword(e.target.value)} required /></label>
    <button disabled={pending}>Sign in</button>
    {error && <p role="alert">{error}</p>}
  </form>;
}
