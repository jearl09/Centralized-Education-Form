import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../styles/audit-trail.css';

const AuditTrail = () => {
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({
    action: '',
    entityType: '',
    startDate: '',
    endDate: '',
    user: ''
  });
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchAuditLogs();
  }, [currentPage, filters]);

  const fetchAuditLogs = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      let url = `/api/audit/logs?page=${currentPage}&size=20`;
      
      // Add filters to URL
      if (filters.action) url += `&action=${filters.action}`;
      if (filters.entityType) url += `&entityType=${filters.entityType}`;
      if (filters.startDate) url += `&startDate=${filters.startDate}`;
      if (filters.endDate) url += `&endDate=${filters.endDate}`;
      
      const response = await axios.get(url, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setAuditLogs(response.data.content || response.data);
      setTotalPages(response.data.totalPages || 0);
    } catch (err) {
      setError('Failed to load audit logs');
      console.error('Error fetching audit logs:', err);
    } finally {
      setLoading(false);
    }
  };

  const searchAuditLogs = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      let url = '/api/audit/search?';
      const params = new URLSearchParams();
      
      if (filters.action) params.append('action', filters.action);
      if (filters.entityType) params.append('entityType', filters.entityType);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      
      const response = await axios.get(`${url}${params.toString()}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setAuditLogs(response.data);
      setTotalPages(0); // Search results are not paginated
    } catch (err) {
      setError('Failed to search audit logs');
      console.error('Error searching audit logs:', err);
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    setFilters({
      action: '',
      entityType: '',
      startDate: '',
      endDate: '',
      user: ''
    });
    setSearchTerm('');
    setCurrentPage(0);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  const getActionIcon = (action) => {
    const icons = {
      'SUBMIT': '📝',
      'APPROVE': '✅',
      'REJECT': '❌',
      'LOGIN': '🔐',
      'LOGOUT': '🚪',
      'CREATE': '➕',
      'UPDATE': '✏️',
      'DELETE': '🗑️',
      'ROLE_CHANGE': '👤',
      'FILE_UPLOAD': '📎',
      'FILE_DELETE': '🗑️📎'
    };
    return icons[action] || '📋';
  };

  const getActionColor = (action) => {
    const colors = {
      'SUBMIT': '#007BFF',
      'APPROVE': '#28A745',
      'REJECT': '#DC3545',
      'LOGIN': '#17A2B8',
      'LOGOUT': '#6C757D',
      'CREATE': '#28A745',
      'UPDATE': '#FFC107',
      'DELETE': '#DC3545',
      'ROLE_CHANGE': '#6F42C1',
      'FILE_UPLOAD': '#20C997',
      'FILE_DELETE': '#DC3545'
    };
    return colors[action] || '#6C757D';
  };

  if (loading) {
    return (
      <div className="audit-trail">
        <div className="loading-spinner">Loading audit logs...</div>
      </div>
    );
  }

  return (
    <div className="audit-trail">
      <div className="audit-header">
        <h1>Audit Trail</h1>
        <p>Track all system activities and user actions</p>
      </div>

      {/* Filters Section */}
      <div className="filters-section">
        <div className="filters-grid">
          <div className="filter-group">
            <label>Action:</label>
            <select
              value={filters.action}
              onChange={(e) => setFilters({ ...filters, action: e.target.value })}
            >
              <option value="">All Actions</option>
              <option value="SUBMIT">Submit</option>
              <option value="APPROVE">Approve</option>
              <option value="REJECT">Reject</option>
              <option value="LOGIN">Login</option>
              <option value="LOGOUT">Logout</option>
              <option value="CREATE">Create</option>
              <option value="UPDATE">Update</option>
              <option value="DELETE">Delete</option>
              <option value="ROLE_CHANGE">Role Change</option>
              <option value="FILE_UPLOAD">File Upload</option>
              <option value="FILE_DELETE">File Delete</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Entity Type:</label>
            <select
              value={filters.entityType}
              onChange={(e) => setFilters({ ...filters, entityType: e.target.value })}
            >
              <option value="">All Entities</option>
              <option value="FORM">Form</option>
              <option value="USER">User</option>
              <option value="TEMPLATE">Template</option>
              <option value="FILE">File</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Start Date:</label>
            <input
              type="date"
              value={filters.startDate}
              onChange={(e) => setFilters({ ...filters, startDate: e.target.value })}
            />
          </div>

          <div className="filter-group">
            <label>End Date:</label>
            <input
              type="date"
              value={filters.endDate}
              onChange={(e) => setFilters({ ...filters, endDate: e.target.value })}
            />
          </div>
        </div>

        <div className="filter-actions">
          <button className="btn-search" onClick={searchAuditLogs}>
            🔍 Search
          </button>
          <button className="btn-clear" onClick={clearFilters}>
            🗑️ Clear Filters
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <div className="search-section">
        <input
          type="text"
          placeholder="Search in details, user, or IP address..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
      </div>

      {/* Error Message */}
      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {/* Audit Logs Table */}
      <div className="audit-table-container">
        <table className="audit-table">
          <thead>
            <tr>
              <th>Action</th>
              <th>User</th>
              <th>Entity</th>
              <th>Details</th>
              <th>IP Address</th>
              <th>Date & Time</th>
            </tr>
          </thead>
          <tbody>
            {auditLogs
              .filter(log => 
                !searchTerm || 
                log.details?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                log.user?.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                log.ipAddress?.includes(searchTerm)
              )
              .map((log) => (
                <tr key={log.id} className="audit-row">
                  <td>
                    <div className="action-cell">
                      <span 
                        className="action-icon"
                        style={{ backgroundColor: getActionColor(log.action) }}
                      >
                        {getActionIcon(log.action)}
                      </span>
                      <span className="action-text">{log.action}</span>
                    </div>
                  </td>
                  <td>
                    <div className="user-cell">
                      <span className="user-name">{log.user?.name || log.user?.email || 'Unknown'}</span>
                      <span className="user-email">{log.user?.email}</span>
                    </div>
                  </td>
                  <td>
                    <div className="entity-cell">
                      <span className="entity-type">{log.entityType}</span>
                      {log.entityId && (
                        <span className="entity-id">ID: {log.entityId}</span>
                      )}
                    </div>
                  </td>
                  <td>
                    <div className="details-cell">
                      <span className="details-text">{log.details}</span>
                    </div>
                  </td>
                  <td>
                    <span className="ip-address">{log.ipAddress || 'N/A'}</span>
                  </td>
                  <td>
                    <div className="date-cell">
                      <span className="date">{formatDate(log.createdAt)}</span>
                    </div>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>

        {auditLogs.length === 0 && !loading && (
          <div className="no-data">
            <p>No audit logs found</p>
            <p>Try adjusting your filters or search criteria</p>
          </div>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button
            className="btn-page"
            disabled={currentPage === 0}
            onClick={() => setCurrentPage(currentPage - 1)}
          >
            ← Previous
          </button>
          
          <span className="page-info">
            Page {currentPage + 1} of {totalPages}
          </span>
          
          <button
            className="btn-page"
            disabled={currentPage >= totalPages - 1}
            onClick={() => setCurrentPage(currentPage + 1)}
          >
            Next →
          </button>
        </div>
      )}

      {/* Export Section */}
      <div className="export-section">
        <h3>Export Options</h3>
        <div className="export-buttons">
          <button className="btn-export">
            📊 Export to CSV
          </button>
          <button className="btn-export">
            📋 Export to PDF
          </button>
          <button className="btn-export">
            📈 Generate Report
          </button>
        </div>
      </div>
    </div>
  );
};

export default AuditTrail; 