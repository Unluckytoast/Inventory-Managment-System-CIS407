import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './Navigation.css';

const Navigation = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const navItems = [
        { title: 'Main Dashboard', path: '/', icon: '' },
        { title: 'Customer Shop', path: '/shop', icon: '' },
        { title: 'Inventory Manager', path: '/inventory-manager', icon: '' },
        { title: 'Purchase Orders', path: '/purchase-orders', icon: '' },
        { title: 'Supplier Manager', path: '/supplier-manager', icon: '' }
    ];

    const currentPage = navItems.find(item => item.path === location.pathname)?.title || 'Inventory System';

    return (
        <nav className="navigation">
            <div className="nav-container">
                <div className="nav-brand" onClick={() => navigate('/')}>
                    <span className="brand-icon"></span>
                    <span className="brand-text">Inventory System</span>
                </div>

                <button 
                    className="nav-toggle"
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                >
                    {isMenuOpen ? '✕' : '☰'}
                </button>

                <div className={`nav-menu ${isMenuOpen ? 'active' : ''}`}>
                    {navItems.map((item, index) => (
                        <div
                            key={index}
                            className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
                            onClick={() => {
                                navigate(item.path);
                                setIsMenuOpen(false);
                            }}
                        >
                            <span className="nav-icon">{item.icon}</span>
                            <span className="nav-title">{item.title}</span>
                        </div>
                    ))}
                </div>

                <div className="nav-current">
                    {currentPage}
                </div>
            </div>
        </nav>
    );
};

export default Navigation;
