ALTER TABLE sessions
     ALTER COLUMN device_id TYPE TEXT USING device_id::TEXT;