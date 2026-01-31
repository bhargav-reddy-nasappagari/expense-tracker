-- V3__create_expenses_table.sql
CREATE TABLE expenses (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    user_id       BIGINT        NOT NULL,
    description   VARCHAR(255)  NOT NULL,
    amount        DECIMAL(10,2) NOT NULL,
    category_id   INT           NOT NULL,
    expense_date  DATE          NOT NULL,
    
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NULL     DEFAULT NULL 
                        ON UPDATE CURRENT_TIMESTAMP,

    -- Ownership & data integrity
    FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
        
    FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE RESTRICT,

    -- Performance indexes
    INDEX idx_user_date_id     (user_id, expense_date DESC, id),
    INDEX idx_user_category (user_id, category_id),
    INDEX idx_category      (category_id)

    -- Note: expense_date validation is handled in application layer

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;