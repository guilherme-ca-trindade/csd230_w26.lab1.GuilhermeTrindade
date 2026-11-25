import axios from 'axios';

// Lecture 2.11.2: one shared client. baseURL means api.get('/books') asks for
// /api/rest/books, which the existing Vite proxy forwards to Spring on 8080.
const api = axios.create({ baseURL: '/api/rest' });

// Lecture 2.12: one client, so the token is attached in one place.
// Storage is read when the request runs, not when this module loads.
api.interceptors.request.use(config => {
  const token = globalThis.localStorage?.getItem('token');
  config.authToken = config.url === '/auth/login' ? null : token;
  config.headers.delete('Authorization');
  if (config.authToken) config.headers.set('Authorization', `Bearer ${config.authToken}`);
  return config;
});

// Lecture 2.12.1: a protected 401 means authentication was not accepted, so the stored
// token is dropped and one full-page navigation goes to the login page. A delayed 401
// belonging to an older token must not sign out a newer login, hence the comparison.
let redirecting = false;
api.interceptors.response.use(response => response, error => {
  const requestToken = error.config?.authToken;
  if (error.response?.status === 401 && requestToken &&
      error.config.url !== '/auth/login' &&
      requestToken === globalThis.localStorage?.getItem('token')) {
    localStorage.removeItem('token');
    window.dispatchEvent(new Event('auth:invalid'));
    if (!redirecting && window.location.pathname !== '/login') {
      redirecting = true;
      window.location.replace('/login?reauth=true');
    }
  }
  return Promise.reject(error); // 403 is forbidden, not a logout.
});

export default api;
