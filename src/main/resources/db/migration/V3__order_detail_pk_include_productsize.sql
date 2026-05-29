-- =====================================================
-- V3: Fix CRITICAL — OrderDetail composite PK thiếu product_size_id
-- Trước đây PK = (order_id, product_id) → 1 đơn không lưu được 2 size cùng product
-- Sau khi sửa: PK = (order_id, product_id, product_size_id)
-- =====================================================

-- Drop PK cũ
ALTER TABLE order_detail DROP PRIMARY KEY;

-- Bảo đảm product_size_id NOT NULL (đã set ở entity)
ALTER TABLE order_detail MODIFY COLUMN product_size_id INT NOT NULL;

-- Add PK mới gồm 3 cột
ALTER TABLE order_detail ADD PRIMARY KEY (order_id, product_id, product_size_id);
