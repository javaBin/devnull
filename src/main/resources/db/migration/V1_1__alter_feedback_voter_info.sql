ALTER TABLE feedback ADD COLUMN voter_id VARCHAR(256) NOT NULL DEFAULT 'unknown';

ALTER TABLE feedback ADD COLUMN ip_address VARCHAR(46) NOT NULL DEFAULT 'unknown';

ALTER TABLE feedback RENAME COLUMN source TO client_info;