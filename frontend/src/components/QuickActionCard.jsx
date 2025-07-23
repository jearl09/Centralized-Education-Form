import React from 'react';
import '../styles/dashboard.css';

const QuickActionCard = ({ icon, label, description, onClick }) => (
  <div className="quick-action-card" onClick={onClick} tabIndex={0} role="button">
    <div className="quick-action-icon">{icon}</div>
    <div className="quick-action-label">{label}</div>
    <div className="quick-action-desc">{description}</div>
  </div>
);

export default QuickActionCard; 