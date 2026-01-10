-- 1. Add columns for Email Verification and Legacy Status
ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN verification_token_hash VARCHAR(64) NULL,
    ADD COLUMN token_created_at TIMESTAMP NULL,
    ADD COLUMN legacy_unverified BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Add Index for fast token lookups
CREATE INDEX idx_verification_token ON users(verification_token_hash);

-- 3. Grandfather Clause: Mark ALL existing users as 'Legacy'
-- This ensures no current user is locked out when the new logic goes live.
UPDATE users SET legacy_unverified = TRUE;