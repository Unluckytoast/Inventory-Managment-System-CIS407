import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './InventoryManager.css';

const InventoryManager = () => {
    const [products, setProducts] = useState([]);
    const [suppliers, setSuppliers] = useState([]);
    const [editId, setEditId] = useState(null);
    const [formData, setFormData] = useState({ name: '', sku: '', description: '', unitPrice: '', reorderPoint: '', targetStock: '', supplierId: '' });
    const [stockUpdate, setStockUpdate] = useState({});
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState('');

    useEffect(() => {
        fetchData();
        // eslint-disable-next-line
    }, []);

    const fetchData = async () => {
        try {
            const [productsRes, suppliersRes] = await Promise.all([
                axios.get('http://localhost:8080/api/stock/available'),
                axios.get('http://localhost:8080/api/suppliers')
            ]);
            setProducts(productsRes.data);
            setSuppliers(suppliersRes.data);
            setLoading(false);
        } catch (error) {
            showMessage('Error loading data');
            setLoading(false);
        }
    };

    const showMessage = (msg) => {
        setMessage(msg);
        setTimeout(() => setMessage(''), 3000);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const productData = {
            ...formData,
            unitPrice: parseFloat(formData.unitPrice) || 0,
            reorderPoint: parseInt(formData.reorderPoint) || 0,
            targetStock: parseInt(formData.targetStock) || 0,
            supplier: formData.supplierId ? { id: parseInt(formData.supplierId) } : null
        };
        if (editId) productData.id = editId;

        try {
            await axios.post('http://localhost:8080/api/products', productData);
            showMessage(editId ? 'Product updated!' : 'Product added!');
            resetForm();
            fetchData();
        } catch (error) {
            showMessage('Error saving product');
        }
    };

    const handleEdit = (product) => {
        setEditId(product.id);
        setFormData({
            name: product.name || '',
            sku: product.sku || '',
            description: product.description || '',
            unitPrice: product.unitPrice || '',
            reorderPoint: product.reorderPoint || '',
            targetStock: product.targetStock || '',
            supplierId: product.supplier?.id || ''
        });
    };

    const resetForm = () => {
        setFormData({ name: '', sku: '', description: '', unitPrice: '', reorderPoint: '', targetStock: '', supplierId: '' });
        setEditId(null);
    };

    const updateStock = async (productId) => {
        const quantity = parseInt(stockUpdate[productId]);
        if (isNaN(quantity) || quantity < 0) return showMessage('Invalid quantity');

        try {
            await axios.post('http://localhost:8080/api/stock/update', { productId, quantity });
            showMessage('Stock updated!');
            setStockUpdate({ ...stockUpdate, [productId]: '' });
            fetchData();
        } catch (error) {
            showMessage('Error updating stock');
        }
    };

    if (loading) {
        return <div className="loading">Loading inventory...</div>;
    }

    return (
        <div className="inventory-manager">
            <h1>Inventory Manager</h1>
            {message && <div className="message">{message}</div>}

            <form onSubmit={handleSubmit} className="product-form">
                <h2>{editId ? 'Edit Product' : 'Add Product'}</h2>
                <div className="form-grid">
                    <input type="text" name="name" placeholder="Product Name" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} required />
                    <input type="text" name="sku" placeholder="SKU" value={formData.sku} onChange={(e) => setFormData({ ...formData, sku: e.target.value })} required />
                    <input type="number" name="unitPrice" placeholder="Unit Price" step="0.01" value={formData.unitPrice} onChange={(e) => setFormData({ ...formData, unitPrice: e.target.value })} />
                    <select name="supplierId" value={formData.supplierId} onChange={(e) => setFormData({ ...formData, supplierId: e.target.value })}>
                        <option value="">Select Supplier</option>
                        {suppliers.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                    <input type="number" name="reorderPoint" placeholder="Reorder Point" value={formData.reorderPoint} onChange={(e) => setFormData({ ...formData, reorderPoint: e.target.value })} />
                    <input type="number" name="targetStock" placeholder="Target Stock" value={formData.targetStock} onChange={(e) => setFormData({ ...formData, targetStock: e.target.value })} />
                    <textarea name="description" placeholder="Description" value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} rows="2" className="full" />
                </div>
                <div className="form-actions">
                    <button type="submit">{editId ? 'Update' : 'Add'} Product</button>
                    {editId && <button type="button" onClick={resetForm}>Cancel</button>}
                </div>
            </form>

            <h2>Inventory</h2>
            <table>
                <thead>
                    <tr>
                        <th>SKU</th>
                        <th>Name</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Update Stock</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {products.map(product => (
                        <tr key={product.id}>
                            <td>{product.sku}</td>
                            <td>{product.name}</td>
                            <td>${product.unitPrice?.toFixed(2)}</td>
                            <td className={product.stockQuantity === 0 ? 'out' : product.stockQuantity < 10 ? 'low' : 'ok'}>
                                {product.stockQuantity}
                            </td>
                            <td>
                                <input type="number" min="0" placeholder="Qty" value={stockUpdate[product.id] || ''} onChange={(e) => setStockUpdate({ ...stockUpdate, [product.id]: e.target.value })} />
                                <button onClick={() => updateStock(product.id)} disabled={!stockUpdate[product.id]}>Set</button>
                            </td>
                            <td>
                                <button onClick={() => handleEdit(product)}>Edit</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default InventoryManager;
