import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './PurchaseOrders.css';

const PurchaseOrders = () => {
    const [lowStockProducts, setLowStockProducts] = useState([]);
    const [purchaseOrders, setPurchaseOrders] = useState([]);
    const [selectedItems, setSelectedItems] = useState({});
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState('');
    const [activeTab, setActiveTab] = useState('low-stock');

    useEffect(() => {
        fetchData();
        // eslint-disable-next-line
    }, []);

    const fetchData = async () => {
        try {
            const [lowStockRes, ordersRes] = await Promise.all([
                axios.get('http://localhost:8080/api/stock/below-reorder'),
                axios.get('http://localhost:8080/api/purchase-orders')
            ]);
            setLowStockProducts(Array.isArray(lowStockRes.data) ? lowStockRes.data : []);
            setPurchaseOrders(Array.isArray(ordersRes.data) ? ordersRes.data : []);
            
            const initial = {};
            const products = Array.isArray(lowStockRes.data) ? lowStockRes.data : [];
            products.forEach(product => {
                initial[product.id] = product.recommendedOrder || 0;
            });
            setSelectedItems(initial);
            setLoading(false);
        } catch (error) {
            console.error('Error loading data:', error);
            showMessage('Error loading data');
            setPurchaseOrders([]);
            setLowStockProducts([]);
            setLoading(false);
        }
    };

    const showMessage = (msg) => {
        setMessage(msg);
        setTimeout(() => setMessage(''), 3000);
    };

    const handleQuantityChange = (productId, value) => {
        setSelectedItems({ ...selectedItems, [productId]: parseInt(value) || 0 });
    };

    const toggleProduct = (productId) => {
        const product = lowStockProducts.find(p => p.id === productId);
        setSelectedItems({ ...selectedItems, [productId]: selectedItems[productId] > 0 ? 0 : product.recommendedOrder });
    };

    const generatePurchaseOrder = async () => {
        const itemsToOrder = lowStockProducts.filter(p => selectedItems[p.id] > 0);
        if (itemsToOrder.length === 0) return showMessage('Select at least one product');

        const ordersBySupplier = {};
        itemsToOrder.forEach(product => {
            const supplierId = product.supplier?.id || 'none';
            if (!ordersBySupplier[supplierId]) {
                ordersBySupplier[supplierId] = { supplier: product.supplier, items: [] };
            }
            ordersBySupplier[supplierId].items.push({
                product: { id: product.id },
                quantity: selectedItems[product.id],
                unitPrice: product.unitPrice
            });
        });

        try {
            await Promise.all(Object.values(ordersBySupplier).map(order =>
                axios.post('http://localhost:8080/api/purchase-orders', {
                    supplier: order.supplier,
                    createdDate: new Date().toISOString(),
                    status: 'CREATED',
                    items: order.items
                })
            ));
            showMessage('✓ Purchase orders created!');
            fetchData();
            setActiveTab('orders');
        } catch (error) {
            showMessage('Error creating orders');
        }
    };

    const receivePurchaseOrder = async (orderId) => {
        try {
            await axios.post(`http://localhost:8080/api/purchase-orders/${orderId}/receive`);
            showMessage('✓ Order received and stock updated!');
            fetchData();
        } catch (error) {
            showMessage('Error receiving order');
        }
    };

    const calculateTotal = (items) => {
        return items.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0).toFixed(2);
    };

    if (loading) {
        return <div className="loading">Loading purchase orders...</div>;
    }

    return (
        <div className="purchase-orders">
            <h1>Purchase Orders</h1>
            {message && <div className="message">{message}</div>}

            <div className="tabs">
                <button className={activeTab === 'low-stock' ? 'active' : ''} onClick={() => setActiveTab('low-stock')}>
                    Low Stock ({lowStockProducts.length})
                </button>
                <button className={activeTab === 'orders' ? 'active' : ''} onClick={() => setActiveTab('orders')}>
                    Orders ({Array.isArray(purchaseOrders) ? purchaseOrders.length : 0})
                </button>
            </div>

            {activeTab === 'low-stock' && (
                lowStockProducts.length === 0 ? (
                    <div className="no-data">✓ All stock levels are healthy!</div>
                ) : (
                    <>
                        <button onClick={generatePurchaseOrder} disabled={Object.values(selectedItems).every(v => v === 0)}>
                            Generate Purchase Orders
                        </button>
                        <table>
                            <thead>
                                <tr>
                                    <th>Select</th>
                                    <th>Product</th>
                                    <th>Supplier</th>
                                    <th>Stock</th>
                                    <th>Reorder</th>
                                    <th>Order Qty</th>
                                    <th>Price</th>
                                    <th>Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                {lowStockProducts.map(product => (
                                    <tr key={product.id} className={selectedItems[product.id] > 0 ? 'selected' : ''}>
                                        <td>
                                            <input type="checkbox" checked={selectedItems[product.id] > 0} onChange={() => toggleProduct(product.id)} />
                                        </td>
                                        <td>{product.name}</td>
                                        <td>{product.supplier?.name || 'None'}</td>
                                        <td className="critical">{product.currentStock}</td>
                                        <td>{product.reorderPoint}</td>
                                        <td>
                                            <input type="number" min="0" value={selectedItems[product.id] || 0} onChange={(e) => handleQuantityChange(product.id, e.target.value)} />
                                        </td>
                                        <td>${product.unitPrice?.toFixed(2)}</td>
                                        <td>${((selectedItems[product.id] || 0) * product.unitPrice).toFixed(2)}</td>
                                    </tr>
                                ))}
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colSpan="7">Total:</td>
                                    <td>${lowStockProducts.reduce((sum, p) => sum + ((selectedItems[p.id] || 0) * p.unitPrice), 0).toFixed(2)}</td>
                                </tr>
                            </tfoot>
                        </table>
                    </>
                )
            )}

            {activeTab === 'orders' && (
                !Array.isArray(purchaseOrders) || purchaseOrders.length === 0 ? (
                    <div className="no-data">No purchase orders found</div>
                ) : (
                    purchaseOrders.map(order => (
                        <div key={order.id} className="order-card">
                            <div className="order-header">
                                <h3>Order #{order.id}</h3>
                                <span className="status">{order.status}</span>
                                <p>{order.supplier?.name || 'No Supplier'}</p>
                                <small>{new Date(order.createdDate).toLocaleString()}</small>
                                {order.status === 'CREATED' && (
                                    <button className="receive-btn" onClick={() => receivePurchaseOrder(order.id)}>
                                        Receive Order
                                    </button>
                                )}
                            </div>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>Qty</th>
                                        <th>Price</th>
                                        <th>Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {order.items?.map((item, idx) => (
                                        <tr key={idx}>
                                            <td>{item.product?.name || `Product #${item.product?.id}`}</td>
                                            <td>{item.quantity}</td>
                                            <td>${item.unitPrice?.toFixed(2)}</td>
                                            <td>${(item.quantity * item.unitPrice).toFixed(2)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                                <tfoot>
                                    <tr>
                                        <td colSpan="3">Order Total:</td>
                                        <td>${calculateTotal(order.items || [])}</td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>
                    ))
                )
            )}
        </div>
    );
};

export default PurchaseOrders;
