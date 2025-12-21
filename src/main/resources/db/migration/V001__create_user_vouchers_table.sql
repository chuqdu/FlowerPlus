-- Migration script for creating user_vouchers table
-- This table manages the relationship between vouchers and users for personal vouchers

CREATE TABLE IF NOT EXISTS user_vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assigned_at DATETIME NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at DATETIME NULL,
    created_by VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_user_vouchers_voucher_id FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_vouchers_user_id FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate assignments
    CONSTRAINT uk_user_vouchers_voucher_user UNIQUE (voucher_id, user_id),
    
    -- Indexes for performance
    INDEX idx_user_voucher_user_id (user_id),
    INDEX idx_user_voucher_voucher_id (voucher_id),
    INDEX idx_user_voucher_assigned_at (assigned_at),
    INDEX idx_user_voucher_is_used (is_used),
    INDEX idx_user_voucher_created_by (created_by)
);

-- Add comment to table
ALTER TABLE user_vouchers COMMENT = 'Table to manage personal voucher assignments to specific users';