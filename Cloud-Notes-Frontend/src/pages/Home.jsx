import React from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, Sparkles, Download, ShieldCheck, ArrowRight } from 'lucide-react';

export default function Home() {
  return (
    <div>
      <section className="hero">
        <div className="container">
          <span className="badge" style={{ marginBottom: '1rem', padding: '0.4rem 0.8rem' }}>
            Academic Resource Platform & AI Study Engine
          </span>
          <h1 className="hero-title">
            Share, discover, and study with <span style={{ color: 'var(--primary)' }}>CloudNotes Hub</span>
          </h1>
          <p className="hero-subtitle">
            Centralized hub for university notes, syllabus materials, and previous exam question papers. Powered by server-side text extraction and AI-driven study summaries.
          </p>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <Link to="/notes" className="btn btn-primary" style={{ padding: '0.75rem 1.5rem', fontSize: '1rem' }}>
              Explore Notes <ArrowRight size={18} />
            </Link>
            <Link to="/papers" className="btn btn-outline" style={{ padding: '0.75rem 1.5rem', fontSize: '1rem' }}>
              Previous Exam Papers
            </Link>
          </div>
        </div>
      </section>

      <section style={{ padding: '3rem 0', background: '#fff', borderTop: '1px solid var(--border)' }}>
        <div className="container">
          <div className="grid">
            <div className="card">
              <div style={{ color: 'var(--primary)', marginBottom: '0.75rem' }}>
                <BookOpen size={28} />
              </div>
              <h3 style={{ marginBottom: '0.5rem' }}>Structured Course Notes</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                Organized systematically by branch, semester, and subject. Supports multi-file uploads (PDF, DOCX, images) with strict signature validation.
              </p>
            </div>

            <div className="card">
              <div style={{ color: '#059669', marginBottom: '0.75rem' }}>
                <Download size={28} />
              </div>
              <h3 style={{ marginBottom: '0.5rem' }}>Previous Exam Papers</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                Prepare with genuine mid-semester and end-semester question papers, sorted chronologically and filterable by exam category.
              </p>
            </div>

            <div className="card">
              <div style={{ color: '#7c3aed', marginBottom: '0.75rem' }}>
                <Sparkles size={28} />
              </div>
              <h3 style={{ marginBottom: '0.5rem' }}>AI Study Summaries</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                Generate instant structured Markdown summaries directly from uploaded PDFs with key concepts, formula highlights, and quick takeaways.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
