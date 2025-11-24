import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './SupplierManager.css';

const SupplierManager = () => {
    const [suppliers, setSuppliers] = useState([]);
    const [form, setForm] = useState({ name: '', contact: '', phone: '', email: '', address: '' });
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState('');

    useEffect(() => {
        fetchSuppliers();
    }, []);

    const fetchSuppliers = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/suppliers');
            setSuppliers(response.data);
            setLoading(false);
        } catch (error) {
            console.error('Error fetching suppliers:', error);
            setLoading(false);
        }
    };

    const showMessage = (msg) => {
        setMessage(msg);
        setTimeout(() => setMessage(''), 3000);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.post('http://localhost:8080/api/suppliers', form);
            showMessage('✓ Supplier created successfully!');
            setForm({ name: '', contact: '', phone: '', email: '', address: '' });
            fetchSuppliers();
        } catch (error) {
            showMessage('Failed to create supplier');
        }
    };

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    if (loading) {
        return <div className="loading">Loading suppliers...</div>;
    }

    return (
        <div className="supplier-manager">
            <h1>Supplier Management</h1>
            {message && <div className="message">{message}</div>}

            <div className="supplier-form">
                <h2>Add New Supplier</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-grid">
                        <div className="form-group">
                            <label>Supplier Name *</label>
                            <input
                                type="text"
                                name="name"
                                value={form.name}
                                onChange={handleChange}
                                required
                                placeholder="Enter supplier name"
                            />
                        </div>

                        <div className="form-group">
                            <label>Contact Person *</label>
                            <input
                                type="text"
                                name="contact"
                                value={form.contact}
                                onChange={handleChange}
                                required
                                placeholder="Enter contact person"
                            />
                        </div>

                        <div className="form-group">
                            <label>Phone</label>
                            <input
                                type="tel"
                                name="phone"
                                value={form.phone}
                                onChange={handleChange}
                                placeholder="555-0123"
                            />
                        </div>

                        <div className="form-group">
                            <label>Email *</label>
                            <input
                                type="email"
                                name="email"
                                value={form.email}
                                onChange={handleChange}
                                required
                                placeholder="supplier@example.com"
                            />
                        </div>

                        <div className="form-group full-width">
                            <label>Address</label>
                            <input
                                type="text"
                                name="address"
                                value={form.address}
                                onChange={handleChange}
                                placeholder="Enter full address"
                            />
                        </div>
                    </div>

                    <div className="form-actions">
                        <button type="submit">Add Supplier</button>
                        <button type="button" onClick={() => setForm({ name: '', contact: '', phone: '', email: '', address: '' })}>
                            Clear Form
                        </button>
                    </div>
                </form>
            </div>

            <div className="suppliers-list">
                <h2>Existing Suppliers ({suppliers.length})</h2>
                {suppliers.length === 0 ? (
                    <p className="empty">No suppliers found. Add your first supplier above.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Contact</th>
                                <th>Phone</th>
                                <th>Email</th>
                                <th>Address</th>
                            </tr>
                        </thead>
                        <tbody>
                            {suppliers.map(supplier => (
                                <tr key={supplier.id}>
                                    <td>{supplier.id}</td>
                                    <td><strong>{supplier.name}</strong></td>
                                    <td>{supplier.contact}</td>
                                    <td>{supplier.phone || 'N/A'}</td>
                                    <td>{supplier.email}</td>
                                    <td>{supplier.address || 'N/A'}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default SupplierManager;
