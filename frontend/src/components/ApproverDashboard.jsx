import React, { useEffect, useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../services/authContext';
import Sidebar from './Sidebar';
import Header from './Header';
import '../styles/dashboard.css';

const ApproverDashboard = () => {
  const { token, user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [pendingForms, setPendingForms] = useState([]);
  const [selectedForm, setSelectedForm] = useState(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState(null);
  const [actionMsg, setActionMsg] = useState(null);

  useEffect(() => {
    fetchPendingForms();
  }, []);

  const fetchPendingForms = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch('/api/forms/pending', {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Failed to fetch pending forms');
      setPendingForms(await res.json());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (formId) => {
    setActionLoading(true);
    setActionMsg(null);
    try {
      const res = await fetch(`/api/forms/${formId}/approve`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ comments: 'Form approved' }),
      });
      if (!res.ok) throw new Error('Failed to approve form');
      setActionMsg('Form approved!');
      fetchPendingForms();
      setSelectedForm(null);
    } catch (e) {
      setActionMsg('Error: ' + e.message);
    } finally {
      setActionLoading(false);
      setTimeout(() => setActionMsg(null), 2500);
    }
  };

  const handleReject = async (formId) => {
    setActionLoading(true);
    setActionMsg(null);
    try {
      const res = await fetch(`/api/forms/${formId}/reject`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ comments: 'Form rejected' }),
      });
      if (!res.ok) throw new Error('Failed to reject form');
      setActionMsg('Form rejected!');
      fetchPendingForms();
      setSelectedForm(null);
    } catch (e) {
      setActionMsg('Error: ' + e.message);
    } finally {
      setActionLoading(false);
      setTimeout(() => setActionMsg(null), 2500);
    }
  };

  const { handleLogout: authLogout } = useContext(AuthContext);
  
  const handleLogout = () => {
    authLogout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="dashboard-layout">
      <Sidebar
        menuItems={[
          { id: 'dashboard', label: 'Dashboard', icon: '🏠', path: '/approver/dashboard' },
          { id: 'pending', label: 'Pending Forms', icon: '📝', path: '/approver/pending' },
        ]}
        activeTab={'pending'}
        onTabChange={() => {}}
        onLogout={handleLogout}
        header="Approver Portal"
      />
      <div className="dashboard-main">
        <Header title="Approver Dashboard" user={user} />
        <div className="dashboard-content">
          <div className="section-header" style={{ marginBottom: 0 }}>
            <h2>Pending Forms</h2>
          </div>
          <div className="section-card">
            {loading ? (
              <div>Loading...</div>
            ) : error ? (
              <div style={{ color: 'red' }}>{error}</div>
            ) : pendingForms.length === 0 ? (
              <div>No pending forms.</div>
            ) : (
              <table className="red-gold-table" style={{ width: '100%' }}>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Submitted</th>
                    <th>Student</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingForms.map(form => (
                    <tr key={form.id}>
                      <td>{form.id}</td>
                      <td>{form.type}</td>
                      <td>{form.status}</td>
                      <td>{form.submittedDate ? new Date(form.submittedDate).toLocaleString() : ''}</td>
                      <td>{form.studentName}</td>
                      <td>
                        <button className="btn btn-primary" onClick={() => setSelectedForm(form)}>
                          View
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {/* Modal for form details and actions */}
          {selectedForm && (
            <div className="modal-backdrop">
              <div className="modal-content" style={{ maxWidth: 500 }}>
                <h3>Form Details</h3>
                <p><b>ID:</b> {selectedForm.id}</p>
                <p><b>Type:</b> {selectedForm.type}</p>
                <p><b>Status:</b> {selectedForm.status}</p>
                <p><b>Student:</b> {selectedForm.studentName}</p>
                <p><b>Submitted:</b> {selectedForm.submittedDate ? new Date(selectedForm.submittedDate).toLocaleString() : ''}</p>
                <p><b>Current Step:</b> {selectedForm.currentStep} / {selectedForm.totalSteps}</p>
                {actionMsg && <div style={{ margin: '10px 0', color: actionMsg.includes('Error') ? 'red' : 'green' }}>{actionMsg}</div>}
                <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
                  <button className="btn btn-primary" onClick={() => handleApprove(selectedForm.id)} disabled={actionLoading}>
                    {actionLoading ? 'Processing...' : 'Approve'}
                  </button>
                  <button className="btn btn-secondary" onClick={() => handleReject(selectedForm.id)} disabled={actionLoading}>
                    {actionLoading ? 'Processing...' : 'Reject'}
                  </button>
                  <button className="btn btn-outline" onClick={() => setSelectedForm(null)} disabled={actionLoading}>
                    Close
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ApproverDashboard; 