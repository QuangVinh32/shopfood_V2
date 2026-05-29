-- =====================================================
-- V4: Bán đồ uống F&B — đơn giản hóa OrderStatus + thêm COD
-- =====================================================

-- 1) Map các đơn cũ ở status cũ sang status mới
UPDATE orders SET order_status = 'COMPLETED' WHERE order_status = 'DELIVERED';
UPDATE orders SET order_status = 'CANCELED' WHERE order_status = 'RETURNED';

-- 2) Thêm cột payment_method (default COD cho đơn cũ)
ALTER TABLE orders
    ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'COD';

-- 3) Mở rộng cột order_status (an toàn vì các enum mới đều ngắn)
ALTER TABLE orders MODIFY COLUMN order_status VARCHAR(15) NOT NULL;
ALTER TABLE order_status_history MODIFY COLUMN from_status VARCHAR(15);
ALTER TABLE order_status_history MODIFY COLUMN to_status   VARCHAR(15) NOT NULL;
