-- V6__add_remember_me_columns.sql

ALTER TABLE users
ADD COLUMN remember_token VARCHAR(64) NULL,
ADD COLUMN remember_expires_at DATETIME NULL;

-- Essential for performance: allows O(1) lookup speed
CREATE INDEX idx_users_remember_token ON users(remember_token);