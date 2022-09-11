CREATE TABLE stored_event (
  id UUID PRIMARY KEY,
  account_id VARCHAR(32) NOT NULL,
  payload CLOB NOT NULL
);

CREATE INDEX stored_event_account_id_index ON stored_event (account_id);
