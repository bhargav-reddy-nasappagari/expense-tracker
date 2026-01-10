-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(10) NULL, -- exactly 10 digits, no +91, no dashes

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    -- DB-level constraints
    CONSTRAINT uk_username UNIQUE (username),
    CONSTRAINT uk_email UNIQUE (email),
    
    -- Phone: either NULL or exactly 10 digits
    CONSTRAINT chk_phone_valid 
        CHECK (phone IS NULL OR (LENGTH(phone) = 10 AND phone REGEXP '^[0-9]{10}$')),

    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;