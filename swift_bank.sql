CREATE DATABASE swift_bank;
USE swift_bank;

CREATE TABLE accounts (
    account_id INT PRIMARY KEY,
    owner_name VARCHAR(50),
    balance DECIMAL(15, 2) NOT NULL CHECK (balance >= 0)
);

CREATE TABLE transfer_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    from_account INT,
    to_account INT,
    amount DECIMAL(15,2),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed two test accounts
INSERT INTO accounts VALUES (101, 'Alice', 1000.00), (102, 'Bob', 500.00);