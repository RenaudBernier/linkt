
import React from 'react';
import './Footer.css'; 

const Footer: React.FC = () => {
  return (
    <footer style={{
      backgroundColor: '#f5f5f5',
      color: '#333',
      padding: '2rem 4rem',
      display: 'flex',
      flexWrap: 'wrap',
      justifyContent: 'space-between',
      fontSize: '0.9rem',
    }}>
      {/* Column 1 */}
      <div>
        <h4 style={{ marginBottom: '0.8rem' }}>Product</h4>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Features</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Pricing</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Integrations</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Documentation</a></li>
        </ul>
      </div>

      {/* Column 2 */}
      <div>
        <h4 style={{ marginBottom: '0.8rem' }}>Company</h4>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>About Us</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Careers</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>News</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Contact</a></li>
        </ul>
      </div>

      {/* Column 3 */}
      <div>
        <h4 style={{ marginBottom: '0.8rem' }}>Developers</h4>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>API Docs</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Open Source</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Contribute</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>GitHub</a></li>
        </ul>
      </div>

      {/* Column 4 */}
      <div>
        <h4 style={{ marginBottom: '0.8rem' }}>Support</h4>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Help Center</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Community Forum</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>System Status</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Report an Issue</a></li>
        </ul>
      </div>

      {/* Column 5 */}
      <div>
        <h4 style={{ marginBottom: '0.8rem' }}>Follow Us</h4>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>Twitter</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>LinkedIn</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>GitHub</a></li>
          <li><a href="#" style={{ textDecoration: 'none', color: '#333' }}>YouTube</a></li>
        </ul>
      </div>

      {/* Bottom bar */}
      <div style={{
        width: '100%',
        borderTop: '1px solid #ccc',
        marginTop: '2rem',
        paddingTop: '1rem',
        textAlign: 'center'
      }}>
        <p>© 2025 Linkt. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default Footer;
