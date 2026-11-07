import { useCallback, useEffect, useState } from 'react';
import { api } from '../api.js';
import { useToast } from '../context/ToastContext.jsx';
import Modal from '../components/Modal.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';

function CategoryForm({ initial, onSubmit, onCancel }) {
  const [nom, setNom] = useState(initial?.nom ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await onSubmit({ nom, description });
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <form onSubmit={submit}>
      {error && <div className="alert error">{error}</div>}
      <label>Nom<input value={nom} onChange={(e) => setNom(e.target.value)} maxLength={50} required autoFocus /></label>
      <label>Description<input value={description} onChange={(e) => setDescription(e.target.value)} maxLength={255} /></label>
      <div className="actions end">
        <button type="button" className="btn ghost" onClick={onCancel}>Annuler</button>
        <button className="btn primary" disabled={busy}>{busy ? 'Enregistrement…' : 'Enregistrer'}</button>
      </div>
    </form>
  );
}

export default function Categories() {
  const notify = useToast();
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(null); // null | 'new' | catégorie
  const [deleting, setDeleting] = useState(null);

  const load = useCallback(async () => {
    try {
      setCategories(await api.get('/categories'));
    } catch (err) {
      notify(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }, [notify]);

  useEffect(() => {
    load();
  }, [load]);

  const save = async (data) => {
    if (editing === 'new') {
      await api.post('/categories', data);
      notify('Catégorie ajoutée');
    } else {
      await api.put(`/categories/${editing.id}`, data);
      notify('Catégorie modifiée');
    }
    setEditing(null);
    load();
  };

  const remove = async () => {
    try {
      await api.del(`/categories/${deleting.id}`);
      notify('Catégorie supprimée');
      load();
    } catch (err) {
      notify(err.message, 'error');
    }
    setDeleting(null);
  };

  return (
    <>
      <div className="page-head">
        <h1>Catégories</h1>
        <button className="btn primary" onClick={() => setEditing('new')}>+ Ajouter une catégorie</button>
      </div>

      <div className="card">
        {loading ? (
          <p className="muted">Chargement…</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Nom</th><th>Description</th><th /></tr></thead>
              <tbody>
                {categories.map((c) => (
                  <tr key={c.id}>
                    <td>{c.nom} {!c.personnalisee && <span className="badge grey">par défaut</span>}</td>
                    <td>{c.description}</td>
                    <td className="row-actions">
                      {c.personnalisee && (
                        <>
                          <button className="btn small" onClick={() => setEditing(c)}>Modifier</button>
                          <button className="btn small danger" onClick={() => setDeleting(c)}>Supprimer</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editing && (
        <Modal title={editing === 'new' ? 'Ajouter une catégorie' : 'Modifier la catégorie'} onClose={() => setEditing(null)}>
          <CategoryForm initial={editing === 'new' ? null : editing} onSubmit={save} onCancel={() => setEditing(null)} />
        </Modal>
      )}
      {deleting && (
        <ConfirmDialog
          title="Supprimer la catégorie"
          message={`Supprimer la catégorie « ${deleting.nom} » ? Impossible si elle contient encore des dépenses.`}
          onConfirm={remove}
          onCancel={() => setDeleting(null)}
        />
      )}
    </>
  );
}
