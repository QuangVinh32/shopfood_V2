-- =====================================================
-- V2: Shipping address book + Payment + Refresh token + Email verify
-- Migration cho ngay 2026-05-29
-- =====================================================

-- 1) Email verification + reset password fields tren users
ALTER TABLE users
    ADD COLUMN email_verified TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN email_verify_token VARCHAR(100) NULL,
    ADD COLUMN email_verify_expires_at DATETIME NULL,
    ADD COLUMN reset_password_token VARCHAR(100) NULL,
    ADD COLUMN reset_password_expires_at DATETIME NULL,
    ADD COLUMN enabled TINYINT(1) NOT NULL DEFAULT 1;

CREATE INDEX idx_users_email_verify_token ON users (email_verify_token);
CREATE INDEX idx_users_reset_password_token ON users (reset_password_token);

-- 2) Refresh token bang rieng
CREATE TABLE refresh_tokens (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    token        VARCHAR(200) NOT NULL UNIQUE,
    user_agent   VARCHAR(255),
    ip_address   VARCHAR(64),
    issued_at    DATETIME NOT NULL,
    expires_at   DATETIME NOT NULL,
    revoked      TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

-- 3) Shipping address book (1 user nhieu dia chi)
CREATE TABLE shipping_addresses (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    address_line  VARCHAR(500) NOT NULL,
    ward          VARCHAR(100),
    district      VARCHAR(100),
    province      VARCHAR(100),
    is_default    TINYINT(1) NOT NULL DEFAULT 0,
    created_at    DATETIME NOT NULL,
    updated_at    DATETIME NOT NULL,
    CONSTRAINT fk_shipping_addresses_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_shipping_addresses_user ON shipping_addresses (user_id);

-- 4) Snapshot dia chi giao + phi ship vao orders
ALTER TABLE orders
    ADD COLUMN receiver_name VARCHAR(100) NULL,
    ADD COLUMN receiver_phone VARCHAR(20) NULL,
    ADD COLUMN shipping_address VARCHAR(500) NULL,
    ADD COLUMN shipping_fee INT NOT NULL DEFAULT 0,
    ADD COLUMN note VARCHAR(500) NULL;

-- 5) Order status history (audit log)
CREATE TABLE order_status_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    INT NOT NULL,
    from_status VARCHAR(20),
    to_status   VARCHAR(20) NOT NULL,
    changed_by  INT NULL,
    changed_at  DATETIME NOT NULL,
    note        VARCHAR(500),
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_osh_user FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

CREATE INDEX idx_osh_order ON order_status_history (order_id);

-- 6) Mo rong payments
ALTER TABLE payments
    ADD COLUMN provider      VARCHAR(20) NULL,
    ADD COLUMN status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN amount        INT NULL,
    ADD COLUMN transaction_id VARCHAR(200) NULL,
    ADD COLUMN gateway_response TEXT NULL,
    ADD COLUMN paid_at       DATETIME NULL,
    ADD COLUMN order_id      INT NULL;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(order_id);

CREATE INDEX idx_payments_order ON payments (order_id);
CREATE INDEX idx_payments_transaction_id ON payments (transaction_id);

-- 7) Idempotency key cho createOrder
CREATE TABLE idempotency_keys (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_value   VARCHAR(100) NOT NULL UNIQUE,
    user_id     INT NOT NULL,
    endpoint    VARCHAR(100) NOT NULL,
    response_json TEXT,
    created_at  DATETIME NOT NULL,
    CONSTRAINT fk_idemp_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);
