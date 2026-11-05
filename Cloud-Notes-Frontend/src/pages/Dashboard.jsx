import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';
import { useAuth } from '../context/AuthContext';
import { BookOpen, FileText, Trash2, HardDrive, Upload } from 'lucide-react';

export default function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [myNotes, setMyNotes] = useState([]);
  const [myPapers, setMyPapers] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const [statsRes, notesRes, papersRes] = await Promise.all([
        api.get('/dashboard/stats'),
        api.get('/notes/my-uploads'),
        api.get('/papers/my-uploads')
      ]);

      if (statsRes.data.success) setStats(statsRes.data.data);
      if (notesRes.data.success) setMyNotes(notesRes.data.data.content);
      if (papersRes.data.success) setMyPapers(papersRes.data.data.content);
    } catch (err) {
      console.error('Error fetching dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleDeleteNote = async (id) => {
    if (window.confirm('Are you sure you want to delete this note and its attached files?')) {
      try {
        await api.delete(`/notes/${id}`);
        fetchDashboardData();
      } catch (err) {
        alert('Failed to delete note');
      }
    }
  };

  const handleDeletePaper = async (id) => {
    if (window.confirm('Are you sure you want to delete this question paper?')) {
      try {
        await api.delete(`/papers/${id}`);
        fetchDashboardData();
      } catch (err) {
        alert('Failed to delete question paper');
      }
    }
  };

  return (
    <div className="container" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>Student Dashboard</h1>
          <p style={{ color: 'var(--text-muted)' }}>
            Welcome back, {user?.name} ({user?.branch} • Semester {user?.semester})
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/upload-note" className="btn btn-primary">
            <Upload size={16} /> Upload Notes
          </Link>
          <Link to="/upload-paper" className="btn btn-outline">
            <FileText size={16} /> Upload Paper
          </Link>
        </div>
      </div>

      {stats && (
        <div className="grid" style={{ marginBottom: '2rem', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))' }}>
          <div className="card">
            <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.25rem' }}>Notes Uploaded</div>
            <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--primary)' }}>{stats.totalNotesUploaded}</div>
          </div>
          <div className="card">
            <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.25rem' }}>Exam Papers Shared</div>
            <div style={{ fontSize: '1.75rem', fontWeight: 700, color: '#059669' }}>{stats.totalPapersUploaded}</div>
          </div>
          <div className="card">
            <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.25rem' }}>Storage Used</div>
            <div style={{ fontSize: '1.75rem', fontWeight: 700, color: '#6366f1' }}>
              {(stats.aggregateFileSize / (1024 * 1024)).toFixed(2)} MB
            </div>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '2rem' }}>
        <div className="card">
          <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <BookOpen size={20} color="var(--primary)" /> My Uploaded Notes
          </h2>
          {myNotes.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>You haven't uploaded any notes yet.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {myNotes.map((note) => (
                <div key={note.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', border: '1px solid var(--border)', borderRadius: '6px' }}>
                  <div>
                    <Link to={`/notes/${note.id}`} style={{ fontWeight: 600, color: 'var(--primary)' }}>
                      {note.title}
                    </Link>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                      {note.subject} • {note.branch} • {note.files?.length || 0} file(s)
                    </div>
                  </div>
                  <button onClick={() => handleDeleteNote(note.id)} className="btn btn-danger" style={{ padding: '0.35rem 0.6rem' }}>
                    <Trash2 size={15} /> Delete
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="card">
          <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <FileText size={20} color="#059669" /> My Question Papers
          </h2>
          {myPapers.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>You haven't uploaded any question papers yet.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {myPapers.map((paper) => (
                <div key={paper.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', border: '1px solid var(--border)', borderRadius: '6px' }}>
                  <div>
                    <span style={{ fontWeight: 600 }}>{paper.title}</span>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                      {paper.subject} • Year {paper.year} • {paper.examType}
                    </div>
                  </div>
                  <button onClick={() => handleDeletePaper(paper.id)} className="btn btn-danger" style={{ padding: '0.35rem 0.6rem' }}>
                    <Trash2 size={15} /> Delete
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
