import React from 'react';
import NotificationSystem from './NotificationSystem';
import '../styles/dashboard.css';

const Header = ({ title = 'Dashboard', user }) => {
  return (
    <header className="dashboard-header">
      <div className="dashboard-title">{title}</div>
      <div className="dashboard-user-info">
        <NotificationSystem />
        <span className="user-avatar">{user?.avatar || '👤'}</span>
        <span className="user-name">{user?.name || user?.email?.split('@')[0] || 'User'}</span>
      </div>
    </header>
  );
};

export default Header;