// Client HTTP du backend Java. En développement, /api est redirigé par Vite vers http://localhost:8080.
// Pour un autre serveur : créer un fichier .env.local contenant VITE_API_URL=http://mon-serveur:8080/api
const BASE = import.meta.env.VITE_API_URL ?? '/api';
const TOKEN_KEY = 'bf_token';

const storage = typeof localStorage !== 'undefined' ? localStorage : null;

export const getToken = () => storage?.getItem(TOKEN_KEY) ?? null;
export const setToken = (token) => {
  if (!storage) return;
  if (token) storage.setItem(TOKEN_KEY, token);
  else storage.removeItem(TOKEN_KEY);
};

let onUnauthorized = () => {};
export const setUnauthorizedHandler = (fn) => {
  onUnauthorized = fn;
};

async function request(method, path, body) {
  const token = getToken();
  let res;
  try {
    res = await fetch(BASE + path, {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new Error('Impossible de joindre le serveur. Vérifiez que le backend est démarré.');
  }
  if (res.status === 204) return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    if (res.status === 401 && token) onUnauthorized(); // session expirée
    if (!data.error && res.status >= 500) throw new Error('Serveur indisponible. Vérifiez que le backend est démarré.');
    throw new Error(data.error || `Erreur ${res.status}`);
  }
  return data;
}

export const api = {
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body),
  put: (path, body) => request('PUT', path, body),
  del: (path) => request('DELETE', path),
};
