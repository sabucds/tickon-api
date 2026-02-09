ALTER TABLE users
  ADD COLUMN email_verified BOOLEAN;
UPDATE users
  SET email_verified = FALSE
  WHERE email_verified IS NULL;
ALTER TABLE users
  ALTER COLUMN email_verified SET NOT NULL;
ALTER TABLE users
  ADD COLUMN status VARCHAR(50);
UPDATE users
  SET status = 'ACTIVE'
  WHERE status IS NULL;
ALTER TABLE users
  ALTER COLUMN status SET NOT NULL;