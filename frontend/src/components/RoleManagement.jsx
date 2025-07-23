import React, { useState } from 'react';
import '../styles/role-management.css';

const RoleManagement = () => {
  const [activeTab, setActiveTab] = useState('roles');
  const [selectedRole, setSelectedRole] = useState(null);
  const [showAddRole, setShowAddRole] = useState(false);

  const roles = [
    {
      id: 1,
      name: 'Student',
      description: 'Can submit forms and view their own submissions',
      permissions: ['submit_forms', 'view_own_forms', 'track_status'],
      userCount: 1250
    },
    {
      id: 2,
      name: 'Department Head',
      description: 'Can approve forms within their department',
      permissions: ['approve_forms', 'view_department_forms', 'manage_templates'],
      userCount: 45
    },
    {
      id: 3,
      name: 'Academic Staff',
      description: 'Can review and process academic requests',
      permissions: ['review_forms', 'process_requests', 'generate_reports'],
      userCount: 120
    },
    {
      id: 4,
      name: 'Administrator',
      description: 'Full system access and management capabilities',
      permissions: ['all_permissions', 'manage_users', 'system_config'],
      userCount: 8
    }
  ];

  const users = [
    {
      id: 1,
      name: 'John Doe',
      email: 'john.doe@university.edu',
      role: 'Student',
      department: 'Computer Science',
      status: 'active',
      lastLogin: '2024-01-15 10:30'
    },
    {
      id: 2,
      name: 'Jane Smith',
      email: 'jane.smith@university.edu',
      role: 'Department Head',
      department: 'Information Technology',
      status: 'active',
      lastLogin: '2024-01-15 09:15'
    },
    {
      id: 3,
      name: 'Mike Johnson',
      email: 'mike.johnson@university.edu',
      role: 'Academic Staff',
      department: 'Engineering',
      status: 'inactive',
      lastLogin: '2024-01-10 14:20'
    }
  ];

  const permissions = [
    { id: 'submit_forms', name: 'Submit Forms', description: 'Can submit new forms' },
    { id: 'view_own_forms', name: 'View Own Forms', description: 'Can view their own form submissions' },
    { id: 'track_status', name: 'Track Status', description: 'Can track form status and history' },
    { id: 'approve_forms', name: 'Approve Forms', description: 'Can approve or reject forms' },
    { id: 'view_department_forms', name: 'View Department Forms', description: 'Can view forms from their department' },
    { id: 'manage_templates', name: 'Manage Templates', description: 'Can create and edit form templates' },
    { id: 'review_forms', name: 'Review Forms', description: 'Can review form submissions' },
    { id: 'process_requests', name: 'Process Requests', description: 'Can process academic requests' },
    { id: 'generate_reports', name: 'Generate Reports', description: 'Can generate system reports' },
    { id: 'manage_users', name: 'Manage Users', description: 'Can manage user accounts' },
    { id: 'system_config', name: 'System Configuration', description: 'Can configure system settings' }
  ];

  const handleRoleSelect = (role) => {
    setSelectedRole(role);
  };

  const handleAddRole = () => {
    setShowAddRole(true);
  };

  return (
    <div className="role-management">
      <div className="role-header">
        <h2>Role Management</h2>
        <p>Manage user roles, permissions, and access control</p>
      </div>

      <div className="role-tabs">
        <button 
          className={`tab-button ${activeTab === 'roles' ? 'active' : ''}`}
          onClick={() => setActiveTab('roles')}
        >
          Roles & Permissions
        </button>
        <button 
          className={`tab-button ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          User Management
        </button>
      </div>

      {activeTab === 'roles' && (
        <div className="roles-content">
          <div className="roles-overview">
            <div className="roles-stats">
              <div className="stat-card">
                <h3>Total Roles</h3>
                <p className="stat-number">{roles.length}</p>
              </div>
              <div className="stat-card">
                <h3>Total Users</h3>
                <p className="stat-number">{users.length}</p>
              </div>
              <div className="stat-card">
                <h3>Active Users</h3>
                <p className="stat-number">{users.filter(u => u.status === 'active').length}</p>
              </div>
            </div>
            <button className="btn-primary" onClick={handleAddRole}>
              + Add New Role
            </button>
          </div>

          <div className="roles-grid">
            {roles.map(role => (
              <div 
                key={role.id} 
                className={`role-card ${selectedRole?.id === role.id ? 'selected' : ''}`}
                onClick={() => handleRoleSelect(role)}
              >
                <div className="role-header">
                  <h3>{role.name}</h3>
                  <span className="user-count">{role.userCount} users</span>
                </div>
                <p className="role-description">{role.description}</p>
                <div className="role-permissions">
                  <h4>Permissions:</h4>
                  <div className="permissions-list">
                    {role.permissions.map(perm => (
                      <span key={perm} className="permission-tag">
                        {permissions.find(p => p.id === perm)?.name || perm}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="role-actions">
                  <button className="btn-secondary">Edit</button>
                  <button className="btn-secondary">Duplicate</button>
                </div>
              </div>
            ))}
          </div>

          {selectedRole && (
            <div className="role-details">
              <h3>Role Details: {selectedRole.name}</h3>
              <div className="details-grid">
                <div className="detail-section">
                  <h4>Basic Information</h4>
                  <p><strong>Name:</strong> {selectedRole.name}</p>
                  <p><strong>Description:</strong> {selectedRole.description}</p>
                  <p><strong>Users:</strong> {selectedRole.userCount}</p>
                </div>
                <div className="detail-section">
                  <h4>Permissions</h4>
                  <div className="permissions-detail">
                    {selectedRole.permissions.map(perm => {
                      const permission = permissions.find(p => p.id === perm);
                      return (
                        <div key={perm} className="permission-item">
                          <span className="permission-name">{permission?.name || perm}</span>
                          <span className="permission-desc">{permission?.description}</span>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'users' && (
        <div className="users-content">
          <div className="users-header">
            <div className="search-bar">
              <input type="text" placeholder="Search users..." />
              <button className="btn-secondary">Search</button>
            </div>
            <button className="btn-primary">+ Add User</button>
          </div>

          <div className="users-table">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Department</th>
                  <th>Status</th>
                  <th>Last Login</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.id}>
                    <td>{user.name}</td>
                    <td>{user.email}</td>
                    <td>
                      <span className={`role-badge ${user.role.toLowerCase().replace(' ', '-')}`}>
                        {user.role}
                      </span>
                    </td>
                    <td>{user.department}</td>
                    <td>
                      <span className={`status-badge ${user.status}`}>
                        {user.status}
                      </span>
                    </td>
                    <td>{user.lastLogin}</td>
                    <td>
                      <div className="user-actions">
                        <button className="btn-secondary">Edit</button>
                        <button className="btn-secondary">View</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default RoleManagement; 