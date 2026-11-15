import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/api';
import { useAuth } from '../context/AuthContext';
import { useBookmarks } from '../context/BookmarksContext';
import DocumentViewerModal from '../components/DocumentViewerModal';
import StarRating from '../components/StarRating';
import {
  FileText,
  Download,
  Sparkles,
  Calendar,
  User,
  ArrowLeft,
  AlertCircle,
  Eye,
  Bookmark,
  Share2,
  Check
} from 'lucide-react';

export default function NoteDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const { isBookmarked, toggleBookmark } = useBookmarks();

  const [note, setNote] = useState(null);
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState(null);
  const [summarizing, setSummarizing] = useState(false);
  const [summaryError, setSummaryError] = useState('');
  const [previewFile, setPreviewFile] = useState(null);
  const [copied, setCopied] = useState(false);

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

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
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

  const saved = isBookmarked(note.id, 'notes');

  return (
    <div className="container" style={{ padding: '2rem 1.5rem', maxWidth: '900px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <Link to="/notes" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)' }}>
          <ArrowLeft size={16} /> Back to Browse
        </Link>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            onClick={handleShare}
            className="btn btn-outline"
            style={{ padding: '0.45rem 0.85rem', fontSize: '0.85rem' }}
            title="Share note link"
          >
            {copied ? <Check size={15} color="var(--success)" /> : <Share2 size={15} />}
            {copied ? 'Link Copied!' : 'Share'}
          </button>
          <button
            onClick={() => toggleBookmark(note, 'notes')}
            className="btn btn-outline"
            style={{
              padding: '0.45rem 0.85rem',
              fontSize: '0.85rem',
              color: saved ? '#f59e0b' : 'inherit',
              borderColor: saved ? 'rgba(245, 158, 11, 0.4)' : 'var(--border)'
            }}
          >
            <Bookmark size={15} fill={saved ? '#f59e0b' : 'none'} />
            {saved ? 'Saved' : 'Bookmark'}
          </button>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '0.75rem' }}>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <span className="badge">{note.branch}</span>
            <span className="badge badge-gray">Semester {note.semester}</span>
          </div>
          <StarRating noteId={note.id} size={18} />
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
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'var(--danger-light)', color: 'var(--danger)', padding: '0.75rem', borderRadius: '6px', marginBottom: '1rem', fontSize: '0.875rem' }}>
            <AlertCircle size={18} /> {summaryError}
          </div>
        )}

        {summary && (
          <div style={{ background: 'var(--purple-light)', border: '1px solid rgba(139, 92, 246, 0.3)', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontWeight: 600, color: '#7c3aed', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <Sparkles size={18} /> {summary.title}
              </span>
              <span style={{ fontSize: '0.75rem', color: '#8b5cf6' }}>Provider: {summary.provider}</span>
            </div>
            <div style={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit', fontSize: '0.9rem', lineHeight: 1.6, color: 'var(--text-main)' }}>
              {summary.markdownContent}
            </div>
          </div>
        )}

        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {note.files?.map((file) => {
            const downloadUrl = file.downloadUrl?.startsWith('http')
              ? file.downloadUrl
              : `http://localhost:8085${file.downloadUrl}`;

            return (
              <div
                key={file.fileId}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '0.75rem 1rem',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  backgroundColor: 'var(--surface-alt)'
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

                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                  {/* In-browser preview button */}
                  <button
                    onClick={() => setPreviewFile(file)}
                    className="btn btn-outline"
                    style={{ padding: '0.35rem 0.65rem', fontSize: '0.85rem' }}
                    title="Read & preview document in browser"
                  >
                    <Eye size={15} /> Preview
                  </button>

                  {user && file.originalFileName.toLowerCase().endsWith('.pdf') && (
                    <button
                      onClick={() => handleGenerateSummary(file.fileId)}
                      disabled={summarizing}
                      className="btn btn-outline"
                      style={{ padding: '0.35rem 0.65rem', fontSize: '0.8rem', color: '#7c3aed' }}
                    >
                      Summarize
                    </button>
                  )}

                  <a
                    href={downloadUrl}
                    download={file.originalFileName}
                    className="btn btn-primary"
                    style={{ padding: '0.35rem 0.75rem', fontSize: '0.85rem' }}
                  >
                    <Download size={15} /> Download
                  </a>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Document Viewer Modal */}
      {previewFile && (
        <DocumentViewerModal
          file={previewFile}
          onClose={() => setPreviewFile(null)}
        />
      )}
    </div>
  );
}
