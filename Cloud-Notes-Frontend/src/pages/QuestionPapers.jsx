import React, { useState, useEffect } from 'react';
import api from '../api/api';
import { useBookmarks } from '../context/BookmarksContext';
import DocumentViewerModal from '../components/DocumentViewerModal';
import {
  Search,
  Download,
  Calendar,
  Layers,
  ChevronDown,
  ChevronUp,
  Folder,
  Eye,
  Bookmark
} from 'lucide-react';

export default function QuestionPapers() {
  const [papers, setPapers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [branch, setBranch] = useState('All');
  const [semester, setSemester] = useState('All');
  const [examType, setExamType] = useState('All');
  const [expandedCardId, setExpandedCardId] = useState(null);
  const [previewFile, setPreviewFile] = useState(null);
  const [showSavedOnly, setShowSavedOnly] = useState(false);

  const { isBookmarked, toggleBookmark } = useBookmarks();

  const fetchPapers = async () => {
    setLoading(true);
    try {
      const params = {};
      if (query.trim()) params.query = query.trim();
      if (branch !== 'All') params.branch = branch;
      if (semester !== 'All') params.semester = parseInt(semester, 10);
      if (examType !== 'All') params.examType = examType;

      const res = await api.get('/papers', { params });
      if (res.data.success) {
        setPapers(res.data.data.content);
      }
    } catch (err) {
      console.error('Failed to fetch papers', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPapers();
  }, [branch, semester, examType]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchPapers();
  };

  const handleReset = () => {
    setQuery('');
    setBranch('All');
    setSemester('All');
    setExamType('All');
    setShowSavedOnly(false);
  };

  const toggleExpand = (id) => {
    setExpandedCardId(expandedCardId === id ? null : id);
  };

  const displayedPapers = showSavedOnly
    ? papers.filter((p) => isBookmarked(p.id, 'papers'))
    : papers;

  return (
    <div className="container" style={{ padding: '2.5rem 1.5rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2.1rem', fontWeight: 800, marginBottom: '0.4rem', letterSpacing: '-0.025em' }}>
          Examination Archives
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>
          Access, study, and preview past midterm, semester-end, and supplementary question papers.
        </p>
      </div>

      <form onSubmit={handleSearchSubmit} className="filter-bar">
        <div className="filter-item" style={{ flex: 2, minWidth: '220px' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Search papers by subject, year or title..."
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
            {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
              <option key={s} value={s}>Semester {s}</option>
            ))}
          </select>
        </div>
        <div className="filter-item">
          <select className="form-control" value={examType} onChange={(e) => setExamType(e.target.value)}>
            <option value="All">All Exam Types</option>
            <option value="MID_SEMESTER">Mid Semester</option>
            <option value="END_SEMESTER">End Semester</option>
            <option value="SUPPLEMENTARY">Supplementary</option>
            <option value="INTERNAL">Internal</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
        <div style={{ display: 'flex', gap: '0.6rem', alignItems: 'center' }}>
          <button
            type="button"
            onClick={() => setShowSavedOnly(!showSavedOnly)}
            className={`btn ${showSavedOnly ? 'btn-primary' : 'btn-outline'}`}
            title="Filter saved papers"
            style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
          >
            <Bookmark size={15} fill={showSavedOnly ? 'currentColor' : 'none'} />
            Saved
          </button>
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
          <p style={{ color: 'var(--text-muted)', marginTop: '1rem', fontWeight: 500 }}>Retrieving examination archives...</p>
        </div>
      ) : displayedPapers.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <Layers size={52} style={{ color: 'var(--text-muted)', marginBottom: '1rem', opacity: 0.7 }} />
          <h3 style={{ fontSize: '1.25rem' }}>
            {showSavedOnly ? 'No saved question papers' : 'No question papers found'}
          </h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem', maxWidth: '420px', margin: '0.5rem auto 0' }}>
            {showSavedOnly
              ? 'Click the bookmark icon on any paper card to save it for quick revision.'
              : 'Try selecting a different branch, semester, or search query.'}
          </p>
          {showSavedOnly && (
            <button onClick={() => setShowSavedOnly(false)} className="btn btn-outline" style={{ marginTop: '1.25rem' }}>
              View All Papers
            </button>
          )}
        </div>
      ) : (
        <div className="grid">
          {displayedPapers.map((paper) => {
            const isExpanded = expandedCardId === paper.id;
            const files = paper.files || [];
            const hasMultipleFiles = files.length > 1;
            const saved = isBookmarked(paper.id, 'papers');

            return (
              <div key={paper.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
                {/* Header tags & Bookmark */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.85rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span className="badge badge-amber">
                      {paper.examType.replace('_', ' ')}
                    </span>
                    <span style={{ fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                      Year {paper.year}
                    </span>
                  </div>

                  <button
                    onClick={() => toggleBookmark(paper, 'papers')}
                    className="btn btn-outline"
                    style={{
                      padding: '0.3rem 0.5rem',
                      borderRadius: '6px',
                      color: saved ? '#f59e0b' : 'var(--text-muted)',
                      borderColor: saved ? 'rgba(245, 158, 11, 0.4)' : 'var(--border)'
                    }}
                    title={saved ? 'Remove from saved' : 'Bookmark paper'}
                  >
                    <Bookmark size={15} fill={saved ? '#f59e0b' : 'none'} />
                  </button>
                </div>

                {/* Title */}
                <h3 style={{ fontSize: '1.2rem', lineHeight: 1.4, marginBottom: '0.4rem', fontWeight: 700 }}>
                  {paper.title}
                </h3>

                {/* Subtitle */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.85rem' }}>
                  <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--primary)' }}>
                    {paper.subject}
                  </span>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>•</span>
                  <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)', fontWeight: 500 }}>
                    {paper.branch} (Sem {paper.semester})
                  </span>
                </div>

                {/* Description */}
                <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', flex: 1, marginBottom: '1.25rem', lineHeight: 1.5 }}>
                  {paper.description || 'Archive examination question paper.'}
                </p>

                {/* Footer and Downloads Bar */}
                <div style={{ borderTop: '1px solid var(--border)', paddingTop: '1rem', marginTop: 'auto' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.825rem', color: 'var(--secondary)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                      <Folder size={15} color="var(--primary)" /> {files.length} document{files.length !== 1 ? 's' : ''}
                    </span>

                    {files.length === 1 ? (
                      <div style={{ display: 'flex', gap: '0.4rem' }}>
                        <button
                          onClick={() => setPreviewFile(files[0])}
                          className="btn btn-outline"
                          style={{ padding: '0.35rem 0.65rem', fontSize: '0.8rem' }}
                          title="Preview paper"
                        >
                          <Eye size={14} /> Preview
                        </button>
                        <a
                          href={`http://localhost:8085${files[0].downloadUrl}`}
                          download={files[0].originalFileName}
                          className="btn btn-primary"
                          style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}
                        >
                          <Download size={14} /> Download
                        </a>
                      </div>
                    ) : (
                      <button
                        onClick={() => toggleExpand(paper.id)}
                        className="btn btn-outline"
                        style={{ padding: '0.4rem 0.85rem', fontSize: '0.825rem', gap: '0.35rem' }}
                      >
                        {isExpanded ? 'Hide Files' : 'View & Preview'}
                        {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                      </button>
                    )}
                  </div>

                  {/* Expandable Multi-File Drawer */}
                  {hasMultipleFiles && isExpanded && (
                    <div style={{ marginTop: '0.9rem', display: 'flex', flexDirection: 'column', gap: '0.5rem', borderTop: '1px dashed var(--border)', paddingTop: '0.75rem' }}>
                      {files.map((file) => (
                        <div
                          key={file.fileId}
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '0.45rem 0.65rem',
                            background: 'var(--surface-alt)',
                            borderRadius: '6px',
                            fontSize: '0.825rem'
                          }}
                        >
                          <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', marginRight: '0.5rem', fontWeight: 500 }}>
                            {file.originalFileName}
                          </div>
                          <div style={{ display: 'flex', gap: '0.35rem' }}>
                            <button
                              onClick={() => setPreviewFile(file)}
                              className="btn btn-outline"
                              style={{ padding: '0.2rem 0.45rem', fontSize: '0.75rem', height: '26px' }}
                              title="Preview file"
                            >
                              <Eye size={12} />
                            </button>
                            <a
                              href={`http://localhost:8085${file.downloadUrl}`}
                              download={file.originalFileName}
                              className="btn btn-primary"
                              style={{ padding: '0.2rem 0.55rem', fontSize: '0.75rem', height: '26px' }}
                            >
                              <Download size={12} />
                            </a>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

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
