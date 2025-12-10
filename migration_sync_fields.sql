-- Migration script to add sync fields to categories and products tables

-- Add sync_status to categories table
ALTER TABLE categories 
ADD COLUMN sync_status VARCHAR(20) DEFAULT 'PENDING' AFTER is_public;

-- Add sync_status and product_string to products table  
ALTER TABLE products 
ADD COLUMN sync_status VARCHAR(20) DEFAULT 'PENDING' AFTER is_custom,
ADD COLUMN product_string TEXT AFTER sync_status;

-- Update existing records to have PENDING status
UPDATE categories SET sync_status = 'PENDING' WHERE sync_status IS NULL;
UPDATE products SET sync_status = 'PENDING' WHERE sync_status IS NULL;

-- Add indexes for better performance
CREATE INDEX idx_categories_sync_status ON categories(sync_status, updated_at);
CREATE INDEX idx_products_sync_status ON products(sync_status, updated_at);