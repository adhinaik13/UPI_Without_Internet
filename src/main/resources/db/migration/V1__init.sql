CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE,
    vpa VARCHAR(50) NOT NULL UNIQUE,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaction (
    id BIGSERIAL PRIMARY KEY,
    sender_id VARCHAR(50) NOT NULL REFERENCES account(user_id),
    receiver_id VARCHAR(50) NOT NULL REFERENCES account(user_id),
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    packet_hash VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    settled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tx_sender ON transaction(sender_id);
CREATE INDEX idx_tx_receiver ON transaction(receiver_id);
CREATE INDEX idx_tx_status ON transaction(status) WHERE status = 'PENDING';