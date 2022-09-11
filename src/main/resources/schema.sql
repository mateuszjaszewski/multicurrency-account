CREATE TABLE events (
  id UUID PRIMARY KEY,
  account_id VARCHAR(32) NOT NULL,
  payload CLOB NOT NULL
);

CREATE INDEX events_account_id_index ON events (account_id);
