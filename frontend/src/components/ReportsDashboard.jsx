import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../styles/reports-dashboard.css';

const ReportsDashboard = () => {
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [dateRange, setDateRange] = useState({
    startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0]
  });

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const response = await axios.get('/api/reports/dashboard', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setDashboardData(response.data);
    } catch (err) {
      setError('Failed to load dashboard data');
      console.error('Error fetching dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchDateRangeData = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`/api/reports/form-stats-by-date?startDate=${dateRange.startDate}&endDate=${dateRange.endDate}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      return response.data;
    } catch (err) {
      console.error('Error fetching date range data:', err);
      return null;
    }
  };

  if (loading) {
    return (
      <div className="reports-dashboard">
        <div className="loading-spinner">Loading dashboard data...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="reports-dashboard">
        <div className="error-message">{error}</div>
      </div>
    );
  }

  if (!dashboardData) {
    return (
      <div className="reports-dashboard">
        <div className="error-message">No data available</div>
      </div>
    );
  }

  const { systemStats, formStatsByType, userActivity, approvalPerformance, monthlyTrends } = dashboardData;

  return (
    <div className="reports-dashboard">
      <div className="dashboard-header">
        <h1>Reports & Analytics Dashboard</h1>
        <div className="date-range-selector">
          <label>Date Range:</label>
          <input
            type="date"
            value={dateRange.startDate}
            onChange={(e) => setDateRange({ ...dateRange, startDate: e.target.value })}
          />
          <span>to</span>
          <input
            type="date"
            value={dateRange.endDate}
            onChange={(e) => setDateRange({ ...dateRange, endDate: e.target.value })}
          />
          <button onClick={fetchDateRangeData}>Apply</button>
        </div>
      </div>

      {/* System Overview Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Forms</h3>
          <div className="stat-number">{systemStats.forms?.total || 0}</div>
          <div className="stat-breakdown">
            <span className="pending">{systemStats.forms?.pending || 0} Pending</span>
            <span className="approved">{systemStats.forms?.approved || 0} Approved</span>
            <span className="rejected">{systemStats.forms?.rejected || 0} Rejected</span>
          </div>
        </div>

        <div className="stat-card">
          <h3>Users</h3>
          <div className="stat-number">{systemStats.users?.total || 0}</div>
          <div className="stat-breakdown">
            <span className="students">{systemStats.users?.students || 0} Students</span>
            <span className="approvers">{systemStats.users?.approvers || 0} Approvers</span>
            <span className="admins">{systemStats.users?.admins || 0} Admins</span>
          </div>
        </div>

        <div className="stat-card">
          <h3>Approval Rate</h3>
          <div className="stat-number">
            {systemStats.forms?.approvalRate ? `${systemStats.forms.approvalRate.toFixed(1)}%` : '0%'}
          </div>
          <div className="stat-description">Overall form approval rate</div>
        </div>

        <div className="stat-card">
          <h3>Files</h3>
          <div className="stat-number">{systemStats.files?.totalFiles || 0}</div>
          <div className="stat-description">Total uploaded files</div>
        </div>
      </div>

      {/* Charts Section */}
      <div className="charts-section">
        <div className="chart-container">
          <h3>Form Types Distribution</h3>
          <div className="chart">
            {formStatsByType.typeCounts && Object.keys(formStatsByType.typeCounts).length > 0 ? (
              <div className="pie-chart">
                {Object.entries(formStatsByType.typeCounts).map(([type, count], index) => (
                  <div key={type} className="pie-segment" style={{
                    '--percentage': (count / Object.values(formStatsByType.typeCounts).reduce((a, b) => a + b, 0)) * 100,
                    '--color': `hsl(${index * 60}, 70%, 60%)`
                  }}>
                    <span className="segment-label">{type}: {count}</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="no-data">No form data available</div>
            )}
          </div>
        </div>

        <div className="chart-container">
          <h3>Monthly Trends</h3>
          <div className="chart">
            {monthlyTrends.monthlySubmissions && Object.keys(monthlyTrends.monthlySubmissions).length > 0 ? (
              <div className="bar-chart">
                {Object.entries(monthlyTrends.monthlySubmissions).map(([month, count]) => (
                  <div key={month} className="bar">
                    <div className="bar-fill" style={{ height: `${(count / Math.max(...Object.values(monthlyTrends.monthlySubmissions))) * 100}%` }}></div>
                    <span className="bar-label">{month}</span>
                    <span className="bar-value">{count}</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="no-data">No trend data available</div>
            )}
          </div>
        </div>
      </div>

      {/* Detailed Statistics */}
      <div className="detailed-stats">
        <div className="stats-section">
          <h3>Approval Performance</h3>
          <div className="performance-stats">
            <div className="performance-item">
              <span className="label">Average Approval Time:</span>
              <span className="value">
                {approvalPerformance.avgApprovalTimeHours ? 
                  `${approvalPerformance.avgApprovalTimeHours.toFixed(1)} hours` : 'N/A'}
              </span>
            </div>
            <div className="performance-item">
              <span className="label">Approval Time Distribution:</span>
              <div className="distribution-chart">
                {approvalPerformance.approvalTimeDistribution && 
                  Object.entries(approvalPerformance.approvalTimeDistribution).map(([timeframe, count]) => (
                    <div key={timeframe} className="distribution-bar">
                      <span className="timeframe">{timeframe}</span>
                      <div className="bar-container">
                        <div className="bar-fill" style={{ width: `${(count / Math.max(...Object.values(approvalPerformance.approvalTimeDistribution))) * 100}%` }}></div>
                      </div>
                      <span className="count">{count}</span>
                    </div>
                  ))}
              </div>
            </div>
          </div>
        </div>

        <div className="stats-section">
          <h3>Most Active Users</h3>
          <div className="active-users">
            {userActivity.mostActiveUsers && userActivity.mostActiveUsers.length > 0 ? (
              userActivity.mostActiveUsers.slice(0, 5).map((user, index) => (
                <div key={user.email} className="user-item">
                  <span className="rank">#{index + 1}</span>
                  <span className="name">{user.name}</span>
                  <span className="email">{user.email}</span>
                  <span className="role">{user.role}</span>
                  <span className="form-count">{user.formCount} forms</span>
                </div>
              ))
            ) : (
              <div className="no-data">No user activity data available</div>
            )}
          </div>
        </div>
      </div>

      {/* Audit Statistics */}
      <div className="audit-stats">
        <h3>System Activity</h3>
        <div className="audit-grid">
          {systemStats.audit && Object.entries(systemStats.audit).map(([action, count]) => (
            <div key={action} className="audit-item">
              <span className="action">{action.replace(/([A-Z])/g, ' $1').trim()}</span>
              <span className="count">{count}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ReportsDashboard; 