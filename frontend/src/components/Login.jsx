import React, { useState, useEffect } from 'react';
import { login, googleLogin } from '../services/authService';
import '../styles/login.css';

const Login = ({ onLoginSuccess, switchToRegister }) => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [googleLoaded, setGoogleLoaded] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      console.log('Attempting login with:', formData);
      
      // Make real API call to backend
      const response = await login(formData);
      console.log('Login successful:', response.data);
      
      // Extract user data from response
      const userData = {
        id: response.data.user.id,
        name: response.data.user.username || response.data.user.email?.split('@')[0] || 'Student',
        email: response.data.user.email,
        role: response.data.user.role?.toLowerCase() || 'student',
        studentId: response.data.user.studentId || '2024-0001'
      };
      
      onLoginSuccess(userData);
    } catch (error) {
      console.error('Login error details:', error);
      
      // Handle different types of errors
      if (error.response?.status === 401) {
        setError('Invalid username or password. Please check your credentials.');
      } else if (error.response?.status === 400) {
        setError(error.response.data.message || 'Login failed. Please try again.');
      } else if (error.response?.status === 500) {
        setError('Server error. Please try again later.');
      } else if (error.code === 'NETWORK_ERROR' || error.message?.includes('Network Error')) {
        setError('Network error. Please check your connection and try again.');
      } else {
        setError('Login failed. Please check your credentials and try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    if (!googleLoaded) {
      setError('Google Sign-In is still loading. Please wait...');
      return;
    }

    setGoogleLoading(true);
    setError('');

    try {
      if (!window.google) {
        throw new Error('Google SDK not loaded');
      }

      console.log('Initializing Google Sign-In...');
      
      // Use the popup method instead of one-tap for more reliable behavior
      window.google.accounts.id.initialize({
        client_id: '544924813725-qaco96p2kmurc75pu1v6sm96ldmvo0v1.apps.googleusercontent.com',
        callback: handleGoogleCallback,
        auto_select: false,
        cancel_on_tap_outside: true
      });

      console.log('Google Sign-In initialized, showing prompt...');

      // Show popup directly
      window.google.accounts.id.prompt((notification) => {
        console.log('Google prompt notification:', notification);
        
        if (notification.isNotDisplayed()) {
          console.log('One-tap not displayed:', notification.getNotDisplayedReason());
          setError(`Google Sign-In not displayed: ${notification.getNotDisplayedReason()}`);
          setGoogleLoading(false);
        } else if (notification.isSkippedMoment()) {
          console.log('One-tap skipped:', notification.getSkippedReason());
          setError(`Google Sign-In skipped: ${notification.getSkippedReason()}`);
          setGoogleLoading(false);
        } else if (notification.isDismissedMoment()) {
          console.log('One-tap dismissed:', notification.getDismissedReason());
          setError(`Google Sign-In dismissed: ${notification.getDismissedReason()}`);
          setGoogleLoading(false);
        } else {
          console.log('One-tap available, showing popup');
          // Fallback to button click
          document.getElementById("google-signin-button")?.click();
        }
      });

    } catch (error) {
      console.error('Google login error:', error);
      setError(`Google login failed: ${error.message}`);
      setGoogleLoading(false);
    }
  };

  const handleGoogleCallback = async (response) => {
    try {
      console.log('Google credential received');
      
      // Make real API call to backend
      const loginResponse = await googleLogin(response.credential);
      console.log('Google login successful:', loginResponse.data);
      
      // Extract user data from response
      const userData = {
        id: loginResponse.data.user.id,
        name: loginResponse.data.user.username || loginResponse.data.user.email?.split('@')[0] || 'Student',
        email: loginResponse.data.user.email,
        role: loginResponse.data.user.role?.toLowerCase() || 'student',
        studentId: loginResponse.data.user.studentId || '2024-0001'
      };
      
      onLoginSuccess(userData);
    } catch (error) {
      console.error('Google login callback error:', error);
      
      // Handle different types of errors
      if (error.response?.status === 401) {
        setError('Google login failed. Please try again.');
      } else if (error.response?.status === 400) {
        setError(error.response.data.message || 'Google login failed. Please try again.');
      } else if (error.response?.status === 500) {
        setError('Server error. Please try again later.');
      } else if (error.code === 'NETWORK_ERROR' || error.message?.includes('Network Error')) {
        setError('Network error. Please check your connection and try again.');
      } else {
        setError('Google login failed. Please try again.');
      }
    } finally {
      setGoogleLoading(false);
    }
  };

  // Load Google SDK
  useEffect(() => {
    const loadGoogleSDK = () => {
      // Check if already loaded
      if (window.google) {
        setGoogleLoaded(true);
        return;
      }

      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      
      script.onload = () => {
        console.log('Google SDK loaded');
        setGoogleLoaded(true);
        
        // Initialize Google Sign-In after SDK loads
        if (window.google) {
          window.google.accounts.id.initialize({
            client_id: '544924813725-qaco96p2kmurc75pu1v6sm96ldmvo0v1.apps.googleusercontent.com',
            callback: handleGoogleCallback,
            auto_select: false,
            cancel_on_tap_outside: true
          });

          // Render the hidden button for fallback
          if (document.getElementById("google-signin-button")) {
            window.google.accounts.id.renderButton(
              document.getElementById("google-signin-button"),
              { 
                theme: "outline", 
                size: "large",
                width: "100%"
              }
            );
          }
        }
      };

      script.onerror = () => {
        console.error('Failed to load Google SDK');
        setError('Failed to load Google Sign-In. Please refresh the page.');
      };

      document.head.appendChild(script);

      return () => {
        // Cleanup
        try {
          if (document.head.contains(script)) {
            document.head.removeChild(script);
          }
        } catch (e) {
          console.log('Script cleanup error:', e);
        }
      };
    };

    return loadGoogleSDK();
  }, []);

  return (
    <div className="login-container">
      <h2 className="login-title">Student Portal Login</h2>
      <p className="login-subtitle">Access your academic forms and track submissions</p>
      
      {/* Google Login Section */}
      <div className="google-login-section">
        <button
          type="button"
          onClick={handleGoogleLogin}
          disabled={googleLoading || !googleLoaded}
          className="google-login-button"
        >
          <svg width="18" height="18" viewBox="0 0 18 18" className="google-icon">
            <path fill="#4285F4" d="M16.51 8H8.98v3h4.3c-.18 1-.74 1.48-1.6 2.04v2.01h2.6a7.8 7.8 0 0 0 2.38-5.88c0-.57-.05-.66-.15-1.18z"/>
            <path fill="#34A853" d="M8.98 17c2.16 0 3.97-.72 5.3-1.94l-2.6-2.04a4.8 4.8 0 0 1-2.7.75 4.8 4.8 0 0 1-4.52-3.36H1.83v2.07A8 8 0 0 0 8.98 17z"/>
            <path fill="#FBBC05" d="M4.46 10.41a4.8 4.8 0 0 1-.25-1.41c0-.49.09-.97.25-1.41V5.52H1.83a8 8 0 0 0 0 7.37l2.63-2.05z"/>
            <path fill="#EA4335" d="M8.98 4.18c1.17 0 2.23.4 3.06 1.2l2.3-2.3A8 8 0 0 0 8.98 1 8 8 0 0 0 1.83 5.52L4.46 7.6a4.8 4.8 0 0 1 4.52-3.42z"/>
          </svg>
          {googleLoading ? 'Signing in with Google...' : 
           !googleLoaded ? 'Loading Google Sign-In...' : 
           'Continue with Google'}
        </button>
        {/* Hidden button for fallback */}
        <div id="google-signin-button" style={{ display: 'none' }}></div>
      </div>

      <div className="divider">
        <span>or</span>
      </div>

      {/* Error Display */}
      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {/* Login Form */}
      <form onSubmit={handleSubmit} className="login-form">
        <div className="form-group">
          <label htmlFor="username">Student ID or Email</label>
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="Enter your student ID or email"
            required
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="Enter your password"
            required
            disabled={loading}
          />
        </div>

        <button
          type="submit"
          className="login-button"
          disabled={loading}
        >
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>

      <div className="login-footer">
        <p>Don't have an account? <button onClick={switchToRegister} className="link-button">Register here</button></p>
        <p className="demo-note">💡 Demo: Use registered credentials or Google login</p>
      </div>
    </div>
  );
};

export default Login;