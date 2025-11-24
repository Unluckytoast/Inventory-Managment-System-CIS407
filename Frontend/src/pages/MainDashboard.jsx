import React from 'react';
import { useNavigate } from 'react-router-dom';
import './MainDashboard.css';

const MainDashboard = () => {
    const navigate = useNavigate();

    const pages = [
        {
            title: 'Customer Shop',
            description: 'Browse products and place orders',
            icon: '',
            path: '/shop',
          
        },
        {
            title: 'Inventory Manager',
            description: 'Add/update products and track stock',
            icon: '',
            path: '/inventory-manager',
           
        },
        {
            title: 'Purchase Orders',
            description: 'Generate orders for low stock items',
            icon: '',
            path: '/purchase-orders',
           
        },
        {
            title: 'Supplier Manager',
            description: 'Add and manage suppliers',
            icon: '',
            path: '/supplier-manager',
            
        }
    ];

    return (
        <div className="main-dashboard">
            <header className="dashboard-header">
                <h1>Inventory Management System</h1>
                <p className="subtitle"></p>
            </header>

            <div className="dashboard-grid">
                {pages.map((page, index) => (
                    <div
                        key={index}
                        className="dashboard-card"
                        onClick={() => navigate(page.path)}
                        style={{ borderTop: `4px solid ${page.color}` }}
                    >
                        <div className="card-icon" style={{ color: page.color }}>
                            {page.icon}
                        </div>
                        <h2>{page.title}</h2>
                        <p>{page.description}</p>
                        <div className="card-arrow" style={{ color: page.color }}>→</div>
                    </div>
                ))}
            </div>

            <footer className="dashboard-footer">
                <p>© 2025 Inventory Management System</p>
            </footer>
        </div>
    );
};

export default MainDashboard;
