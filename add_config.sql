-- Add missing MAX_REFRESH_TOKEN_PER_USER configuration
INSERT OR IGNORE INTO Configuration (config_key, config_value) 
VALUES ('MAX_REFRESH_TOKEN_PER_USER', '5');

-- Add missing REFRESH_CLEAN_UP_INTERVAL configuration if it doesn't exist
INSERT OR IGNORE INTO Configuration (config_key, config_value) 
VALUES ('REFRESH_CLEAN_UP_INTERVAL', '86400000');

-- Verify the configurations were added
SELECT * FROM Configuration WHERE config_key IN ('MAX_REFRESH_TOKEN_PER_USER', 'REFRESH_CLEAN_UP_INTERVAL');