CREATE TABLE users
(
  id            SERIAL PRIMARY KEY,
  email         VARCHAR(100) UNIQUE      NOT NULL,
  password_hash VARCHAR(255)             NOT NULL,
  active        BOOLEAN     DEFAULT TRUE NOT NULL,
  last_login    TIMESTAMPTZ,
  created_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
