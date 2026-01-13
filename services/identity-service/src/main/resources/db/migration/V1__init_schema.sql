create extension if not exists citext;

create table if not exists users (
  id uuid primary key,
  username citext not null,
  email citext not null,
  password_hash text not null,
  first_name text,
  last_name text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  is_deleted boolean not null default false,
  deleted_at timestamptz
);

create table if not exists sessions (
  id uuid primary key,
  refresh_token_hash text not null unique,
  user_id uuid not null references users (id),
  family_id uuid not null,
  device_id text not null,
  rotated_from_session_id uuid,
  expires_at timestamptz not null,
  revoked_at timestamptz,
  revoke_reason text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
