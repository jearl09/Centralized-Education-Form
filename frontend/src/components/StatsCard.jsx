import React from 'react';
import '../styles/dashboard.css';

const StatsCard = ({ icon, value, label, colorClass = '' }) => (
  <div className={`stats-card ${colorClass}`}>
    <div className="stats-icon">{icon}</div>
    <div className="stats-value">{value}</div>
    <div className="stats-label">{label}</div>
  </div>
);

export default StatsCard; 