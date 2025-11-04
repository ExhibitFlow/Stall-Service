CREATE TABLE stall (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    size VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_size CHECK (size IN ('SMALL', 'MEDIUM', 'LARGE')),
    CONSTRAINT chk_status CHECK (status IN ('AVAILABLE', 'HELD', 'RESERVED'))
);

CREATE INDEX idx_stall_status ON stall(status);
CREATE INDEX idx_stall_code ON stall(code);
