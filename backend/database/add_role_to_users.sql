-- Migration: Add role column to users table
-- Purpose: Enable role-based access control (RBAC)

-- Add role column with ENUM type
ALTER TABLE users 
ADD COLUMN role ENUM('student', 'staff', 'admin') NOT NULL DEFAULT 'student'
AFTER email;

-- Add index for faster role-based queries
CREATE INDEX idx_users_role ON users(role);

-- Optional: Update existing users if needed
-- UPDATE users SET role = 'admin' WHERE email = 'admin@example.com';
-- UPDATE users SET role = 'staff' WHERE email LIKE '%staff%';
