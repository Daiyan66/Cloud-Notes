import React, { useState, useEffect } from 'react';
import { X, Download, Maximize2, Minimize2, ExternalLink, FileText, AlertTriangle } from 'lucide-react';

export default function DocumentViewerModal({ file, onClose }) {
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [loadError, setLoadError] = useState(false);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  if (!file) return null;

  const downloadUrl = file.downloadUrl
    ? (file.downloadUrl.startsWith('http') ? file.downloadUrl : `http://localhost:8085${file.downloadUrl}`)
    : '#';

  const isPdf = file.contentType === 'application/pdf' || file.originalFileName?.toLowerCase().endsWith('.pdf');
  const isImage = file.contentType?.startsWith('image/') || /\.(jpe?g|png|webp|gif|svg)$/i.test(file.originalFileName || '');

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 1000,
        backgroundColor: 'rgba(0, 0, 0, 0.75)',
        backdropFilter: 'blur(4px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: isFullscreen ? '0' : '1.5rem',
        animation: 'fadeIn 0.2s ease-out'
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        style={{
          width: isFullscreen ? '100vw' : '92vw',
          maxWidth: isFullscreen ? '100vw' : '1100px',
          height: isFullscreen ? '100vh' : '90vh',
          backgroundColor: 'var(--surface)',
          borderRadius: isFullscreen ? '0' : 'var(--radius)',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-hover)',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          transition: 'all 0.2s ease'
        }}
      >
        {/* Header */}
        <div
          style={{
            padding: '0.85rem 1.25rem',
            borderBottom: '1px solid var(--border)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: 'var(--surface-alt)',
            gap: '1rem'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', minWidth: 0 }}>
            <FileText size={20} color="var(--primary)" style={{ flexShrink: 0 }} />
            <div style={{ minWidth: 0 }}>
              <div
                style={{
                  fontWeight: 600,
                  fontSize: '0.95rem',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                  color: 'var(--text-main)'
                }}
                title={file.originalFileName}
              >
                {file.originalFileName}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                {file.size ? `${(file.size / (1024 * 1024)).toFixed(2)} MB • ` : ''}
                {isPdf ? 'PDF Document' : isImage ? 'Image File' : file.contentType || 'Document'}
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexShrink: 0 }}>
            <a
              href={downloadUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="btn btn-outline"
              style={{ padding: '0.4rem 0.65rem', fontSize: '0.8rem' }}
              title="Open in new browser tab"
            >
              <ExternalLink size={15} />
            </a>
            <a
              href={downloadUrl}
              download={file.originalFileName}
              className="btn btn-primary"
              style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem' }}
              title="Download file"
            >
              <Download size={15} /> Download
            </a>
            <button
              onClick={() => setIsFullscreen(!isFullscreen)}
              className="btn btn-outline"
              style={{ padding: '0.4rem 0.65rem', fontSize: '0.8rem' }}
              title={isFullscreen ? 'Exit full screen' : 'Full screen'}
            >
              {isFullscreen ? <Minimize2 size={15} /> : <Maximize2 size={15} />}
            </button>
            <button
              onClick={onClose}
              className="btn btn-outline"
              style={{ padding: '0.4rem 0.65rem', fontSize: '0.8rem', color: 'var(--danger)' }}
              title="Close preview (Esc)"
            >
              <X size={16} />
            </button>
          </div>
        </div>

        {/* Content Viewer Body */}
        <div
          style={{
            flex: 1,
            backgroundColor: isImage ? 'rgba(0, 0, 0, 0.05)' : '#525659',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            overflow: 'auto',
            position: 'relative'
          }}
        >
          {isImage ? (
            <div style={{ padding: '1.5rem', display: 'flex', justifyContent: 'center', alignItems: 'center', width: '100%', height: '100%' }}>
              <img
                src={downloadUrl}
                alt={file.originalFileName}
                style={{
                  maxWidth: '100%',
                  maxHeight: '100%',
                  objectFit: 'contain',
                  borderRadius: 'var(--radius-sm)',
                  boxShadow: 'var(--shadow-md)'
                }}
                onError={() => setLoadError(true)}
              />
            </div>
          ) : isPdf ? (
            <iframe
              src={`${downloadUrl}#toolbar=1&navpanes=0`}
              title={file.originalFileName}
              style={{ width: '100%', height: '100%', border: 'none' }}
              onError={() => setLoadError(true)}
            />
          ) : (
            <div
              style={{
                textAlign: 'center',
                padding: '2.5rem',
                backgroundColor: 'var(--surface)',
                borderRadius: 'var(--radius)',
                maxWidth: '480px',
                border: '1px solid var(--border)',
                margin: '1.5rem'
              }}
            >
              <AlertTriangle size={44} style={{ color: 'var(--amber)', marginBottom: '0.85rem' }} />
              <h3 style={{ fontSize: '1.15rem', marginBottom: '0.5rem' }}>No direct in-browser preview</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
                This file type (<strong>{file.contentType || file.originalFileName?.split('.').pop()}</strong>) cannot be rendered directly in the embedded frame.
              </p>
              <a href={downloadUrl} download={file.originalFileName} className="btn btn-primary">
                <Download size={16} /> Download to View
              </a>
            </div>
          )}

          {loadError && (
            <div
              style={{
                position: 'absolute',
                inset: 0,
                backgroundColor: 'var(--surface)',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '2rem'
              }}
            >
              <AlertTriangle size={40} style={{ color: 'var(--danger)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>Failed to load preview</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1rem' }}>
                The backend file could not be rendered inside the frame.
              </p>
              <a href={downloadUrl} download={file.originalFileName} className="btn btn-primary">
                <Download size={16} /> Download File
              </a>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
