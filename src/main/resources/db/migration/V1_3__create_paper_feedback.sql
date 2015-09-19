CREATE TABLE paper_feedback (
  id         SERIAL PRIMARY KEY,
  created    TIMESTAMP NOT NULL,
  event_id   UUID      NOT NULL,
  session_id UUID      NOT NULL,
  green      INTEGER   NOT NULL,
  yellow     INTEGER   NOT NULL,
  red        INTEGER   NOT NULL,
  UNIQUE (event_id, session_id)
);