-- Create product_favorites table
CREATE TABLE product_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_product_favorites_user 
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_favorites_product 
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate favorites
    CONSTRAINT uk_user_product_favorite 
        UNIQUE (user_id, product_id)
);

-- Create indexes for better performance
CREATE INDEX idx_product_favorites_user_id ON product_favorites(user_id);
CREATE INDEX idx_product_favorites_product_id ON product_favorites(product_id);
CREATE INDEX idx_product_favorites_created_at ON product_favorites(created_at);