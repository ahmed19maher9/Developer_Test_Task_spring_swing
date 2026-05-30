-- SQL Script for Customer Table Creation
-- This script creates the customers table for different database systems

-- For MySQL
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- For MS SQL Server
-- CREATE TABLE customers (
--     id INT IDENTITY(1,1) PRIMARY KEY,
--     name VARCHAR(100) NOT NULL,
--     email VARCHAR(100) NOT NULL UNIQUE,
--     phone VARCHAR(50),
--     created_at DATETIME DEFAULT GETDATE()
-- );

-- For SQLite
-- CREATE TABLE IF NOT EXISTS customers (
--     id INTEGER PRIMARY KEY AUTOINCREMENT,
--     name TEXT NOT NULL,
--     email TEXT NOT NULL UNIQUE,
--     phone TEXT,
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP
-- );

-- For PostgreSQL
-- CREATE TABLE IF NOT EXISTS customers (
--     id SERIAL PRIMARY KEY,
--     name VARCHAR(100) NOT NULL,
--     email VARCHAR(100) NOT NULL UNIQUE,
--     phone VARCHAR(50),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );
