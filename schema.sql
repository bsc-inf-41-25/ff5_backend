
CREATE DATABASE IF NOT EXISTS ticketmaster
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ticketmaster;

-- 1. Users and security
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'ORGANIZER', 'VALIDATOR', 'ADMIN')
        NOT NULL DEFAULT 'CUSTOMER',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

-- 2. Customer profiles
CREATE TABLE IF NOT EXISTS customer_profiles (
    user_id BIGINT PRIMARY KEY,
    phone VARCHAR(30),
    city VARCHAR(100),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Interests and customer preferences
CREATE TABLE IF NOT EXISTS interests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customer_interests (
    customer_id BIGINT NOT NULL,
    interest_id BIGINT NOT NULL,
    PRIMARY KEY (customer_id, interest_id),
    FOREIGN KEY (customer_id) REFERENCES users(id)
        ON DELETE CASCADE,
    FOREIGN KEY (interest_id) REFERENCES interests(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Events
CREATE TABLE IF NOT EXISTS events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organizer_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    venue VARCHAR(200) NOT NULL,
    location VARCHAR(255),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    start_time DATETIME(6) NOT NULL,
    end_time DATETIME(6) NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'CANCELLED')
        NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (organizer_id) REFERENCES users(id),
    INDEX idx_event_status_start (status, start_time),
    INDEX idx_event_category (category),
    CHECK (end_time > start_time)
) ENGINE=InnoDB;

-- 5. Ticket types, prices and capacity
CREATE TABLE IF NOT EXISTS ticket_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    total_quantity INT NOT NULL,
    quantity_reserved INT NOT NULL DEFAULT 0,
    quantity_sold INT NOT NULL DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events(id),
    UNIQUE KEY uq_event_ticket_type (event_id, name),
    CHECK (price >= 0),
    CHECK (total_quantity > 0),
    CHECK (quantity_reserved >= 0),
    CHECK (quantity_sold >= 0),
    CHECK (quantity_reserved + quantity_sold <= total_quantity)
) ENGINE=InnoDB;

-- 6. Orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    status ENUM('PENDING', 'PAID', 'FAILED', 'CANCELLED')
        NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(12,2) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (customer_id) REFERENCES users(id),
    INDEX idx_order_customer_status (customer_id, status)
) ENGINE=InnoDB;

-- 7. Items within an order
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    ticket_type_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id),
    CHECK (quantity > 0),
    CHECK (unit_price >= 0)
) ENGINE=InnoDB;

-- 8. Payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    method VARCHAR(50) NOT NULL DEFAULT 'MOCK',
    status ENUM('SUCCESS', 'FAILED') NOT NULL,
    reference VARCHAR(150) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    CHECK (amount >= 0)
) ENGINE=InnoDB;

-- 9. Digital tickets and door validation
CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    ticket_type_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    ticket_code VARCHAR(100) NOT NULL UNIQUE,
    status ENUM('VALID', 'USED', 'CANCELLED')
        NOT NULL DEFAULT 'VALID',
    issued_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    used_at DATETIME(6) NULL,
    validated_by BIGINT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id),
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (validated_by) REFERENCES users(id),
    INDEX idx_ticket_owner (owner_id),
    INDEX idx_ticket_type_status (ticket_type_id, status)
) ENGINE=InnoDB;

-- Seed the shared interest categories
INSERT IGNORE INTO interests (name)
VALUES ('Music'), ('Sports'), ('Tech'), ('Community');
