import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/api';
import { useAuth } from '../context/AuthContext';
import { FileText, Download, Sparkles, Calendar, User, ArrowLeft, AlertCircle } from 'lucide-react';

export default function NoteDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const [note, setNote] = useState(null);
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState(null);
  const [summarizing, setSummarizing] = useState(false);
  const [summaryError, setSummaryError] = useState('');

  useEffect(() => {
    api.get(`/notes/${id}`)
      .then((res) => {
        if (res.data.success) {
          setNote(res.data.data);
        }
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [id]);

  const handleGenerateSummary = async (fileId) => {
    setSummarizing(true);
    setSummaryError('');
    try {
      const res = await api.post(`/ai/summarize?noteId=${id}${fileId ? `&fileId=${fileId}` : ''}`);
      if (res.data.success) {
        setSummary(res.data.data);
      }
    } catch (err) {
      setSummaryError(err.response?.data?.message || 'Failed to generate summary.');
    } finally {
      setSummarizing(false);
    }
  };

  if (loading) {
    return <div className="container" style={{ padding: '3rem', textAlign: 'center' }}>Loading note details...</div>;
  }

  if (!note) {
    return (
      <div className="container" style={{ padding: '3rem', textAlign: 'center' }}>
        <h2>Note not found</h2>
        <Link to="/notes" className="btn btn-outline" style={{ marginTop: '1rem' }}>Back to Notes</Link>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2rem 1.5rem', maxWidth: '900px' }}>
      <Link to="/notes" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
        <ArrowLeft size={16} /> Back to Browse
      </Link>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
          <span className="badge">{note.branch}</span>
          <span className="badge" style={{ backgroundColor: '#f1f5f9', color: '#475569' }}>Semester {note.semester}</span>
        </div>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>{note.title}</h1>
        <p style={{ color: 'var(--primary)', fontWeight: 600, fontSize: '1.1rem', marginBottom: '1rem' }}>
          {note.subject}
        </p>

        <p style={{ color: 'var(--secondary)', lineHeight: 1.6, marginBottom: '1.5rem' }}>
          {note.description || 'No detailed description available.'}
        </p>

        <div style={{ display: 'flex', gap: '1.5rem', borderTop: '1px solid var(--border)', paddingTop: '1rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <User size={16} /> Uploaded by {note.uploaderName}
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Calendar size={16} /> {new Date(note.createdAt).toLocaleDateString()}
          </span>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1.25rem' }}>Attached Documents ({note.files?.length || 0})</h2>
          {user && (
            <button
              onClick={() => handleGenerateSummary(null)}
              disabled={summarizing}
              className="btn btn-outline"
              style={{ color: '#7c3aed', borderColor: '#c4b5fd' }}
            >
              <Sparkles size={16} /> {summarizing ? 'Analyzing document...' : 'AI Study Summary'}
            </button>
          )}
        </div>

        {summaryError && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: '#fee2e2', color: '#dc2626', padding: '0.75rem', borderRadius: '6px', marginBottom: '1rem', fontSize: '0.875rem' }}>
            <AlertCircle size={18} /> {summaryError}
          </div>
        )}

        {summary && (
          <div style={{ background: '#f5f3ff', border: '1px solid #ddd6fe', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontWeight: 600, color: '#6d28d9', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <Sparkles size={18} /> {summary.title}
              </span>
              <span style={{ fontSize: '0.75rem', color: '#8b5cf6' }}>Provider: {summary.provider}</span>
            </div>
            <div style={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit', fontSize: '0.9rem', lineHeight: 1.6, color: '#334155' }}>
              {summary.markdownContent}
            </div>
          </div>
        )}

        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {note.files?.map((file) => (
            <div
              key={file.fileId}
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '0.75rem 1rem',
                border: '1px solid var(--border)',
                borderRadius: '6px'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <FileText size={20} color="var(--primary)" />
                <div>
                  <div style={{ fontWeight: 500, fontSize: '0.95rem' }}>{file.originalFileName}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    {(file.size / (1024 * 1024)).toFixed(2)} MB • {file.contentType}
                  </div>
                </div>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                {user && file.originalFileName.toLowerCase().endsWith('.pdf') && (
                  <button
                    onClick={() => handleGenerateSummary(file.fileId)}
                    disabled={summarizing}
                    className="btn btn-outline"
                    style={{ padding: '0.35rem 0.6rem', fontSize: '0.8rem', color: '#7c3aed' }}
                  >
                    Summarize PDF
                  </button>
                )}
                <a
                  href={`http://localhost:8085${file.downloadUrl}`}
                  download={file.originalFileName}
                  className="btn btn-primary"
                  style={{ padding: '0.35rem 0.75rem', fontSize: '0.85rem' }}
                >
                  <Download size={15} /> Download
                </a>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
