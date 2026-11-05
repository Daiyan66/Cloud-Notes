import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Notes from './pages/Notes';
import NoteDetail from './pages/NoteDetail';
import UploadNote from './pages/UploadNote';
import QuestionPapers from './pages/QuestionPapers';
import UploadPaper from './pages/UploadPaper';
import Dashboard from './pages/Dashboard';
import Admin from './pages/Admin';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { user, loading, isAdmin } = useAuth();
  if (loading) return <div>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (requireAdmin && !isAdmin) return <Navigate to="/dashboard" replace />;
  return children;
};

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
          <Navbar />
          <main style={{ flex: 1 }}>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/notes" element={<Notes />} />
              <Route path="/notes/:id" element={<NoteDetail />} />
              <Route path="/papers" element={<QuestionPapers />} />

              {/* Protected Student Routes */}
              <Route
                path="/upload-note"
                element={
                  <ProtectedRoute>
                    <UploadNote />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/upload-paper"
                element={
                  <ProtectedRoute>
                    <UploadPaper />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <Dashboard />
                  </ProtectedRoute>
                }
              />

              {/* Protected Admin Route */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute requireAdmin>
                    <Admin />
                  </ProtectedRoute>
                }
              />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
