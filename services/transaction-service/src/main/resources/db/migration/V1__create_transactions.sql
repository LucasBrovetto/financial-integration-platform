CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    terminal_id VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    failure_reason VARCHAR(255)
);

CREATE INDEX idx_transactions_terminal_id ON transactions (terminal_id);
