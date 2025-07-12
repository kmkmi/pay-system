
-- V1__Initial_Schema.sql

-- Create Payment Table
-- This table holds the master record for each payment attempt.
CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL COMMENT 'Unique order ID provided by the client, used for idempotency',
    amount DECIMAL(19, 4) NOT NULL COMMENT 'Payment amount',
    currency VARCHAR(10) NOT NULL COMMENT 'Currency of the payment (e.g., KRW, USD)',
    payment_method VARCHAR(50) COMMENT 'Payment method used (e.g., CARD, BANK_TRANSFER)',
    status VARCHAR(20) NOT NULL COMMENT 'Current status of the payment (e.g., REQUESTED, SUCCESS, FAILED, CANCELLED)',
    pg_transaction_id VARCHAR(255) COMMENT 'Transaction ID from the Payment Gateway',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_id UNIQUE (order_id)
) COMMENT 'Payment master table';

-- Create Transaction Table
-- This table acts as an audit log for all actions related to a payment.
CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL COMMENT 'Foreign key referencing the payment table',
    transaction_type VARCHAR(20) NOT NULL COMMENT 'Type of transaction (e.g., PAYMENT, CANCEL)',
    status VARCHAR(20) NOT NULL COMMENT 'Result of the transaction (e.g., SUCCESS, FAILED)',
    amount DECIMAL(19, 4) NOT NULL COMMENT 'Transaction amount',
    pg_response TEXT COMMENT 'Raw response from the Payment Gateway (e.g., in JSON format)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payment(id)
) COMMENT 'Payment transaction log table';

