alter table users
  add column if not exists is_deleted boolean not null default false,
  add column if not exists deleted_at timestamptz;

alter table users 
  alter column email type citext using email::citext;

alter table users
  alter column username type citext using username::citext;

create unique index if not exists ux_users_email_active
  on users (email)
  where is_deleted = false;

create unique index if not exists ux_users_username_active
  on users (username)
  where is_deleted = false;
