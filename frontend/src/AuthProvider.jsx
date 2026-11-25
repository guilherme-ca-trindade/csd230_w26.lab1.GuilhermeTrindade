import { useEffect, useState } from 'react';
import { hasRole } from './tokenRoles.js';
import { AuthContext } from './AuthContext.js';

export default function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));

  // Lecture 2.12.1: the response interceptor fires this when a protected request was
  // refused with 401, so the provider drops its token state too.
  useEffect(() => {
    function invalidSession() { setToken(null); }
    window.addEventListener('auth:invalid', invalidSession);
    return () => window.removeEventListener('auth:invalid', invalidSession);
  }, []);

  function login(nextToken) {
    if (typeof nextToken !== 'string' || !nextToken.trim()) throw new Error('Login did not return a token.');
    localStorage.setItem('token', nextToken);
    setToken(nextToken);
  }

  function logout() {
    localStorage.removeItem('token');
    setToken(null);
  }

  // isAdmin is derived from the current token, so there is no second roles state to
  // keep in step. It decides what to show - the server still decides what is allowed.
  return <AuthContext.Provider value={{ token, login, logout, isAdmin: hasRole(token, 'ROLE_ADMIN') }}>{children}</AuthContext.Provider>;
}
