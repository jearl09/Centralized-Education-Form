import React, { createContext, useState, useEffect } from 'react';
import { login, logout, register } from './authService';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => sessionStorage.getItem('authToken'));
  const [user, setUser] = useState(() => {
    const userInfo = sessionStorage.getItem('userInfo');
    return userInfo ? JSON.parse(userInfo) : null;
  });

  useEffect(() => {
    if (token) sessionStorage.setItem('authToken', token);
    else sessionStorage.removeItem('authToken');
  }, [token]);

  useEffect(() => {
    if (user) sessionStorage.setItem('userInfo', JSON.stringify(user));
    else sessionStorage.removeItem('userInfo');
  }, [user]);

  const handleLogin = async (credentials) => {
    const response = await login(credentials);
    setToken(response.data.token);
    setUser(response.data.user);
    return response;
  };

  const handleLogout = () => {
    logout();
    setToken(null);
    setUser(null);
  };

  const handleRegister = async (userData) => {
    const response = await register(userData);
    setToken(response.data.token);
    setUser(response.data.user);
    return response;
  };

  return (
    <AuthContext.Provider value={{ token, user, handleLogin, handleLogout, handleRegister }}>
      {children}
    </AuthContext.Provider>
  );
}; 