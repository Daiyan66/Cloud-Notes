import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import api from '../api/api';
import { useAuth } from '../context/AuthContext';
import { useBookmarks } from '../context/BookmarksContext';
import {
  BookOpen,
  FileText,
  Trash2,
  HardDrive,
  Upload,
  Bookmark,
  ExternalLink,
  FolderOpen
} from 'lucide-react';

export default function Dashboard() {
  const { user } = useAuth();
  const {
    bookmarkedNotes,
    bookmarkedPapers,
    toggleBookmark,
    clearBookmarks,
    totalBookmarks
  } = useBookmarks();

  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const initialTab = searchParams.get('tab') === 'saved' ? 'saved' : 'uploads';

  const [activeTab, setActiveTab] = useState(initialTab);
  const [stats, setStats] = useState(null);
  const [myNotes, setMyNotes] = useState([]);
  const [myPapers, setMyPapers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const tabParam = new URLSearchParams(location.search).get('tab');
    if (tabParam === 'saved') {
      setActiveTab('saved');
    }
  }, [location.search]);

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
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
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
          <div className="card">
            <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.25rem' }}>Saved Bookmarks</div>
            <div style={{ fontSize: '1.75rem', fontWeight: 700, color: '#f59e0b' }}>
              {totalBookmarks}
            </div>
          </div>
        </div>
      )}

      {/* Navigation Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border)', paddingBottom: '0.75rem' }}>
        <button
          onClick={() => setActiveTab('uploads')}
          className={`btn ${activeTab === 'uploads' ? 'btn-primary' : 'btn-outline'}`}
          style={{ padding: '0.5rem 1rem' }}
        >
          <FolderOpen size={16} /> My Uploads
        </button>
        <button
          onClick={() => setActiveTab('saved')}
          className={`btn ${activeTab === 'saved' ? 'btn-primary' : 'btn-outline'}`}
          style={{ padding: '0.5rem 1rem' }}
        >
          <Bookmark size={16} /> Saved Bookmarks ({totalBookmarks})
        </button>
      </div>

      {activeTab === 'uploads' ? (
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
                  <div key={note.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', border: '1px solid var(--border)', borderRadius: '6px', backgroundColor: 'var(--surface-alt)' }}>
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
                  <div key={paper.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', border: '1px solid var(--border)', borderRadius: '6px', backgroundColor: 'var(--surface-alt)' }}>
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
      ) : (
        /* Saved Bookmarks Tab */
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '2rem' }}>
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Bookmark size={20} color="#f59e0b" /> Saved Course Notes ({bookmarkedNotes.length})
              </h2>
              {bookmarkedNotes.length > 0 && (
                <button
                  onClick={() => clearBookmarks('notes')}
                  className="btn btn-outline"
                  style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                >
                  Clear Notes
                </button>
              )}
            </div>

            {bookmarkedNotes.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2rem 1rem', color: 'var(--text-muted)' }}>
                <p>No notes saved yet.</p>
                <Link to="/notes" className="btn btn-outline" style={{ marginTop: '0.75rem', display: 'inline-flex' }}>
                  Explore Notes to Save
                </Link>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {bookmarkedNotes.map((note) => (
                  <div
                    key={note.id || note}
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '0.85rem 1rem',
                      border: '1px solid var(--border)',
                      borderRadius: '8px',
                      backgroundColor: 'var(--surface-alt)'
                    }}
                  >
                    <div>
                      <Link to={`/notes/${note.id || note}`} style={{ fontWeight: 600, color: 'var(--primary)', fontSize: '0.95rem' }}>
                        {note.title || `Saved Note (${note.id || note})`}
                      </Link>
                      {note.subject && (
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                          {note.subject} • {note.branch} • Semester {note.semester}
                        </div>
                      )}
                    </div>

                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <Link
                        to={`/notes/${note.id || note}`}
                        className="btn btn-outline"
                        style={{ padding: '0.35rem 0.65rem', fontSize: '0.8rem' }}
                      >
                        <ExternalLink size={14} /> Open
                      </Link>
                      <button
                        onClick={() => toggleBookmark(note, 'notes')}
                        className="btn btn-danger"
                        style={{ padding: '0.35rem 0.6rem', fontSize: '0.8rem' }}
                        title="Remove bookmark"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <FileText size={20} color="#059669" /> Saved Question Papers ({bookmarkedPapers.length})
              </h2>
              {bookmarkedPapers.length > 0 && (
                <button
                  onClick={() => clearBookmarks('papers')}
                  className="btn btn-outline"
                  style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                >
                  Clear Papers
                </button>
              )}
            </div>

            {bookmarkedPapers.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2rem 1rem', color: 'var(--text-muted)' }}>
                <p>No question papers saved yet.</p>
                <Link to="/papers" className="btn btn-outline" style={{ marginTop: '0.75rem', display: 'inline-flex' }}>
                  Explore Question Papers
                </Link>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {bookmarkedPapers.map((paper) => (
                  <div
                    key={paper.id || paper}
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '0.85rem 1rem',
                      border: '1px solid var(--border)',
                      borderRadius: '8px',
                      backgroundColor: 'var(--surface-alt)'
                    }}
                  >
                    <div>
                      <span style={{ fontWeight: 600, fontSize: '0.95rem' }}>
                        {paper.title || `Saved Paper (${paper.id || paper})`}
                      </span>
                      {paper.subject && (
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                          {paper.subject} • Year {paper.year} • {paper.branch}
                        </div>
                      )}
                    </div>

                    <button
                      onClick={() => toggleBookmark(paper, 'papers')}
                      className="btn btn-danger"
                      style={{ padding: '0.35rem 0.6rem', fontSize: '0.8rem' }}
                      title="Remove bookmark"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
