-- Create budgets table
CREATE TABLE budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE,
    is_recurring BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent overlapping start dates for same category
    CONSTRAINT uk_user_category_period UNIQUE (user_id, category_id, period_start),
    
    -- Data integrity constraints
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_period_valid CHECK (period_end IS NULL OR period_end >= period_start)
);

-- Indexes for performance
CREATE INDEX idx_user_category ON budgets(user_id, category_id);
CREATE INDEX idx_user_active ON budgets(user_id, is_active);
CREATE INDEX idx_category_period ON budgets(category_id, period_start, period_end);