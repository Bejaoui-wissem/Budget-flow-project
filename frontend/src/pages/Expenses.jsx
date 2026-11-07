import { useCallback, useEffect, useState } from 'react';
import { api } from '../api.js';
import { useToast } from '../context/ToastContext.jsx';
import Modal from '../components/Modal.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';

const PAYMENTS = ['Espèces', 'Carte', 'Virement', 'Autre'];
const today = () => new Date().toISOString().slice(0, 10);
const money = (v) => `${Number(v).toFixed(2)} DT`;
const frDate = (iso) => iso.split('-').reverse().join('/');
const EMPTY_FILTERS = { q: '', categoryId: '', from: '', to: '', sort: 'date:desc' };

function toQuery(f) {
  const p = new URLSearchParams();
  const [sort, order] = f.sort.split(':');
  if (f.q.trim()) p.set('q', f.q.trim());
  if (f.categoryId) p.set('categoryId', f.categoryId);
  if (f.from) p.set('from', f.from);
  if (f.to) p.set('to', f.to);
  p.set('sort', sort);
  p.set('order', order);
  return p.toString();
}

function ExpenseForm({ initial, categories, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    montant: initial?.montant ?? '',
    date: initial?.date ?? today(),
    categoryId: initial?.categoryId ?? categories[0]?.id ?? '',
    modePaiement: initial?.modePaiement ?? 'Espèces',
    description: initial?.description ?? '',
  });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (!(Number(form.montant) > 0)) return setError('Le montant doit être strictement positif');
    setBusy(true);
    try {
      await onSubmit({
        montant: Number(form.montant),
        date: form.date,
        categoryId: Number(form.categoryId),
        modePaiement: form.modePaiement,
        description: form.description,
      });
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <form onSubmit={submit}>
      {error && <div className="alert error">{error}</div>}
      <div className="grid2">
        <label>Montant (DT)
          <input type="number" step="0.01" min="0.01" value={form.montant} onChange={set('montant')} required autoFocus />
        </label>
        <label>Date<input type="date" value={form.date} onChange={set('date')} required /></label>
        <label>Catégorie
          <select value={form.categoryId} onChange={set('categoryId')} required>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
          </select>
        </label>
        <label>Mode de paiement
          <select value={form.modePaiement} onChange={set('modePaiement')}>
            {PAYMENTS.map((p) => <option key={p}>{p}</option>)}
          </select>
        </label>
      </div>
      <label>Description
        <input value={form.description} onChange={set('description')} maxLength={255} />
      </label>
      <div className="actions end">
        <button type="button" className="btn ghost" onClick={onCancel}>Annuler</button>
        <button className="btn primary" disabled={busy}>{busy ? 'Enregistrement…' : 'Enregistrer'}</button>
      </div>
    </form>
  );
}

export default function Expenses() {
  const notify = useToast();
  const [categories, setCategories] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [query, setQuery] = useState(toQuery(EMPTY_FILTERS));
  const [editing, setEditing] = useState(null); // null | 'new' | dépense
  const [deleting, setDeleting] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setExpenses(await api.get(`/expenses?${query}`));
    } catch (err) {
      notify(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }, [query, notify]);

  useEffect(() => {
    api.get('/categories').then(setCategories).catch((err) => notify(err.message, 'error'));
  }, [notify]);

  useEffect(() => {
    load();
  }, [load]);

  const save = async (data) => {
    if (editing === 'new') {
      await api.post('/expenses', data);
      notify('Dépense ajoutée');
    } else {
      await api.put(`/expenses/${editing.id}`, data);
      notify('Dépense modifiée');
    }
    setEditing(null);
    load();
  };

  const remove = async () => {
    try {
      await api.del(`/expenses/${deleting.id}`);
      notify('Dépense supprimée');
      load();
    } catch (err) {
      notify(err.message, 'error');
    }
    setDeleting(null);
  };

  const applyFilters = (e) => {
    e.preventDefault();
    setQuery(toQuery(filters));
  };
  const resetFilters = () => {
    setFilters(EMPTY_FILTERS);
    setQuery(toQuery(EMPTY_FILTERS));
  };
  const setFilter = (field) => (e) => setFilters({ ...filters, [field]: e.target.value });

  const total = expenses.reduce((sum, e) => sum + Number(e.montant), 0);

  return (
    <>
      <div className="page-head">
        <h1>Mes dépenses</h1>
        <button className="btn primary" onClick={() => setEditing('new')} disabled={categories.length === 0}>+ Ajouter une dépense</button>
      </div>

      <form className="card filters" onSubmit={applyFilters}>
        <label>Recherche<input placeholder="Description…" value={filters.q} onChange={setFilter('q')} /></label>
        <label>Catégorie
          <select value={filters.categoryId} onChange={setFilter('categoryId')}>
            <option value="">Toutes</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
          </select>
        </label>
        <label>Du<input type="date" value={filters.from} onChange={setFilter('from')} /></label>
        <label>Au<input type="date" value={filters.to} onChange={setFilter('to')} /></label>
        <label>Tri
          <select value={filters.sort} onChange={setFilter('sort')}>
            <option value="date:desc">Date (récent)</option>
            <option value="date:asc">Date (ancien)</option>
            <option value="montant:desc">Montant (décroissant)</option>
            <option value="montant:asc">Montant (croissant)</option>
          </select>
        </label>
        <div className="actions">
          <button className="btn primary">Filtrer</button>
          <button type="button" className="btn ghost" onClick={resetFilters}>Réinitialiser</button>
        </div>
      </form>

      <div className="card">
        {loading ? (
          <p className="muted">Chargement…</p>
        ) : expenses.length === 0 ? (
          <div className="empty">
            <div className="empty-icon">🧾</div>
            <p>Aucune dépense pour le moment.</p>
          </div>
        ) : (
          <>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Date</th><th>Catégorie</th><th>Description</th><th>Paiement</th><th className="num">Montant</th><th /></tr>
                </thead>
                <tbody>
                  {expenses.map((e) => (
                    <tr key={e.id}>
                      <td>{frDate(e.date)}</td>
                      <td><span className="badge">{e.categoryName}</span></td>
                      <td>{e.description}</td>
                      <td>{e.modePaiement}</td>
                      <td className="num">{money(e.montant)}</td>
                      <td className="row-actions">
                        <button className="btn small" onClick={() => setEditing(e)}>Modifier</button>
                        <button className="btn small danger" onClick={() => setDeleting(e)}>Supprimer</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="total">Total : <strong>{money(total)}</strong> ({expenses.length} dépense{expenses.length > 1 ? 's' : ''})</div>
          </>
        )}
      </div>

      {editing && (
        <Modal title={editing === 'new' ? 'Ajouter une dépense' : 'Modifier la dépense'} onClose={() => setEditing(null)}>
          <ExpenseForm initial={editing === 'new' ? null : editing} categories={categories} onSubmit={save} onCancel={() => setEditing(null)} />
        </Modal>
      )}
      {deleting && (
        <ConfirmDialog
          title="Supprimer la dépense"
          message={`Supprimer « ${deleting.description || deleting.categoryName} » de ${money(deleting.montant)} ? Cette action est définitive.`}
          onConfirm={remove}
          onCancel={() => setDeleting(null)}
        />
      )}
    </>
  );
}
