-- V2__create_categories_table.sql
CREATE TABLE categories (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    
    name       VARCHAR(50) NOT NULL,
    user_id    BIGINT      NOT NULL,
    is_default BOOLEAN     NOT NULL DEFAULT FALSE,  -- true = protected forever
    
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NULL     DEFAULT NULL 
                           ON UPDATE CURRENT_TIMESTAMP,

    -- Ownership & integrity
    FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,

    -- One user cannot have two categories with same name (case-sensitive in DB)
    UNIQUE KEY uk_user_category_name (user_id, name),

    -- Fast lookup by user
    INDEX idx_user_id (user_id),

    -- Optional: extra safety – default categories cannot be renamed/deleted in app
    -- (enforced in Java, but we keep the flag for clarity)
    INDEX idx_is_default (is_default)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;