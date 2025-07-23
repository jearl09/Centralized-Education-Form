import React, { useState, useEffect, useContext } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import FormSubmission from './FormSubmission';
import NotificationSystem from './NotificationSystem';
import '../styles/dashboard.css';
import { AuthContext } from '../services/authContext';
import Sidebar from './Sidebar';
import Header from './Header';
import WelcomeBanner from './WelcomeBanner';
import StatsCard from './StatsCard';
import QuickActionCard from './QuickActionCard';

const Dashboard = ({ onLogout }) => {
  const { token, user } = useContext(AuthContext);
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [forms, setForms] = useState([]);
  const [tracking, setTracking] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Get user info with fallbacks
  const currentUserRole = user?.role || 'student';
  const currentUserName = user?.name || user?.email?.split('@')[0] || 'Student';

  // Student-specific menu items only
  const studentMenuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: '🏠', path: '/student/dashboard' },
    { id: 'forms', label: 'My Forms', icon: '📝', path: '/student/forms' },
    { id: 'tracking', label: 'Track Status', icon: '📊', path: '/student/tracking' },
    { id: 'notifications', label: 'Notifications', icon: '🔔', path: '/student/notifications' },
    { id: 'profile', label: 'My Profile', icon: '👤', path: '/student/profile' }
  ];

  // Get current active tab from URL
  const getActiveTabFromPath = () => {
    const path = location.pathname;
    const menuItem = studentMenuItems.find(item => item.path === path);
    return menuItem ? menuItem.id : 'dashboard';
  };

  const [activeTab, setActiveTab] = useState(getActiveTabFromPath());

  // Update active tab when URL changes
  useEffect(() => {
    setActiveTab(getActiveTabFromPath());
  }, [location.pathname]);

  // Handle tab navigation
  const handleTabChange = (tabId) => {
    const menuItem = studentMenuItems.find(item => item.id === tabId);
    if (menuItem) {
      setActiveTab(tabId);
      navigate(menuItem.path);
    }
  };

  // Fetch data for each tab
  useEffect(() => {
    setError(null);
    if (activeTab === 'forms') {
      setLoading(true);
      fetch('/api/student/forms', { headers: { Authorization: `Bearer ${token}` } })
        .then(res => res.json())
        .then(data => { setForms(data); setLoading(false); })
        .catch(e => { setError('Failed to load forms'); setLoading(false); });
    } else if (activeTab === 'tracking') {
      setLoading(true);
      fetch('/api/student/forms/1/status', { headers: { Authorization: `Bearer ${token}` } })
        .then(res => res.json())
        .then(data => { setTracking(data); setLoading(false); })
        .catch(e => { setError('Failed to load tracking'); setLoading(false); });
    } else if (activeTab === 'notifications') {
      setLoading(true);
      fetch('/api/student/notifications', { headers: { Authorization: `Bearer ${token}` } })
        .then(res => res.json())
        .then(data => { setNotifications(data); setLoading(false); })
        .catch(e => { setError('Failed to load notifications'); setLoading(false); });
    }
  }, [activeTab, token]);

  // Add state for profile editing
  const [editProfile, setEditProfile] = useState(false);
  const [profileFields, setProfileFields] = useState({
    name: user?.name || '',
    email: user?.email || '',
    course: 'Bachelor of Science in Computer Science',
    yearLevel: '3rd Year',
    department: 'College of Computer Studies',
  });
  const [profileMsg, setProfileMsg] = useState(null);
  const [profileLoading, setProfileLoading] = useState(false);

  // Handle profile field changes
  const handleProfileChange = (e) => {
    setProfileFields({ ...profileFields, [e.target.name]: e.target.value });
  };

  // Save profile changes
  const handleProfileSave = async () => {
    setProfileLoading(true);
    setProfileMsg(null);
    try {
      const res = await fetch('/api/student/profile', {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(profileFields),
      });
      if (!res.ok) throw new Error('Failed to update profile');
      setEditProfile(false);
      setProfileMsg('Profile updated successfully!');
      // Optionally update user context here if needed
    } catch (e) {
      setProfileMsg('Failed to update profile.');
    } finally {
      setProfileLoading(false);
      setTimeout(() => setProfileMsg(null), 2500);
    }
  };

  // Cancel editing
  const handleProfileCancel = () => {
    setEditProfile(false);
    setProfileFields({
      name: user?.name || '',
      email: user?.email || '',
      course: 'Bachelor of Science in Computer Science',
      yearLevel: '3rd Year',
      department: 'College of Computer Studies',
    });
    setProfileMsg(null);
  };

  // Add this useEffect to fetch profile data when profile tab is active
  useEffect(() => {
    if (activeTab === 'profile') {
      setProfileLoading(true);
      fetch('/api/student/profile', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
        .then(res => res.json())
        .then(data => {
          setProfileFields({
            name: data.username || data.name || '',
            email: data.email || '',
            course: data.course || '',
            yearLevel: data.yearLevel || '',
            department: data.department || '',
          });
          setProfileLoading(false);
        })
        .catch(() => {
          setProfileLoading(false);
        });
    }
  }, [activeTab, token]);

  const renderContent = () => {
    if (loading) return <div>Loading...</div>;
    if (error) return <div style={{ color: 'red' }}>{error}</div>;
    switch (activeTab) {
      case 'forms':
        return (
          <FormSubmission />
        );
      case 'tracking':
        return (
          <div className="dashboard-content">
            <h2>Track Status</h2>
            {Array.isArray(tracking) && tracking.length === 0 ? (
              <div>No tracking info available.</div>
            ) : (
              <div className="tracking-cards">
                {Array.isArray(tracking) ? tracking.map((item, idx) => (
                  <div className="tracking-card" key={idx}>
                    <h3>Form #{item.id}</h3>
                    <p>Status: <span className={`status-badge ${item.status}`}>{item.status}</span></p>
                    <p>Current Step: {item.currentStep} / {item.totalSteps}</p>
                    <p>Last Updated: {item.updatedAt || '-'}</p>
                  </div>
                )) : <div>{JSON.stringify(tracking)}</div>}
              </div>
            )}
          </div>
        );
      case 'notifications':
        return (
          <div className="dashboard-content">
            <h2>Notifications</h2>
            {notifications.length === 0 ? (
              <div>No notifications.</div>
            ) : (
              <table className="red-gold-table">
                <thead>
                  <tr>
                    <th>Message</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {notifications.map((notif, idx) => (
                    <tr key={idx}>
                      <td>{notif.message}</td>
                      <td>{notif.date}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        );
      case 'profile':
        return (
          <div className="dashboard-content">
            <div className="section-header">
              <h2>My Profile</h2>
              <p>Manage your personal information and account settings</p>
            </div>
            <div className="profile-section">
              <div className="profile-card">
                <div className="profile-header">
                  <div className="profile-avatar">👤</div>
                  <div className="profile-info">
                    {editProfile ? (
                      <input
                        type="text"
                        name="name"
                        value={profileFields.name}
                        onChange={handleProfileChange}
                        className="profile-input"
                        placeholder="Name"
                      />
                    ) : (
                      <h3>{profileFields.name}</h3>
                    )}
                    <p className="user-role">Student</p>
                    <p className="user-id">Student ID: 2024-0001</p>
                  </div>
                </div>
                <div className="profile-details">
                  <div className="detail-group">
                    <label>Email</label>
                    {editProfile ? (
                      <input
                        type="email"
                        name="email"
                        value={profileFields.email}
                        onChange={handleProfileChange}
                        className="profile-input"
                        placeholder="Email"
                      />
                    ) : (
                      <p>{profileFields.email}</p>
                    )}
                  </div>
                  <div className="detail-group">
                    <label>Course</label>
                    {editProfile ? (
                      <input
                        type="text"
                        name="course"
                        value={profileFields.course}
                        onChange={handleProfileChange}
                        className="profile-input"
                        placeholder="Course"
                      />
                    ) : (
                      <p>{profileFields.course}</p>
                    )}
                  </div>
                  <div className="detail-group">
                    <label>Year Level</label>
                    {editProfile ? (
                      <input
                        type="text"
                        name="yearLevel"
                        value={profileFields.yearLevel}
                        onChange={handleProfileChange}
                        className="profile-input"
                        placeholder="Year Level"
                      />
                    ) : (
                      <p>{profileFields.yearLevel}</p>
                    )}
                  </div>
                  <div className="detail-group">
                    <label>Department</label>
                    {editProfile ? (
                      <input
                        type="text"
                        name="department"
                        value={profileFields.department}
                        onChange={handleProfileChange}
                        className="profile-input"
                        placeholder="Department"
                      />
                    ) : (
                      <p>{profileFields.department}</p>
                    )}
                  </div>
                </div>
                <div className="profile-actions">
                  {editProfile ? (
                    <>
                      <button className="btn btn-primary" onClick={handleProfileSave} disabled={profileLoading}>
                        {profileLoading ? 'Saving...' : 'Save'}
                      </button>
                      <button className="btn btn-secondary" onClick={handleProfileCancel} disabled={profileLoading}>
                        Cancel
                      </button>
                    </>
                  ) : (
                    <button className="btn btn-primary" onClick={() => setEditProfile(true)}>
                      Edit Profile
                    </button>
                  )}
                  <button className="btn btn-outline" onClick={onLogout}>Logout</button>
                </div>
                {profileMsg && (
                  <div style={{ marginTop: 10, color: profileMsg.includes('success') ? 'green' : 'red' }}>
                    {profileMsg}
                  </div>
                )}
              </div>
            </div>
          </div>
        );
      case 'dashboard':
      default:
        return (
          <div className="dashboard-content">
            <WelcomeBanner
              name={currentUserName}
              subtitle="Track your academic forms and stay updated with your submissions."
            />
            <div className="stats-grid">
              <StatsCard icon="📝" value={3} label="Forms Submitted" />
              <StatsCard icon="⏳" value={1} label="Pending Review" />
              <StatsCard icon="✅" value={1} label="Approved" />
              <StatsCard icon="❌" value={1} label="Rejected" />
            </div>
            <div className="quick-actions">
              <h2>Quick Actions</h2>
              <div className="quick-actions-grid">
                <QuickActionCard
                  icon="📝"
                  label="Submit New Form"
                  description="Create and submit a new academic form"
                  onClick={() => handleTabChange('forms')}
                />
                <QuickActionCard
                  icon="📊"
                  label="Track My Forms"
                  description="Monitor the status of your submissions"
                  onClick={() => handleTabChange('tracking')}
                />
                <QuickActionCard
                  icon="🔔"
                  label="View Notifications"
                  description="Check your latest updates and alerts"
                  onClick={() => handleTabChange('notifications')}
                />
                <QuickActionCard
                  icon="👤"
                  label="My Profile"
                  description="Update your personal information"
                  onClick={() => handleTabChange('profile')}
                />
              </div>
            </div>
          </div>
        );
    }
  };

  return (
    <div className="dashboard">
      <Sidebar
        menuItems={studentMenuItems}
        activeTab={activeTab}
        onTabChange={handleTabChange}
        onLogout={onLogout}
        header="🎓 Student Portal"
      />
      <div className="main-content">
        <Header
          title={studentMenuItems.find(item => item.id === activeTab)?.label || 'Dashboard'}
          user={user}
        />
        <main className="content-area">
          {renderContent()}
        </main>
      </div>
    </div>
  );
};

export default Dashboard; 