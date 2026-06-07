-- V2__Add_User_Role.sql
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER';

-- Seed an admin user if not exists (password is 'Admin@1234')
-- The hash below is for 'Admin@1234' using BCrypt
INSERT INTO users (email, password_hash, display_name, role, is_active)
SELECT 'admin@messageforge.local', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07Xd00WgpBgS3zpqS.', 'System Admin', 'ADMIN', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@messageforge.local');
