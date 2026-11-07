import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <div className="brand">💰 BudgetFlow</div>
        <nav onClick={() => setOpen(false)}>
          <NavLink to="/expenses">💸 Dépenses</NavLink>
          <NavLink to="/categories">📂 Catégories</NavLink>
        </nav>
        <div className="sidebar-foot">
          <div className="user-name">{user.prenom} {user.nom}</div>
          <div className="user-mail">{user.email}</div>
          <button className="btn ghost" onClick={handleLogout}>Déconnexion</button>
        </div>
      </aside>
      <div className="content">
        <header className="topbar">
          <button className="btn ghost menu-btn" onClick={() => setOpen((o) => !o)} aria-label="Menu">☰</button>
          <span className="brand-mobile">BudgetFlow</span>
        </header>
        <main className="page">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
