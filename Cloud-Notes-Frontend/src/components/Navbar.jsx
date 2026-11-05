import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { BookOpen, Upload, LogOut, User as UserIcon, Shield, FileText } from 'lucide-react';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="container nav-container">
        <Link to="/" className="nav-brand">
          <BookOpen size={24} />
          <span>CloudNotes Hub</span>
        </Link>

        <div className="nav-links">
          <Link to="/notes" className="nav-item">Browse Notes</Link>
          <Link to="/papers" className="nav-item">Question Papers</Link>

          {user ? (
            <>
              <Link to="/upload-note" className="btn btn-outline" style={{ padding: '0.4rem 0.8rem' }}>
                <Upload size={16} /> Upload Notes
              </Link>
              <Link to="/upload-paper" className="btn btn-outline" style={{ padding: '0.4rem 0.8rem' }}>
                <FileText size={16} /> Upload Papers
              </Link>
              <Link to="/dashboard" className="nav-item" title="Dashboard">
                <UserIcon size={18} style={{ verticalAlign: 'middle' }} /> {user.name.split(' ')[0]}
              </Link>
              {isAdmin && (
                <Link to="/admin" className="nav-item" title="Admin Console" style={{ color: '#dc2626' }}>
                  <Shield size={18} style={{ verticalAlign: 'middle' }} /> Admin
                </Link>
              )}
              <button onClick={handleLogout} className="btn btn-outline" title="Log out">
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline">Log in</Link>
              <Link to="/register" className="btn btn-primary">Sign up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
