import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import AdminDashboard from './components/AdminDashboard';
import ApproverDashboard from './components/ApproverDashboard';
import ReportsDashboard from './components/ReportsDashboard';
import AuditTrail from './components/AuditTrail';
import FileUpload from './components/FileUpload';
import FormSubmission from './components/FormSubmission';
import { logout, getCurrentUser } from './services/authService';
import Notifications from './components/Notifications';
import './styles/global.css';

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const user = getCurrentUser();
  return user ? children : <Navigate to="/login" replace />;
};

// Public Route Component (redirects to appropriate dashboard if already logged in)
const PublicRoute = ({ children }) => {
  const user = getCurrentUser();
  if (!user) return children;
  
  // Redirect to appropriate dashboard based on role
  if (user.role && user.role.toLowerCase() === 'admin') {
    return <Navigate to="/admin/dashboard" replace />;
  } else if (user.role && (user.role.toLowerCase() === 'approver' || user.role.toLowerCase() === 'department head' || user.role.toLowerCase() === 'academic staff')) {
    return <Navigate to="/approver/dashboard" replace />;
  } else {
    return <Navigate to="/student/dashboard" replace />;
  }
};

// Login Component with Navigation
const LoginPage = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleLoginSuccess = (userData) => {
    setIsLoading(true);
    
    // Simulate API call delay
    setTimeout(() => {
      // Set default student role if not provided
      const userWithRole = {
        ...userData,
        role: userData.role || 'student',
        name: userData.name || userData.email?.split('@')[0] || 'Student'
      };
      
      setUser(userWithRole);
      sessionStorage.setItem('userInfo', JSON.stringify(userWithRole));
      setIsLoading(false);
      console.log('User logged in:', userWithRole);
      
      // Navigate to correct dashboard based on role
      if (userWithRole.role && userWithRole.role.toLowerCase() === 'admin') {
        navigate('/admin');
      } else if (userWithRole.role && (userWithRole.role.toLowerCase() === 'approver' || userWithRole.role.toLowerCase() === 'department head' || userWithRole.role.toLowerCase() === 'academic staff')) {
        navigate('/approver');
      } else {
        navigate('/student/dashboard');
      }
    }, 1000);
  };

  const switchToRegister = () => navigate('/register');

  if (isLoading) {
    return (
      <div className="app">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <main className="auth-container">
      <Login
        onLoginSuccess={handleLoginSuccess}
        switchToRegister={switchToRegister}
      />
    </main>
  );
};

// Register Component with Navigation
const RegisterPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const handleRegisterSuccess = (userData) => {
    setIsLoading(true);
    // Simulate API call delay
    setTimeout(() => {
      // Do NOT set user in localStorage after registration
      setIsLoading(false);
      console.log('User registered:', userData);
      // Redirect to login page after registration
      navigate('/login');
    }, 1000);
  };

  const switchToLogin = () => navigate('/login');

  if (isLoading) {
    return (
      <div className="app">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <main className="auth-container">
      <Register
        onRegisterSuccess={handleRegisterSuccess}
        switchToLogin={switchToLogin}
      />
    </main>
  );
};

// Dashboard Component with Navigation
const DashboardPage = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const currentUser = getCurrentUser();
    if (currentUser) {
      const userData = {
        id: currentUser.id,
        name: currentUser.username || currentUser.email?.split('@')[0] || 'Student',
        email: currentUser.email,
        role: currentUser.role?.toLowerCase() || 'student',
        studentId: currentUser.studentId || '2024-0001'
      };
      setUser(userData);
      console.log('User session restored from authService:', userData);
    } else {
      navigate('/login');
    }
  }, [navigate]);

  const handleLogout = async () => {
    setIsLoading(true);
    try {
      await logout();
      setUser(null);
      setIsLoading(false);
      console.log('User logged out successfully');
      navigate('/login');
    } catch (error) {
      console.error('Logout error:', error);
      setUser(null);
      setIsLoading(false);
      navigate('/login');
    }
  };

  if (isLoading) {
    return (
      <div className="app">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  // Role-based dashboard rendering
  if (user.role && user.role.toLowerCase() === 'admin') {
    return <AdminDashboard user={user} onLogout={handleLogout} />;
  }
  if (user.role && (user.role.toLowerCase() === 'approver' || user.role.toLowerCase() === 'department head' || user.role.toLowerCase() === 'academic staff')) {
    return <ApproverDashboard user={user} onLogout={handleLogout} />;
  }
  // Default: Student dashboard
  return <Dashboard user={user} onLogout={handleLogout} />;
};

// Main App Component
const App = () => {
  return (
    <Router>
      <div className="app">
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          } />
          <Route path="/register" element={
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          } />
          
          {/* Protected Student Routes */}
          <Route path="/student" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          <Route path="/student/dashboard" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          <Route path="/student/forms" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          <Route path="/student/form-submission" element={
            <ProtectedRoute>
              <FormSubmission />
            </ProtectedRoute>
          } />
          <Route path="/student/file-upload/:formId" element={
            <ProtectedRoute>
              <FileUpload />
            </ProtectedRoute>
          } />
          <Route path="/student/tracking" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          <Route path="/student/notifications" element={
            <ProtectedRoute>
              <Notifications />
            </ProtectedRoute>
          } />
          <Route path="/student/profile" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          
          {/* Protected Approver Routes */}
          <Route path="/approver" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          <Route path="/approver/dashboard" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          <Route path="/approver/*" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          {/* Protected Admin Routes */}
          <Route path="/admin" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          <Route path="/admin/dashboard" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          <Route path="/admin/reports" element={
            <ProtectedRoute>
              <ReportsDashboard />
            </ProtectedRoute>
          } />
          <Route path="/admin/audit-trail" element={
            <ProtectedRoute>
              <AuditTrail />
            </ProtectedRoute>
          } />
          <Route path="/admin/*" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />
          
          {/* Default redirects */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </div>
    </Router>
  );
};

export default App;
