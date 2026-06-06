-- ============================================================================
-- MessageForge - Supabase PostgreSQL DDL (Frontend + Bridge Schema)
-- ============================================================================
-- Architecture Hybride :
-- - Supabase gère USERS (table complète) et MESSAGES (table pont relationelle)
-- - Les détails enrichis (contenu formaté, décorateurs, métadonnées) seront 
--   stockés plus tard dans MongoDB (document par message_id)

-- ============================================================================
-- Table: users
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(255),
  avatar_url TEXT,
  is_active BOOLEAN DEFAULT true,
  role VARCHAR(50) NOT NULL DEFAULT 'user' CHECK (role IN ('user', 'admin')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index sur email pour les connexions rapides
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ============================================================================
-- Table: system_templates
-- ============================================================================
CREATE TABLE IF NOT EXISTS system_templates (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  channel_type VARCHAR(100) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_templates_channel_type ON system_templates(channel_type);

-- ============================================================================
-- Table: audit_logs
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_id UUID NOT NULL,
  action VARCHAR(150) NOT NULL,
  details JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_admin_id ON audit_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- ============================================================================
-- Table: messages
-- ============================================================================
-- Cette table sert de pont relationnel pour Supabase
-- Les détails enrichis (brut, formaté, décorateurs, destinataires) sont 
-- gérés par le frontend (Pinia + localStorage) ou MongoDB
CREATE TABLE IF NOT EXISTS messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index sur user_id pour les requêtes rapides par utilisateur
CREATE INDEX IF NOT EXISTS idx_messages_user_id ON messages(user_id);

-- ============================================================================
-- Trigger pour mettre à jour updated_at sur la table users
-- ============================================================================
CREATE OR REPLACE FUNCTION update_user_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_user_timestamp();

-- ============================================================================
-- Notes d'Architecture :
-- ============================================================================
-- 1. Le frontend (Vue 3 + Pinia) gère localement en mode mock :
--    - État du message actuel (sujet, contenu brut, décorateurs, canaux)
--    - Génération des prévisualisations (Email, SMS, LinkedIn, Slack, X)
--    - Simulation de l'envoi avec progression par canal
--    - Persistance locale via localStorage
--
-- 2. Les tables Supabase (users + messages) serviront de :
--    - Authentification (gestion des utilisateurs)
--    - Pont relationnel (liaison utilisateur <-> message)
--
-- 3. MongoDB (future phase) stockera les documents enrichis :
--    - Contenu brut et formaté
--    - Métadonnées (décorateurs, canaux, statut)
--    - Historique des envois avec statuts par canal
--
-- 4. Flux d'authentification mock actuel :
--    - Pas de backend API
--    - JWT simulé dans localStorage (useAuthStore)
--    - Navigation protégée via route guards
