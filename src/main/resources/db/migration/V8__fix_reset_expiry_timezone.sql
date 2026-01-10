-- V8__fix_reset_expiry_timezone.sql
ALTER TABLE users 
MODIFY COLUMN reset_expires_at TIMESTAMP NULL;

-- Optional: Recreate index
DROP INDEX idx_reset_token ON users;
CREATE INDEX idx_reset_token ON users(reset_token);