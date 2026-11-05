import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import { Upload, X, AlertCircle } from 'lucide-react';

export default function UploadNote() {
  const [formData, setFormData] = useState({
    title: '',
    subject: '',
    branch: 'Computer Science',
    semester: 1,
    description: ''
  });
  const [files, setFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleFileChange = (e) => {
    const selectedFiles = Array.from(e.target.files);
    if (files.length + selectedFiles.length > 10) {
      setError('You can upload a maximum of 10 files per note.');
      return;
    }

    for (let f of selectedFiles) {
      if (f.size > 10 * 1024 * 1024) {
        setError(`File "${f.name}" exceeds the 10MB limit.`);
        return;
      }
    }

    setError('');
    setFiles([...files, ...selectedFiles]);
  };

  const removeFile = (index) => {
    setFiles(files.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (files.length === 0) {
      setError('Please attach at least one note document.');
      return;
    }

    setUploading(true);
    setError('');

    const data = new FormData();
    data.append('title', formData.title);
    data.append('subject', formData.subject);
    data.append('branch', formData.branch);
    data.append('semester', formData.semester);
    data.append('description', formData.description);

    files.forEach((file) => {
      data.append('files', file);
    });

    try {
      const res = await api.post('/notes', data, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      if (res.data.success) {
        navigate(`/notes/${res.data.data.id}`);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload notes. Please check file types and limits.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '680px', padding: '2rem 1.5rem' }}>
      <div className="card">
        <h2 style={{ marginBottom: '0.5rem' }}>Upload Study Notes</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.9rem' }}>
          Share clean, high quality course notes with your classmates. Supported formats: PDF, JPG, PNG, DOC, DOCX (Max 10 files, 10MB each).
        </p>

        {error && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: '#fee2e2', color: '#dc2626', padding: '0.75rem', borderRadius: '6px', marginBottom: '1rem', fontSize: '0.875rem' }}>
            <AlertCircle size={18} /> {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Note Title *</label>
            <input
              type="text"
              required
              className="form-control"
              placeholder="e.g. Operating Systems Chapter 3: Memory Management"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Subject *</label>
            <input
              type="text"
              required
              className="form-control"
              placeholder="e.g. Operating Systems"
              value={formData.subject}
              onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Branch</label>
              <select
                className="form-control"
                value={formData.branch}
                onChange={(e) => setFormData({ ...formData, branch: e.target.value })}
              >
                <option value="Computer Science">Computer Science</option>
                <option value="Information Technology">Information Technology</option>
                <option value="Electronics">Electronics</option>
                <option value="Mechanical">Mechanical</option>
                <option value="Civil">Civil</option>
                <option value="Electrical">Electrical</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Semester</label>
              <select
                className="form-control"
                value={formData.semester}
                onChange={(e) => setFormData({ ...formData, semester: parseInt(e.target.value, 10) })}
              >
                {[1,2,3,4,5,6,7,8].map((s) => (
                  <option key={s} value={s}>Semester {s}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Description (Optional)</label>
            <textarea
              rows={3}
              className="form-control"
              placeholder="Key topics covered, reference textbooks, professors..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Attach Files (Up to 10 files, 10MB each) *</label>
            <input
              type="file"
              multiple
              accept=".pdf,.png,.jpg,.jpeg,.doc,.docx"
              onChange={handleFileChange}
              className="form-control"
            />
          </div>

          {files.length > 0 && (
            <div style={{ marginBottom: '1.25rem' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.5rem' }}>Selected Files ({files.length}/10):</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                {files.map((file, idx) => (
                  <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#f8fafc', padding: '0.4rem 0.75rem', borderRadius: '4px', fontSize: '0.85rem' }}>
                    <span>{file.name} ({(file.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    <button type="button" onClick={() => removeFile(idx)} style={{ border: 'none', background: 'transparent', color: 'var(--danger)' }}>
                      <X size={16} />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <button
            type="submit"
            disabled={uploading}
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.5rem' }}
          >
            {uploading ? 'Uploading Files...' : 'Publish Note'}
          </button>
        </form>
      </div>
    </div>
  );
}
