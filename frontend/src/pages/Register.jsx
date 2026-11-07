import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ prenom: '', nom: '', email: '', motDePasse: '', confirmationMotDePasse: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (form.motDePasse.length < 8) return setError('Le mot de passe doit contenir au moins 8 caractères');
    if (form.motDePasse !== form.confirmationMotDePasse) return setError('Les mots de passe ne correspondent pas');
    setBusy(true);
    try {
      await register(form);
      navigate('/expenses');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={submit}>
        <h1>Créer un compte</h1>
        {error && <div className="alert error">{error}</div>}
        <div className="grid2">
          <label>Prénom<input value={form.prenom} onChange={set('prenom')} required /></label>
          <label>Nom<input value={form.nom} onChange={set('nom')} required /></label>
        </div>
        <label>E-mail<input type="email" value={form.email} onChange={set('email')} autoComplete="username" required /></label>
        <label>Mot de passe (8 caractères min.)
          <input type="password" value={form.motDePasse} onChange={set('motDePasse')} autoComplete="new-password" required />
        </label>
        <label>Confirmation
          <input type="password" value={form.confirmationMotDePasse} onChange={set('confirmationMotDePasse')} autoComplete="new-password" required />
        </label>
        <button className="btn primary full" disabled={busy}>{busy ? 'Création…' : 'Créer mon compte'}</button>
        <p className="muted center">Déjà inscrit ? <Link to="/login">Se connecter</Link></p>
      </form>
    </div>
  );
}
