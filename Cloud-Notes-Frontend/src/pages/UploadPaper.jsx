import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import { FileText, X, AlertCircle } from 'lucide-react';

export default function UploadPaper() {
  const [formData, setFormData] = useState({
    title: '',
    subject: '',
    branch: 'Computer Science',
    semester: 1,
    year: new Date().getFullYear(),
    examType: 'END_SEMESTER',
    description: ''
  });
  const [files, setFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleFileChange = (e) => {
    const selectedFiles = Array.from(e.target.files);
    if (files.length + selectedFiles.length > 10) {
      setError('You can upload a maximum of 10 files per question paper.');
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
      setError('Please attach at least one question paper document or scan.');
      return;
    }

    setUploading(true);
    setError('');

    const data = new FormData();
    data.append('title', formData.title);
    data.append('subject', formData.subject);
    data.append('branch', formData.branch);
    data.append('semester', formData.semester);
    data.append('year', formData.year);
    data.append('examType', formData.examType);
    data.append('description', formData.description);

    files.forEach((file) => {
      data.append('files', file);
    });

    try {
      const res = await api.post('/papers', data, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      if (res.data.success) {
        navigate('/papers');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload question paper.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '680px', padding: '2rem 1.5rem' }}>
      <div className="card">
        <h2 style={{ marginBottom: '0.5rem' }}>Upload Question Paper</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.9rem' }}>
          Contribute previous examination question papers to assist peers in test preparation.
        </p>

        {error && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: '#fee2e2', color: '#dc2626', padding: '0.75rem', borderRadius: '6px', marginBottom: '1rem', fontSize: '0.875rem' }}>
            <AlertCircle size={18} /> {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Paper Title *</label>
            <input
              type="text"
              required
              className="form-control"
              placeholder="e.g. Data Structures & Algorithms End-Semester Dec 2023"
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
              placeholder="e.g. Data Structures"
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

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Exam Year</label>
              <input
                type="number"
                min={2000}
                max={2030}
                className="form-control"
                value={formData.year}
                onChange={(e) => setFormData({ ...formData, year: parseInt(e.target.value, 10) })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Exam Type</label>
              <select
                className="form-control"
                value={formData.examType}
                onChange={(e) => setFormData({ ...formData, examType: e.target.value })}
              >
                <option value="MID_SEMESTER">Mid Semester</option>
                <option value="END_SEMESTER">End Semester</option>
                <option value="SUPPLEMENTARY">Supplementary</option>
                <option value="INTERNAL">Internal</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Description (Optional)</label>
            <textarea
              rows={2}
              className="form-control"
              placeholder="Exam session, marking scheme details, syllabus pattern..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Attach Question Paper Files (PDF, Images) *</label>
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
            {uploading ? 'Uploading Paper...' : 'Publish Question Paper'}
          </button>
        </form>
      </div>
    </div>
  );
}
