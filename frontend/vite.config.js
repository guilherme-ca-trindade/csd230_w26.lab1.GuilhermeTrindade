import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Lecture 2.11: the browser only ever talks to Vite. Relative /api requests are
    // forwarded to Spring on 8080, so the page stays on one origin and no CORS policy
    // is involved. A proxy routes requests; it does not grant API permission.
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
});
