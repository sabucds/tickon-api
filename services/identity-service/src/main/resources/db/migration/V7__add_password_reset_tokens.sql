-- Password reset tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id UUID PRIMARY KEY,
  token_hash TEXT NOT NULL UNIQUE,
  user_id UUID NOT NULL REFERENCES users(id),
  email CITEXT NOT NULL,
  absolute_expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for lookups by token hash (most common query)
CREATE UNIQUE INDEX idx_password_reset_tokens_token_hash
  ON password_reset_tokens(token_hash);

-- Index for user_id (cleanup queries, invalidation)
CREATE INDEX idx_password_reset_tokens_user_id
  ON password_reset_tokens(user_id);

-- Index for created_at (cleanup old tokens)
CREATE INDEX idx_password_reset_tokens_created_at
  ON password_reset_tokens(created_at);

-- Partial index for active tokens (not used, not expired)
CREATE INDEX idx_password_reset_tokens_active
  ON password_reset_tokens(user_id, absolute_expires_at)
  WHERE used_at IS NULL;
