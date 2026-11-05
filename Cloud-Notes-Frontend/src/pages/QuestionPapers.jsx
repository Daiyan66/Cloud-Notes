import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';
import { Search, FileText, Download, Calendar, User } from 'lucide-react';

export default function QuestionPapers() {
  const [papers, setPapers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [branch, setBranch] = useState('All');
  const [semester, setSemester] = useState('All');
  const [examType, setExamType] = useState('All');

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
  };

  return (
    <div className="container" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>Previous Exam Question Papers</h1>
        <p style={{ color: 'var(--text-muted)' }}>Browse mid-term and semester examination archives.</p>
      </div>

      <form onSubmit={handleSearchSubmit} className="filter-bar">
        <div className="filter-item" style={{ flex: 2, minWidth: '220px' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Search exam papers by title or subject..."
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
            {[1,2,3,4,5,6,7,8].map((s) => (
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
          <p style={{ color: 'var(--text-muted)' }}>Loading question papers...</p>
        </div>
      ) : papers.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
          <FileText size={48} style={{ color: 'var(--text-muted)', marginBottom: '1rem' }} />
          <h3>No question papers found</h3>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>
            Try selecting different filters or upload the first paper for this course!
          </p>
        </div>
      ) : (
        <div className="grid">
          {papers.map((paper) => (
            <div key={paper.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                <span className="badge" style={{ backgroundColor: '#fef3c7', color: '#b45309' }}>
                  {paper.examType.replace('_', ' ')}
                </span>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Year {paper.year}</span>
              </div>
              <h3 style={{ fontSize: '1.15rem', marginBottom: '0.35rem' }}>{paper.title}</h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--primary)', fontWeight: 500, marginBottom: '0.5rem' }}>
                {paper.subject} • {paper.branch} (Sem {paper.semester})
              </p>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', flex: 1, marginBottom: '1rem' }}>
                {paper.description || 'Archive examination question paper.'}
              </p>

              <div style={{ borderTop: '1px solid var(--border)', paddingTop: '0.75rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  {paper.files?.length || 0} file(s)
                </span>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  {paper.files?.map((file) => (
                    <a
                      key={file.fileId}
                      href={`http://localhost:8085${file.downloadUrl}`}
                      download={file.originalFileName}
                      className="btn btn-primary"
                      style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }}
                    >
                      <Download size={14} /> Download
                    </a>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
