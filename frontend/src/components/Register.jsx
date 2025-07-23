import React, { useState } from 'react';
import { register } from '../services/authService';
import '../styles/register.css'; // Import from styles folder

const Register = ({ onRegisterSuccess, switchToLogin }) => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

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

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long');
      setLoading(false);
      return;
    }

    try {
      // Prepare registration data for backend
      const registrationData = {
        username: formData.username,
        email: formData.email,
        password: formData.password,
        role: 'Student' // Default role for students
      };
      
      // Make real API call to backend
      const response = await register(registrationData);
      console.log('Registration successful:', response.data);
      
      // Extract user data from response
      const userData = {
        id: response.data.user.id,
        name: response.data.user.username || response.data.user.email?.split('@')[0] || 'Student',
        email: response.data.user.email,
        role: response.data.user.role?.toLowerCase() || 'student'
      };
      
      onRegisterSuccess(userData);
    } catch (error) {
      console.error('Registration error:', error);
      
      // Handle different types of errors
      if (error.response?.status === 400) {
        const errorMessage = error.response.data.message;
        if (errorMessage.includes('Username already exists')) {
          setError('Username already exists. Please choose a different username.');
        } else if (errorMessage.includes('Email already exists')) {
          setError('Email already exists. Please use a different email address.');
        } else if (errorMessage.includes('Google login')) {
          setError('An account with this email already exists using Google login. Please use Google sign-in instead.');
        } else {
          setError(errorMessage || 'Registration failed. Please try again.');
        }
      } else if (error.response?.status === 500) {
        setError('Server error. Please try again later.');
      } else if (error.code === 'NETWORK_ERROR' || error.message?.includes('Network Error')) {
        setError('Network error. Please check your connection and try again.');
      } else {
        setError('Registration failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-container">
      <h2 className="register-title">Student Registration</h2>
      <p className="register-subtitle">Create your student account to access academic forms</p>
      
      <form onSubmit={handleSubmit} className="register-form">
        <div className="form-group">
          <label htmlFor="email">Email Address</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="Enter your email address"
            required
            className="form-input"
            disabled={loading}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="username">Username</label>
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="Choose a username"
            required
            className="form-input"
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
            placeholder="Create a password (min 6 characters)"
            required
            className="form-input"
            disabled={loading}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="confirmPassword">Confirm Password</label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="Confirm your password"
            required
            className="form-input"
            disabled={loading}
          />
        </div>
        
        {error && <div className="error-message">{error}</div>}
        
        <button 
          type="submit" 
          disabled={loading}
          className="register-button"
        >
          {loading ? 'Creating Account...' : 'Create Account'}
        </button>
      </form>
      
      <div className="register-footer">
        <p>Already have an account? <button onClick={switchToLogin} className="link-button">Login here</button></p>
        <p className="demo-note">💡 Demo: All fields are required for registration</p>
      </div>
    </div>
  );
};

export default Register;