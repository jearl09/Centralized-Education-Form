import React, { useEffect, useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../services/authContext';
import Sidebar from './Sidebar';
import Header from './Header';
import '../styles/dashboard.css';

const AdminDashboard = () => {
  const { token, user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [msg, setMsg] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newUser, setNewUser] = useState({
    username: '',
    email: '',
    role: '',
    department: '',
    course: '',
    yearLevel: ''
  });
  const [showEditModal, setShowEditModal] = useState(false);
  const [editUser, setEditUser] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);

  useEffect(() => {
    fetchUsers();
    fetchRoles();
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch('/api/admin/users', {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Failed to fetch users');
      setUsers(await res.json());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchRoles = async () => {
    try {
      const res = await fetch('/api/admin/roles', {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Failed to fetch roles');
      setRoles(await res.json());
    } catch (e) {
      setRoles(['Student', 'Approver', 'Admin']);
    }
  };

  const handleRoleChange = async (userId, newRole) => {
    setMsg(null);
    setError(null);
    try {
      const res = await fetch('/api/admin/roles/assign', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ userId, role: newRole }),
      });
      if (!res.ok) throw new Error('Failed to update role');
      setMsg('Role updated!');
      fetchUsers();
    } catch (e) {
      setError('Error: ' + e.message);
    } finally {
      setTimeout(() => setMsg(null), 2000);
    }
  };

  const { handleLogout: authLogout } = useContext(AuthContext);
  
  const handleLogout = () => {
    authLogout();
    navigate('/login', { replace: true });
  };

  // Modal form handlers
  const handleNewUserChange = (e) => {
    const { name, value } = e.target;
    setNewUser((prev) => ({ ...prev, [name]: value }));
  };

  const handleOpenCreateModal = () => {
    setNewUser({ username: '', email: '', role: roles[0] || '', department: '', course: '', yearLevel: '' });
    setShowCreateModal(true);
  };

  const handleCloseCreateModal = () => {
    setShowCreateModal(false);
  };

  // Edit modal handlers
  const handleOpenEditModal = (user) => {
    setEditUser({ ...user });
    setShowEditModal(true);
  };
  const handleEditUserChange = (e) => {
    const { name, value } = e.target;
    setEditUser((prev) => ({ ...prev, [name]: value }));
  };
  const handleCloseEditModal = () => {
    setShowEditModal(false);
    setEditUser(null);
  };

  // Delete confirm handlers
  const handleOpenDeleteConfirm = (user) => {
    setUserToDelete(user);
    setShowDeleteConfirm(true);
  };
  const handleCloseDeleteConfirm = () => {
    setShowDeleteConfirm(false);
    setUserToDelete(null);
  };

  // Create user handler
  const handleCreateUser = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch('/api/admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(newUser),
      });
      if (!res.ok) throw new Error('Failed to create user');
      setMsg('User created!');
      setShowCreateModal(false);
      fetchUsers();
    } catch (e) {
      setError('Error: ' + e.message);
    } finally {
      setLoading(false);
      setTimeout(() => setMsg(null), 2000);
    }
  };

  // Edit user handler
  const handleEditUser = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/admin/users/${editUser.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(editUser),
      });
      if (!res.ok) throw new Error('Failed to update user');
      setMsg('User updated!');
      setShowEditModal(false);
      fetchUsers();
    } catch (e) {
      setError('Error: ' + e.message);
    } finally {
      setLoading(false);
      setTimeout(() => setMsg(null), 2000);
    }
  };

  // Delete user handler
  const handleDeleteUser = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/admin/users/${userToDelete.id}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (!res.ok) throw new Error('Failed to delete user');
      setMsg('User deleted!');
      setShowDeleteConfirm(false);
      fetchUsers();
    } catch (e) {
      setError('Error: ' + e.message);
    } finally {
      setLoading(false);
      setTimeout(() => setMsg(null), 2000);
    }
  };

  return (
    <div className="dashboard" style={{ minHeight: '100vh', display: 'flex', background: '#f8f9fa' }}>
      <Sidebar
        menuItems={[
          { id: 'dashboard', label: 'Dashboard', icon: '🏠', path: '/admin/dashboard' },
          { id: 'users', label: 'User Management', icon: '👥', path: '/admin/users' },
          { id: 'reports', label: 'Reports & Analytics', icon: '📊', path: '/admin/reports' },
          { id: 'audit', label: 'Audit Trail', icon: '📋', path: '/admin/audit-trail' },
        ]}
        activeTab={'users'}
        onTabChange={() => {}}
        onLogout={handleLogout}
        header="Admin Portal"
      />
      <div className="main-content" style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <Header title="Admin Dashboard" user={user} />
        <main className="content-area" style={{ flex: 1, padding: '24px 0 0 0' }}>
          <div className="dashboard-content">
            <div className="section-header" style={{ marginBottom: 0 }}>
              <h2>User & Role Management</h2>
            </div>
            {/* Create User Button */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
              <button className="btn btn-primary" onClick={handleOpenCreateModal}>
                + Create User
              </button>
            </div>
            {/* Create User Modal */}
            {showCreateModal && (
              <div className="modal-overlay">
                <div className="modal-card">
                  <h3>Create New User</h3>
                  <form onSubmit={handleCreateUser}>
                    <div className="form-group">
                      <label>Name</label>
                      <input name="username" value={newUser.username} onChange={handleNewUserChange} required />
                    </div>
                    <div className="form-group">
                      <label>Email</label>
                      <input name="email" type="email" value={newUser.email} onChange={handleNewUserChange} required />
                    </div>
                    <div className="form-group">
                      <label>Role</label>
                      <select name="role" value={newUser.role} onChange={handleNewUserChange} required>
                        {roles.map(r => <option key={r} value={r}>{r}</option>)}
                      </select>
                    </div>
                    <div className="form-group">
                      <label>Department</label>
                      <input name="department" value={newUser.department} onChange={handleNewUserChange} />
                    </div>
                    <div className="form-group">
                      <label>Course</label>
                      <input name="course" value={newUser.course} onChange={handleNewUserChange} />
                    </div>
                    <div className="form-group">
                      <label>Year</label>
                      <input name="yearLevel" value={newUser.yearLevel} onChange={handleNewUserChange} />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 16 }}>
                      <button type="button" className="btn btn-secondary" onClick={handleCloseCreateModal}>Cancel</button>
                      <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Creating...' : 'Create'}</button>
                    </div>
                  </form>
                </div>
              </div>
            )}
            {/* End Create User Modal */}
            <div className="section-card">
              {loading ? (
                <div>Loading...</div>
              ) : error ? (
                <div style={{ color: 'red' }}>{error}</div>
              ) : (
                <table className="red-gold-table" style={{ width: '100%' }}>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Department</th>
                      <th>Course</th>
                      <th>Year</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map(u => (
                      <tr key={u.id}>
                        <td>{u.id}</td>
                        <td>{u.username || '-'}</td>
                        <td>{u.email}</td>
                        <td>
                          <select
                            value={u.role}
                            onChange={e => handleRoleChange(u.id, e.target.value)}
                            style={{ padding: '4px 8px', borderRadius: 6 }}
                          >
                            {roles.map(r => (
                              <option key={r} value={r}>{r}</option>
                            ))}
                          </select>
                        </td>
                        <td>{u.department || '-'}</td>
                        <td>{u.course || '-'}</td>
                        <td>{u.yearLevel || '-'}</td>
                        <td>
                          <button className="btn btn-outline" style={{ marginRight: 8 }} onClick={() => handleOpenEditModal(u)}>Edit</button>
                          <button className="btn btn-warning" onClick={() => handleOpenDeleteConfirm(u)}>Delete</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
              {msg && <div style={{ color: 'green', marginTop: 10 }}>{msg}</div>}
            </div>
            {/* Edit User Modal */}
            {showEditModal && editUser && (
              <div className="modal-overlay">
                <div className="modal-card">
                  <h3>Edit User</h3>
                  <form onSubmit={handleEditUser}>
                    <div className="form-group">
                      <label>Name</label>
                      <input name="username" value={editUser.username} onChange={handleEditUserChange} required />
                    </div>
                    <div className="form-group">
                      <label>Email</label>
                      <input name="email" type="email" value={editUser.email} onChange={handleEditUserChange} required />
                    </div>
                    <div className="form-group">
                      <label>Role</label>
                      <select name="role" value={editUser.role} onChange={handleEditUserChange} required>
                        {roles.map(r => <option key={r} value={r}>{r}</option>)}
                      </select>
                    </div>
                    <div className="form-group">
                      <label>Department</label>
                      <input name="department" value={editUser.department} onChange={handleEditUserChange} />
                    </div>
                    <div className="form-group">
                      <label>Course</label>
                      <input name="course" value={editUser.course} onChange={handleEditUserChange} />
                    </div>
                    <div className="form-group">
                      <label>Year</label>
                      <input name="yearLevel" value={editUser.yearLevel} onChange={handleEditUserChange} />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 16 }}>
                      <button type="button" className="btn btn-secondary" onClick={handleCloseEditModal}>Cancel</button>
                      <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Saving...' : 'Save'}</button>
                    </div>
                  </form>
                </div>
              </div>
            )}
            {/* End Edit User Modal */}
            {/* Delete Confirm Modal */}
            {showDeleteConfirm && userToDelete && (
              <div className="modal-overlay">
                <div className="modal-card">
                  <h3>Delete User</h3>
                  <p>Are you sure you want to delete <b>{userToDelete.username || userToDelete.email}</b>?</p>
                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 16 }}>
                    <button className="btn btn-secondary" onClick={handleCloseDeleteConfirm}>Cancel</button>
                    <button className="btn btn-warning" onClick={handleDeleteUser} disabled={loading}>{loading ? 'Deleting...' : 'Delete'}</button>
                  </div>
                </div>
              </div>
            )}
            {/* End Delete Confirm Modal */}
          </div>
        </main>
      </div>
    </div>
  );
};

export default AdminDashboard; 