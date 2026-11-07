import Modal from './Modal.jsx';

export default function ConfirmDialog({ title, message, confirmLabel = 'Supprimer', onConfirm, onCancel }) {
  return (
    <Modal title={title} onClose={onCancel}>
      <p>{message}</p>
      <div className="actions end">
        <button className="btn ghost" onClick={onCancel}>Annuler</button>
        <button className="btn danger" onClick={onConfirm}>{confirmLabel}</button>
      </div>
    </Modal>
  );
}
