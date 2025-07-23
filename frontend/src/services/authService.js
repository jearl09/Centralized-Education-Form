// authService.js - Updated with Google OAuth support
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/auth';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor to handle token expiration
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      sessionStorage.removeItem('authToken');
      sessionStorage.removeItem('userInfo');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Traditional login function
export const login = async (credentials) => {
  try {
    const response = await api.post('/login', credentials);
    
    if (response.data.token) {
      sessionStorage.setItem('authToken', response.data.token);
      sessionStorage.setItem('userInfo', JSON.stringify(response.data.user));
    }
    
    return response;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

// Google OAuth login function
export const googleLogin = async (googleCredential) => {
  try {
    const response = await api.post('/google', {
      credential: googleCredential
    });
    
    if (response.data.token) {
      sessionStorage.setItem('authToken', response.data.token);
      sessionStorage.setItem('userInfo', JSON.stringify(response.data.user));
    }
    
    return response;
  } catch (error) {
    console.error('Google login error:', error);
    throw error;
  }
};

// Traditional register function
export const register = async (userData) => {
  try {
    const response = await api.post('/register', userData);
    
    if (response.data.token) {
      sessionStorage.setItem('authToken', response.data.token);
      sessionStorage.setItem('userInfo', JSON.stringify(response.data.user));
    }
    
    return response;
  } catch (error) {
    console.error('Registration error:', error);
    throw error;
  }
};

// Logout function
export const logout = () => {
  sessionStorage.removeItem('authToken');
  sessionStorage.removeItem('userInfo');
  
  // Sign out from Google as well
  if (window.google && window.google.accounts) {
    window.google.accounts.id.disableAutoSelect();
  }
  
  return Promise.resolve();
};

// Get current user info
export const getCurrentUser = () => {
  const userInfo = sessionStorage.getItem('userInfo');
  return userInfo ? JSON.parse(userInfo) : null;
};

// Check if user is authenticated
export const isAuthenticated = () => {
  const token = sessionStorage.getItem('authToken');
  const userInfo = sessionStorage.getItem('userInfo');
  return !!(token && userInfo);
};

// Get auth token
export const getAuthToken = () => {
  return sessionStorage.getItem('authToken');
};

// Refresh token function (if your backend supports it)
export const refreshToken = async () => {
  try {
    const response = await api.post('/refresh');
    
    if (response.data.token) {
      sessionStorage.setItem('authToken', response.data.token);
      return response.data.token;
    }
    
    return null;
  } catch (error) {
    console.error('Token refresh error:', error);
    logout(); // Force logout if refresh fails
    throw error;
  }
};

// Update user profile
export const updateProfile = async (profileData) => {
  try {
    const response = await api.put('/profile', profileData);
    
    if (response.data.user) {
      sessionStorage.setItem('userInfo', JSON.stringify(response.data.user));
    }
    
    return response;
  } catch (error) {
    console.error('Profile update error:', error);
    throw error;
  }
};

// Change password (for traditional accounts)
export const changePassword = async (passwordData) => {
  try {
    const response = await api.put('/change-password', passwordData);
    return response;
  } catch (error) {
    console.error('Password change error:', error);
    throw error;
  }
};

// Get user role
export const getUserRole = () => {
  const user = getCurrentUser();
  return user ? user.role : null;
};

// Check if user has specific role
export const hasRole = (role) => {
  const userRole = getUserRole();
  return userRole === role;
};

// Check if user is admin
export const isAdmin = () => {
  return hasRole('Admin');
};

// Check if user is approver
export const isApprover = () => {
  return hasRole('Approver');
};

// Check if user is student
export const isStudent = () => {
  return hasRole('Student');
};

// Notification API
const notificationApi = axios.create({
  baseURL: 'http://localhost:8080/api/notifications',
  headers: { 'Content-Type': 'application/json' }
});

notificationApi.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const fetchNotifications = async () => {
  const res = await notificationApi.get('/');
  return res.data;
};

export const fetchUnreadNotifications = async () => {
  const res = await notificationApi.get('/unread');
  return res.data;
};

export const fetchNotificationStats = async () => {
  const res = await notificationApi.get('/stats');
  return res.data;
};

export const markNotificationAsRead = async (id) => {
  const res = await notificationApi.patch(`/${id}/read`);
  return res.data;
};

export const markAllNotificationsAsRead = async () => {
  const res = await notificationApi.patch('/read-all');
  return res.data;
};

export const markNotificationAsArchived = async (id) => {
  const res = await notificationApi.patch(`/${id}/archive`);
  return res.data;
};

export const deleteNotification = async (id) => {
  const res = await notificationApi.delete(`/${id}`);
  return res.data;
};

export default api;