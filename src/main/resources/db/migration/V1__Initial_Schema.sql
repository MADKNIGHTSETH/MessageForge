-- V1__Initial_Schema.sql
-- MessageForge Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Messages table
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    raw_content TEXT NOT NULL,
    title VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB DEFAULT '{}',
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_messages_user_id ON messages(user_id);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- Channel Messages (formatted versions for each channel)
CREATE TABLE channel_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    channel_type VARCHAR(50) NOT NULL,
    formatted_content TEXT NOT NULL,
    applied_decorators JSONB DEFAULT '[]',
    status VARCHAR(30) DEFAULT 'PENDING',
    sent_at TIMESTAMP WITH TIME ZONE,
    external_message_id VARCHAR(255),
    error_message TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_channel_msg_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
);

CREATE INDEX idx_channel_messages_message_id ON channel_messages(message_id);
CREATE INDEX idx_channel_messages_channel_type ON channel_messages(channel_type);
CREATE INDEX idx_channel_messages_status ON channel_messages(status);
CREATE INDEX idx_channel_messages_created_at ON channel_messages(created_at);

-- Channel Integrations (user's configured channels)
CREATE TABLE channel_integrations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel_type VARCHAR(50) NOT NULL,
    is_enabled BOOLEAN DEFAULT false,
    credentials BYTEA,
    settings JSONB DEFAULT '{}',
    last_tested_at TIMESTAMP WITH TIME ZONE,
    test_status VARCHAR(30),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_user_channel UNIQUE(user_id, channel_type)
);

CREATE INDEX idx_channel_integrations_user_id ON channel_integrations(user_id);
CREATE INDEX idx_channel_integrations_enabled ON channel_integrations(is_enabled);

-- Formatter Templates
CREATE TABLE formatter_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    channel_type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    template_body TEXT NOT NULL,
    is_default BOOLEAN DEFAULT false,
    is_system BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_formatter_templates_user_id ON formatter_templates(user_id);
CREATE INDEX idx_formatter_templates_channel_type ON formatter_templates(channel_type);
CREATE INDEX idx_formatter_templates_system ON formatter_templates(is_system);

-- Message Recipients (track who received what message on which channel)
CREATE TABLE message_recipients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    channel_message_id UUID NOT NULL REFERENCES channel_messages(id) ON DELETE CASCADE,
    recipient_address VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(100),
    status VARCHAR(30) DEFAULT 'PENDING',
    delivery_status VARCHAR(30),
    delivery_timestamp TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_recipients_channel_message_id ON message_recipients(channel_message_id);
CREATE INDEX idx_message_recipients_status ON message_recipients(status);

-- Message Attachments
CREATE TABLE message_attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    storage_path TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_attachments_message_id ON message_attachments(message_id);

-- Refresh Tokens for JWT
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT false
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- Audit Log
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(255),
    changes JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Channel Statistics (for dashboard)
CREATE TABLE channel_statistics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel_type VARCHAR(50) NOT NULL,
    messages_sent INT DEFAULT 0,
    messages_failed INT DEFAULT 0,
    last_sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_user_channel_stats UNIQUE(user_id, channel_type)
);

CREATE INDEX idx_channel_statistics_user_id ON channel_statistics(user_id);

-- Create indexes for search optimization
CREATE INDEX idx_messages_user_status ON messages(user_id, status);
CREATE INDEX idx_messages_user_created ON messages(user_id, created_at DESC);
CREATE INDEX idx_channel_messages_channel_status ON channel_messages(channel_type, status);
CREATE INDEX idx_channel_integrations_user_enabled ON channel_integrations(user_id, is_enabled);
