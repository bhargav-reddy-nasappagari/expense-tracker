-- Add columns for secure token storage
ALTER TABLE users
ADD COLUMN reset_token VARCHAR(255) NULL,
ADD COLUMN reset_expires_at DATETIME NULL;

-- Create an index for fast lookups when validating tokens
CREATE INDEX idx_reset_token ON users(reset_token);