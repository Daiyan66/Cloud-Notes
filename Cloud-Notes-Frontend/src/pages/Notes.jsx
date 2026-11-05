import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';
import { Search, BookOpen, User, Folder, ArrowRight, Sparkles } from 'lucide-react';

export default function Notes() {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [branch, setBranch] = useState('All');
  const [semester, setSemester] = useState('All');

  const fetchNotes = async () => {
    setLoading(true);
    try {
      const params = {};
      if (query.trim()) params.query = query.trim();
      if (branch !== 'All') params.branch = branch;
      if (semester !== 'All') params.semester = parseInt(semester, 10);

      const res = await api.get('/notes', { params });
      if (res.data.success) {
        setNotes(res.data.data.content);
      }
    } catch (err) {
      console.error('Failed to fetch notes', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotes();
  }, [branch, semester]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchNotes();
  };

  const handleReset = () => {
    setQuery('');
    setBranch('All');
    setSemester('All');
  };

  return (
    <div className="container" style={{ padding: '2.5rem 1.5rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2.1rem', fontWeight: 800, marginBottom: '0.4rem', letterSpacing: '-0.025em' }}>
          Course Notes & Lecture Materials
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>
          Browse verified curriculum notes, lab assignments, and study materials organized by semester and branch.
        </p>
      </div>

      <form onSubmit={handleSearchSubmit} className="filter-bar">
        <div className="filter-item" style={{ flex: 2, minWidth: '240px' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Search notes by subject, topic or title..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
        <div className="filter-item">
          <select className="form-control" value={branch} onChange={(e) => setBranch(e.target.value)}>
            <option value="All">All Branches</option>
            <option value="Computer Science">Computer Science</option>
            <option value="Information Technology">Information Technology</option>
            <option value="Electronics">Electronics</option>
            <option value="Mechanical">Mechanical</option>
            <option value="Civil">Civil</option>
            <option value="Electrical">Electrical</option>
          </select>
        </div>
        <div className="filter-item">
          <select className="form-control" value={semester} onChange={(e) => setSemester(e.target.value)}>
            <option value="All">All Semesters</option>
            {[1,2,3,4,5,6,7,8].map(s => (
              <option key={s} value={s}>Semester {s}</option>
            ))}
          </select>
        </div>
        <div style={{ display: 'flex', gap: '0.6rem' }}>
          <button type="submit" className="btn btn-primary">
            <Search size={16} /> Search
          </button>
          <button type="button" onClick={handleReset} className="btn btn-outline">
            Reset
          </button>
        </div>
      </form>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem 0' }}>
          <div style={{ display: 'inline-block', width: '32px', height: '32px', border: '3px solid var(--border)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
          <p style={{ color: 'var(--text-muted)', marginTop: '1rem', fontWeight: 500 }}>Loading course materials...</p>
        </div>
      ) : notes.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <BookOpen size={52} style={{ color: 'var(--text-muted)', marginBottom: '1rem', opacity: 0.7 }} />
          <h3 style={{ fontSize: '1.25rem' }}>No notes found</h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem', maxWidth: '420px', margin: '0.5rem auto 0' }}>
            Adjust your search terms or filters to discover available curriculum notes.
          </p>
        </div>
      ) : (
        <div className="grid">
          {notes.map((note) => (
            <div key={note.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.85rem' }}>
                <span className="badge">
                  {note.branch}
                </span>
                <span style={{ fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                  Semester {note.semester}
                </span>
              </div>

              <h3 style={{ fontSize: '1.2rem', lineHeight: 1.4, marginBottom: '0.4rem', fontWeight: 700 }}>
                <Link to={`/notes/${note.id}`} style={{ color: 'inherit' }}>
                  {note.title}
                </Link>
              </h3>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.85rem' }}>
                <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--primary)' }}>
                  {note.subject}
                </span>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>•</span>
                <span style={{ fontSize: '0.825rem', color: '#7c3aed', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
                  <Sparkles size={13} /> AI Summaries Ready
                </span>
              </div>

              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', flex: 1, marginBottom: '1.25rem', lineHeight: 1.5 }}>
                {note.description ? (note.description.length > 110 ? note.description.substring(0, 110) + '...' : note.description) : 'Curriculum note materials and lecture recordings.'}
              </p>

              <div style={{ borderTop: '1px solid var(--border)', paddingTop: '1rem', marginTop: 'auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  <User size={14} /> {note.uploaderName}
                </span>
                <Link
                  to={`/notes/${note.id}`}
                  className="btn btn-outline"
                  style={{ padding: '0.4rem 0.85rem', fontSize: '0.825rem', gap: '0.35rem' }}
                >
                  <Folder size={14} color="var(--primary)" /> View Files ({note.files?.length || 0}) <ArrowRight size={13} />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
