import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Le serveur de développement redirige /api vers le backend Java (port 8080).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
