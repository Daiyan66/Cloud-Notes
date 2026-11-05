import React, { useState, useEffect } from 'react';
import api from '../api/api';
import { Shield, Users, BookOpen, FileText, CheckCircle, XCircle } from 'lucide-react';

export default function Admin() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchAdminData = async () => {
    setLoading(true);
    try {
      const [statsRes, usersRes] = await Promise.all([
        api.get('/admin/stats'),
        api.get('/admin/users')
      ]);

      if (statsRes.data.success) setStats(statsRes.data.data);
      if (usersRes.data.success) setUsers(usersRes.data.data.content);
    } catch (err) {
      console.error('Failed to fetch admin statistics', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminData();
  }, []);

  const handleToggleUser = async (userId) => {
    try {
      await api.patch(`/admin/users/${userId}/toggle-status`);
      fetchAdminData();
    } catch (err) {
      alert('Could not toggle user status');
    }
  };

  if (loading) {
    return <div className="container" style={{ padding: '3rem', textAlign: 'center' }}>Loading administration portal...</div>;
  }

  return (
    <div className="container" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '2rem' }}>
        <Shield size={32} color="#dc2626" />
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>Administrator Console</h1>
          <p style={{ color: 'var(--text-muted)' }}>Platform metrics, moderation controls, and student management.</p>
        </div>
      </div>

      {stats && (
        <div className="grid" style={{ marginBottom: '2rem' }}>
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
              <Users size={18} /> Registered Students
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--primary)' }}>{stats.totalUsers}</div>
          </div>

          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
              <BookOpen size={18} /> Published Notes
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: '#059669' }}>{stats.totalNotes}</div>
          </div>

          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
              <FileText size={18} /> Question Papers
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: '#7c3aed' }}>{stats.totalPapers}</div>
          </div>
        </div>
      )}

      <div className="card" style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem' }}>User Roster & Access Controls</h2>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid var(--border)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '0.75rem' }}>Name</th>
                <th style={{ padding: '0.75rem' }}>Email</th>
                <th style={{ padding: '0.75rem' }}>Branch</th>
                <th style={{ padding: '0.75rem' }}>Semester</th>
                <th style={{ padding: '0.75rem' }}>Role</th>
                <th style={{ padding: '0.75rem' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '0.75rem', fontWeight: 500 }}>{u.name}</td>
                  <td style={{ padding: '0.75rem', color: 'var(--text-muted)' }}>{u.email}</td>
                  <td style={{ padding: '0.75rem' }}>{u.branch || '—'}</td>
                  <td style={{ padding: '0.75rem' }}>{u.semester ? `Sem ${u.semester}` : '—'}</td>
                  <td style={{ padding: '0.75rem' }}>
                    <span className="badge" style={{ backgroundColor: u.roles.includes('ROLE_ADMIN') ? '#fef2f2' : '#eff6ff', color: u.roles.includes('ROLE_ADMIN') ? '#dc2626' : '#2563eb' }}>
                      {u.roles.join(', ').replace(/ROLE_/g, '')}
                    </span>
                  </td>
                  <td style={{ padding: '0.75rem' }}>
                    <button
                      onClick={() => handleToggleUser(u.id)}
                      className="btn btn-outline"
                      style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}
                    >
                      Toggle Status
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
