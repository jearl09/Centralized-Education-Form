import React from 'react';
import '../styles/dashboard.css';

const WelcomeBanner = ({ name, subtitle }) => (
  <div className="welcome-banner">
    <h2>Welcome back, {name}!</h2>
    <p>{subtitle}</p>
  </div>
);

export default WelcomeBanner; 