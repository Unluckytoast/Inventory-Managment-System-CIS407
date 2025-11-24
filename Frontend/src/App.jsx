import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainDashboard from './pages/MainDashboard';
import Shop from './pages/Shop';
import InventoryManager from './pages/InventoryManager';
import PurchaseOrders from './pages/PurchaseOrders';
import SupplierManager from './pages/SupplierManager';
import Navigation from './components/Navigation';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<MainDashboard />} />
                <Route path="/shop" element={<><Navigation /><Shop /></>} />
                <Route path="/inventory-manager" element={<><Navigation /><InventoryManager /></>} />
                <Route path="/purchase-orders" element={<><Navigation /><PurchaseOrders /></>} />
                <Route path="/supplier-manager" element={<><Navigation /><SupplierManager /></>} />
            </Routes>
        </Router>
    );
}

export default App;
