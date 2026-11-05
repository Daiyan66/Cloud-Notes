import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';
import { Search, Filter, BookOpen, Download, User } from 'lucide-react';

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
    <div className="container" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>Course Notes Hub</h1>
        <p style={{ color: 'var(--text-muted)' }}>Search and discover peer-reviewed student notes.</p>
      </div>

      <form onSubmit={handleSearchSubmit} className="filter-bar">
        <div className="filter-item" style={{ flex: 2, minWidth: '240px' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Search by title, subject, or keyword..."
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
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button type="submit" className="btn btn-primary">
            <Search size={16} /> Search
          </button>
          <button type="button" onClick={handleReset} className="btn btn-outline">
            Reset
          </button>
        </div>
      </form>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <p style={{ color: 'var(--text-muted)' }}>Loading course notes...</p>
        </div>
      ) : notes.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
          <BookOpen size={48} style={{ color: 'var(--text-muted)', marginBottom: '1rem' }} />
          <h3>No notes found</h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>
            Try adjusting your search criteria or filter options.
          </p>
        </div>
      ) : (
        <div className="grid">
          {notes.map((note) => (
            <div key={note.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                <span className="badge">{note.branch}</span>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Sem {note.semester}</span>
              </div>
              <h3 style={{ fontSize: '1.15rem', marginBottom: '0.35rem' }}>
                <Link to={`/notes/${note.id}`} style={{ color: 'inherit' }}>
                  {note.title}
                </Link>
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--primary)', fontWeight: 500, marginBottom: '0.5rem' }}>
                Subject: {note.subject}
              </p>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', flex: 1, marginBottom: '1rem' }}>
                {note.description ? (note.description.length > 100 ? note.description.substring(0, 100) + '...' : note.description) : 'No description provided.'}
              </p>
              <div style={{ borderTop: '1px solid var(--border)', paddingTop: '0.75rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                  <User size={14} /> {note.uploaderName}
                </span>
                <Link to={`/notes/${note.id}`} className="btn btn-outline" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }}>
                  View Files ({note.files?.length || 0})
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
