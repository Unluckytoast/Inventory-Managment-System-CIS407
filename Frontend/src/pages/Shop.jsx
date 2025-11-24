import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './Shop.css';

const Shop = () => {
    const [products, setProducts] = useState([]);
    const [cart, setCart] = useState([]);
    const [customer, setCustomer] = useState({ name: '', email: '', phone: '', address: '' });
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState('');

    useEffect(() => {
        fetchProducts();
    }, []);

    const fetchProducts = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/stock/available');
            setProducts(response.data);
            setLoading(false);
        } catch (error) {
            console.error('Error fetching products:', error);
            setLoading(false);
        }
    };

    const showMessage = (msg) => {
        setMessage(msg);
        setTimeout(() => setMessage(''), 3000);
    };

    const addToCart = (product) => {
        if (product.stockQuantity === 0) return showMessage('Out of stock!');
        
        const existing = cart.find(item => item.id === product.id);
        if (existing) {
            if (existing.quantity < product.stockQuantity) {
                setCart(cart.map(item => item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item));
            } else {
                showMessage('Not enough stock!');
            }
        } else {
            setCart([...cart, { ...product, quantity: 1 }]);
        }
    };

    const updateQuantity = (productId, change) => {
        setCart(cart.map(item => {
            if (item.id !== productId) return item;
            const newQty = item.quantity + change;
            if (newQty <= 0) return null;
            const product = products.find(p => p.id === productId);
            if (newQty > product.stockQuantity) {
                showMessage('Not enough stock!');
                return item;
            }
            return { ...item, quantity: newQty };
        }).filter(Boolean));
    };

    const calculateTotal = () => {
        return cart.reduce((total, item) => total + (item.unitPrice * item.quantity), 0).toFixed(2);
    };

    const handleCheckout = async (e) => {
        e.preventDefault();
        if (cart.length === 0) return showMessage('Cart is empty!');

        try {
            await axios.post('http://localhost:8080/api/orders', {
                customer,
                orderDate: new Date().toISOString(),
                status: 'PENDING',
                items: cart.map(item => ({ product: { id: item.id }, quantity: item.quantity, unitPrice: item.unitPrice }))
            });
            showMessage('✓ Order placed successfully!');
            setCart([]);
            setCustomer({ name: '', email: '', phone: '', address: '' });
            fetchProducts();
        } catch (error) {
            showMessage('Failed to place order');
        }
    };

    if (loading) {
        return <div className="loading">Loading products...</div>;
    }

    return (
        <div className="shop-container">
            <h1>Customer Shop</h1>
            {message && <div className="message">{message}</div>}

            <div className="shop-layout">
                <div className="products-section">
                    <h2>Products</h2>
                    <div className="product-grid">
                        {products.map(product => (
                            <div key={product.id} className="product-card">
                                <h3>{product.name}</h3>
                                <p className="description">{product.description}</p>
                                <div className="product-footer">
                                    <span className="price">${product.unitPrice?.toFixed(2)}</span>
                                    <span className={`stock ${product.stockQuantity > 0 ? 'in' : 'out'}`}>
                                        {product.stockQuantity > 0 ? `${product.stockQuantity} in stock` : 'Out of stock'}
                                    </span>
                                </div>
                                <button onClick={() => addToCart(product)} disabled={!product.stockQuantity}>
                                    Add to Cart
                                </button>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="cart-section">
                    <h2>Cart ({cart.length})</h2>
                    {cart.length === 0 ? (
                        <p className="empty">Cart is empty</p>
                    ) : (
                        <>
                            {cart.map(item => (
                                <div key={item.id} className="cart-item">
                                    <div>
                                        <h4>{item.name}</h4>
                                        <p>${item.unitPrice?.toFixed(2)} × {item.quantity} = ${(item.unitPrice * item.quantity).toFixed(2)}</p>
                                    </div>
                                    <div className="controls">
                                        <button onClick={() => updateQuantity(item.id, -1)}>-</button>
                                        <span>{item.quantity}</span>
                                        <button onClick={() => updateQuantity(item.id, 1)}>+</button>
                                    </div>
                                </div>
                            ))}
                            <div className="total">Total: ${calculateTotal()}</div>
                            <form onSubmit={handleCheckout}>
                                <h3>Checkout</h3>
                                <input type="text" placeholder="Name" value={customer.name} onChange={(e) => setCustomer({ ...customer, name: e.target.value })} required />
                                <input type="email" placeholder="Email" value={customer.email} onChange={(e) => setCustomer({ ...customer, email: e.target.value })} required />
                                <input type="tel" placeholder="Phone" value={customer.phone} onChange={(e) => setCustomer({ ...customer, phone: e.target.value })} />
                                <input type="text" placeholder="Address" value={customer.address} onChange={(e) => setCustomer({ ...customer, address: e.target.value })} required />
                                <button type="submit">Place Order</button>
                            </form>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Shop;
