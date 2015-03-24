CREATE TABLE feedback (
  id               SERIAL PRIMARY KEY,
  created          TIMESTAMP    NOT NULL,
  source           VARCHAR(100) NOT NULL,

  session_id       VARCHAR(36)  NOT NULL,
  rating_overall   SMALLINT     NOT NULL,
  rating_relevance SMALLINT,
  rating_content   SMALLINT,
  rating_quality   SMALLINT
);
