-- 創建訂單表
CREATE TABLE orders (
    order_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    billing_address VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    notes VARCHAR(1000),
    order_date DATETIME NOT NULL,
    confirmed_date DATETIME,
    paid_date DATETIME,
    shipped_date DATETIME,
    delivered_date DATETIME,
    cancelled_date DATETIME,
    cancellation_reason VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_order_customer_id (customer_id),
    INDEX idx_order_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_order_customer_status (customer_id, status)
);

-- 創建訂單項目表
CREATE TABLE order_items (
    order_item_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    product_specifications VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_item_order_id (order_id),
    INDEX idx_order_item_product_id (product_id)
);

-- 創建購物車表
CREATE TABLE carts (
    cart_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    last_updated DATETIME NOT NULL,
    expiry_date DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE INDEX idx_cart_customer_id (customer_id),
    INDEX idx_cart_expiry_date (expiry_date),
    INDEX idx_cart_last_updated (last_updated)
);

-- 創建購物車項目表
CREATE TABLE cart_items (
    cart_item_id VARCHAR(50) PRIMARY KEY,
    cart_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    product_specifications VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    INDEX idx_cart_item_cart_id (cart_id),
    INDEX idx_cart_item_product_id (product_id),
    UNIQUE INDEX idx_cart_item_cart_product (cart_id, product_id)
);