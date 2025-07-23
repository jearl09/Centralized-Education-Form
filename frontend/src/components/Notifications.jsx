import React, { useState, useEffect, useContext } from 'react';
import '../styles/notification-system.css';
import { AuthContext } from '../services/authContext.jsx';
import { useNavigate } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';

const Notifications = () => {
  const { token } = useContext(AuthContext);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all');
  const navigate = useNavigate();

  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();
  }, []);

  const fetchNotifications = async () => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/student/notifications', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        setNotifications(await response.json());
      }
    } catch (error) {
      setError('Failed to load notifications');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const response = await fetch('/api/student/notifications/unread-count', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        setUnreadCount((await response.json()).unreadCount);
      }
    } catch {}
  };

  const markAsRead = async (notificationId) => {
    try {
      const response = await fetch(`/api/student/notifications/${notificationId}/read`, {
        method: 'PATCH',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        setNotifications(prev => prev.map(notif => notif.id === notificationId ? { ...notif, status: 'READ', readAt: new Date().toISOString() } : notif));
        setUnreadCount(prev => Math.max(0, prev - 1));
      }
    } catch {}
  };

  const markAllAsRead = async () => {
    try {
      const response = await fetch('/api/student/notifications/mark-all-read', {
        method: 'PATCH',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        setNotifications(prev => prev.map(notif => ({ ...notif, status: 'READ', readAt: notif.readAt || new Date().toISOString() })));
        setUnreadCount(0);
      }
    } catch {}
  };

  const archiveNotification = async (notificationId) => {
    try {
      const response = await fetch(`/api/student/notifications/${notificationId}/archive`, {
        method: 'PATCH',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        setNotifications(prev => prev.map(notif => notif.id === notificationId ? { ...notif, status: 'ARCHIVED' } : notif));
      }
    } catch {}
  };

  const deleteNotification = async (notificationId) => {
    try {
      const response = await fetch(`/api/student/notifications/${notificationId}/delete`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        setNotifications(prev => prev.filter(notif => notif.id !== notificationId));
      }
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
    if (notif.status === 'UNREAD') markAsRead(notif.id);
    if (notif.actionUrl) navigate(notif.actionUrl);
  };

  return (
    <div className="dashboard-layout">
      <Sidebar
        menuItems={[
          { id: 'dashboard', label: 'Dashboard', icon: '🏠', path: '/student/dashboard' },
          { id: 'forms', label: 'My Forms', icon: '📝', path: '/student/forms' },
          { id: 'tracking', label: 'Track Status', icon: '📊', path: '/student/tracking' },
          { id: 'notifications', label: 'Notifications', icon: '🔔', path: '/student/notifications' },
          { id: 'profile', label: 'My Profile', icon: '👤', path: '/student/profile' }
        ]}
        activeTab={'notifications'}
        onTabChange={tabId => navigate(`/student/${tabId}`)}
        onLogout={() => { localStorage.removeItem('user'); navigate('/login'); }}
        header="Student Portal"
      />
      <div className="dashboard-main">
        <Header title="Notifications" user={null} />
        <div className="dashboard-content" style={{ marginTop: '2rem' }}>
          <div className="section-card">
            <div className="section-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 0 }}>
              <h2 style={{ margin: 0 }}>Notifications</h2>
              <button className="mark-all-read-btn" onClick={markAllAsRead} disabled={unreadCount === 0}>Mark all as read</button>
            </div>
            <div className="tabs-container" style={{ margin: '1.5rem 0 1rem 0', display: 'flex', gap: '1rem' }}>
              <button className={`filter-btn ${filter === 'all' ? 'active' : ''}`} onClick={() => setFilter('all')}>All</button>
              <button className={`filter-btn ${filter === 'unread' ? 'active' : ''}`} onClick={() => setFilter('unread')}>Unread ({unreadCount})</button>
              <button className={`filter-btn ${filter === 'read' ? 'active' : ''}`} onClick={() => setFilter('read')}>Read</button>
              <button className={`filter-btn ${filter === 'archived' ? 'active' : ''}`} onClick={() => setFilter('archived')}>Archived</button>
            </div>
            <div className="notifications-list full-page">
              {isLoading ? (
                <div className="loading-spinner"></div>
              ) : error ? (
                <div className="error-message">{error}</div>
              ) : filteredNotifications.length === 0 ? (
                <div className="no-notifications">
                  <span className="no-notifications-icon">📭</span>
                  <p>No notifications</p>
                </div>
              ) : (
                filteredNotifications.map(notification => (
                  <div
                    key={notification.id}
                    className={`notification-item ${notification.status.toLowerCase()}${notification.status === 'UNREAD' ? ' unread-highlight' : ''}`}
                    onClick={() => handleNotificationClick(notification)}
                    tabIndex={0}
                    role="button"
                    onKeyDown={e => { if (e.key === 'Enter') handleNotificationClick(notification); }}
                    style={{
                      boxShadow: '0 2px 8px rgba(128,0,32,0.07)',
                      borderRadius: 12,
                      marginBottom: 16,
                      background: notification.status === 'UNREAD' ? '#fff8f0' : '#fff',
                      borderLeft: notification.status === 'UNREAD' ? '4px solid #a8071a' : '4px solid transparent',
                      display: 'flex',
                      alignItems: 'flex-start',
                      padding: '18px 22px',
                      transition: 'background 0.2s, box-shadow 0.2s',
                      position: 'relative',
                      opacity: notification.status === 'ARCHIVED' ? 0.6 : 1
                    }}
                  >
                    <div className="notification-icon" style={{ backgroundColor: getNotificationColor(notification.type), color: '#fff', borderRadius: '50%', width: 40, height: 40, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem', marginRight: 18 }}>
                      {getNotificationIcon(notification.type)}
                    </div>
                    <div className="notification-content" style={{ flex: 1 }}>
                      <div className="notification-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h4 className="notification-title" style={{ fontWeight: 600, fontSize: '1.1rem', margin: 0 }}>{notification.title}</h4>
                        <span className="notification-time" style={{ fontSize: '0.9rem', color: '#888', marginLeft: 12 }}>{formatDate(notification.createdAt)}</span>
                      </div>
                      <p className="notification-message" style={{ color: '#444', fontSize: '1rem', margin: '6px 0 0 0' }}>{notification.message}</p>
                      {notification.relatedFormId && (
                        <div className="notification-action" style={{ marginTop: 6 }}>
                          <span className="form-link" style={{ color: '#a8071a', fontWeight: 500 }}>Form ID: #{notification.relatedFormId}</span>
                        </div>
                      )}
                    </div>
                    <div className="notification-actions" style={{ display: 'flex', flexDirection: 'column', gap: 6, marginLeft: 16 }}>
                      {notification.status === 'UNREAD' && (
                        <button className="action-btn read-btn" style={{ background: '#a8071a', color: '#fff', borderRadius: 6, border: 'none', padding: '4px 8px', fontWeight: 600, cursor: 'pointer' }} onClick={e => { e.stopPropagation(); markAsRead(notification.id); }} title="Mark as read">✓</button>
                      )}
                      <button className="action-btn archive-btn" style={{ background: '#ffe066', color: '#a8071a', borderRadius: 6, border: 'none', padding: '4px 8px', fontWeight: 600, cursor: 'pointer' }} onClick={e => { e.stopPropagation(); archiveNotification(notification.id); }} title="Archive">📁</button>
                      <button className="action-btn delete-btn" style={{ background: '#fff', color: '#a8071a', border: '1px solid #a8071a', borderRadius: 6, padding: '4px 8px', fontWeight: 600, cursor: 'pointer' }} onClick={e => { e.stopPropagation(); deleteNotification(notification.id); }} title="Delete">🗑️</button>
                    </div>
                    {notification.status === 'UNREAD' && <span className="dot" style={{ position: 'absolute', top: 18, right: 18, width: 10, height: 10, background: '#ffd700', borderRadius: '50%' }}></span>}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Notifications; 