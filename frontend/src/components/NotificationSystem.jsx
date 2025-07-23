import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/notification-system.css';
import { AuthContext } from '../services/authContext.jsx';
import {
  fetchNotifications,
  fetchUnreadNotifications,
  fetchNotificationStats,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  markNotificationAsArchived,
  deleteNotification
} from '../services/authService.js';

const NotificationSystem = () => {
  const { token } = useContext(AuthContext);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showDropdown, setShowDropdown] = useState(false);
  const [filter, setFilter] = useState('all');
  const [bellAnimate, setBellAnimate] = useState(false);
  const navigate = useNavigate();

  // Fetch notifications on mount and poll
  useEffect(() => {
    loadNotifications();
    const interval = setInterval(() => {
      loadNotifications(true);
    }, 30000);
    return () => clearInterval(interval);
  }, []);

  // Animate bell when new notifications arrive
  useEffect(() => {
    if (unreadCount > 0) {
      setBellAnimate(true);
      setTimeout(() => setBellAnimate(false), 1000);
    }
  }, [unreadCount]);

  const loadNotifications = async (silent = false) => {
    if (!silent) setIsLoading(true);
    try {
      const [notifs, stats] = await Promise.all([
        fetchNotifications(),
        fetchNotificationStats()
      ]);
      setNotifications(notifs);
      setUnreadCount(stats.unread);
    } catch (e) {
      setError('Failed to load notifications');
    } finally {
      setIsLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId) => {
    try {
      await markNotificationAsRead(notificationId);
      setNotifications(prev => prev.map(notif => notif.id === notificationId ? { ...notif, status: 'READ', readAt: new Date().toISOString() } : notif));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch {}
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllNotificationsAsRead();
      setNotifications(prev => prev.map(notif => ({ ...notif, status: 'READ', readAt: notif.readAt || new Date().toISOString() })));
      setUnreadCount(0);
    } catch {}
  };

  const handleArchive = async (notificationId) => {
    try {
      await markNotificationAsArchived(notificationId);
      setNotifications(prev => prev.map(notif => notif.id === notificationId ? { ...notif, status: 'ARCHIVED' } : notif));
    } catch {}
  };

  const handleDelete = async (notificationId) => {
    try {
      await deleteNotification(notificationId);
      setNotifications(prev => prev.filter(notif => notif.id !== notificationId));
    } catch {}
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'form_status': return '📋';
      case 'approval': return '✅';
      case 'rejection': return '❌';
      case 'system': return '🔔';
      default: return '📢';
    }
  };

  const getNotificationColor = (type) => {
    switch (type) {
      case 'form_status': return '#17a2b8';
      case 'approval': return '#28a745';
      case 'rejection': return '#dc3545';
      case 'system': return '#6c757d';
      default: return '#ffc107';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Just now';
    const date = new Date(dateString);
    const now = new Date();
    const diffInMinutes = Math.floor((now - date) / (1000 * 60));
    if (diffInMinutes < 1) return 'Just now';
    if (diffInMinutes < 60) return `${diffInMinutes}m ago`;
    if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}h ago`;
    return date.toLocaleDateString();
  };

  const filteredNotifications = notifications.filter(notif => {
    switch (filter) {
      case 'unread': return notif.status === 'UNREAD';
      case 'read': return notif.status === 'READ';
      case 'archived': return notif.status === 'ARCHIVED';
      default: return true;
    }
  });

  const handleNotificationClick = (notif) => {
    if (notif.status === 'UNREAD') handleMarkAsRead(notif.id);
    if (notif.actionUrl) navigate(notif.actionUrl);
  };

  if (isLoading) {
    return (
      <div className="notification-system">
        <div className="notification-bell">
          <span className="bell-icon">🔔</span>
          <div className="loading-spinner"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="notification-system">
      {/* Notification Bell Icon */}
      <div className="notification-bell-wrapper" style={{ position: 'relative', display: 'inline-block' }}>
        <button
          className={`notification-bell${bellAnimate ? ' animate' : ''}`}
          onClick={() => setShowDropdown(!showDropdown)}
          aria-label="Notifications"
          style={{ position: 'relative', zIndex: 20 }}
        >
          <span className="bell-icon">🔔</span>
          {unreadCount > 0 && (
            <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
          )}
        </button>

        {/* Notification Dropdown */}
        {showDropdown && (
          <div
            className="notification-dropdown"
            style={{
              position: 'absolute',
              top: 'calc(100% + 8px)',
              right: 0,
              minWidth: 340,
              maxWidth: 420,
              width: '90vw',
              background: '#fff',
              borderRadius: 12,
              boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
            }}
          >
            <div className="notification-header">
              <h3>Notifications</h3>
              <button className="mark-all-read" onClick={handleMarkAllAsRead} disabled={unreadCount === 0}>
                Mark all as read
              </button>
            </div>
            <div className="notification-filters">
              <button className={`filter-btn${filter === 'all' ? ' active' : ''}`} onClick={() => setFilter('all')}>All</button>
              <button className={`filter-btn${filter === 'unread' ? ' active' : ''}`} onClick={() => setFilter('unread')}>Unread</button>
              <button className={`filter-btn${filter === 'read' ? ' active' : ''}`} onClick={() => setFilter('read')}>Read</button>
              <button className={`filter-btn${filter === 'archived' ? ' active' : ''}`} onClick={() => setFilter('archived')}>Archived</button>
            </div>
            <div className="notification-list">
              {filteredNotifications.length === 0 ? (
                <div className="no-notifications">
                  <p>No notifications</p>
                </div>
              ) : (
                filteredNotifications.map(notif => (
                  <div
                    key={notif.id}
                    className={`notification-item ${notif.status.toLowerCase()}`}
                    onClick={() => handleNotificationClick(notif)}
                    style={{ borderLeft: `3px solid ${getNotificationColor(notif.type)}` }}
                  >
                    <span className="notification-icon">{getNotificationIcon(notif.type)}</span>
                    <div className="notification-content">
                      <h4>{notif.title}</h4>
                      <p>{notif.message}</p>
                      <span className="notification-time">{formatDate(notif.createdAt)}</span>
                    </div>
                    {notif.status === 'UNREAD' && <span className="unread-indicator"></span>}
                    <div className="notification-actions">
                      {notif.status !== 'READ' && notif.status !== 'ARCHIVED' && (
                        <button className="action-btn read-btn" onClick={e => { e.stopPropagation(); handleMarkAsRead(notif.id); }}>Mark as read</button>
                      )}
                      {notif.status !== 'ARCHIVED' && (
                        <button className="action-btn archive-btn" onClick={e => { e.stopPropagation(); handleArchive(notif.id); }}>Archive</button>
                      )}
                      <button className="action-btn delete-btn" onClick={e => { e.stopPropagation(); handleDelete(notif.id); }}>Delete</button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default NotificationSystem; 