
-- Suppliers
CREATE TABLE IF NOT EXISTS suppliers (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL,
	contact TEXT,
	phone TEXT,
	email TEXT,
	address TEXT
);

-- Products
CREATE TABLE IF NOT EXISTS products (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	sku TEXT UNIQUE,
	name TEXT NOT NULL,
	description TEXT,
	supplier_id INTEGER,
	unit_price REAL DEFAULT 0,
	reorder_point INTEGER DEFAULT 0,
	target_stock INTEGER DEFAULT 0,
	FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);

-- Customers
CREATE TABLE IF NOT EXISTS customers (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL,
	email TEXT,
	phone TEXT,
	address TEXT
);

-- Stock
CREATE TABLE IF NOT EXISTS stock (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	product_id INTEGER UNIQUE,
	quantity INTEGER NOT NULL DEFAULT 0,
	last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	customer_id INTEGER,
	order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
	status TEXT,
	FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Order Items
CREATE TABLE IF NOT EXISTS order_items (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	order_id INTEGER,
	product_id INTEGER,
	quantity INTEGER NOT NULL,
	unit_price REAL,
	FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
	FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Purchase Orders
CREATE TABLE IF NOT EXISTS purchase_orders (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	supplier_id INTEGER,
	created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
	status TEXT,
	FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- Purchase Order Items
CREATE TABLE IF NOT EXISTS purchase_order_items (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	purchase_order_id INTEGER,
	product_id INTEGER,
	quantity INTEGER NOT NULL,
	unit_price REAL,
	FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
	FOREIGN KEY (product_id) REFERENCES products(id)
);
