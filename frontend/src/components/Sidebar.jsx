import React from 'react';
import '../styles/dashboard.css';

const Sidebar = ({ menuItems, activeTab, onTabChange, onLogout, header = 'Student Portal' }) => {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">{header}</div>
      <ul className="sidebar-menu">
        {menuItems.map(item => (
          <li
            key={item.id}
            className={activeTab === item.id ? 'active' : ''}
            onClick={() => onTabChange(item.id)}
          >
            <span className="sidebar-icon">{item.icon}</span>
            {item.label}
          </li>
        ))}
      </ul>
      <button className="btn btn-outline logout-btn" onClick={onLogout}>Logout</button>
    </aside>
  );
};

export default Sidebar; 