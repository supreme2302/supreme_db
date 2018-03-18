CREATE EXTENSION IF NOT EXISTS CITEXT;

DROP TABLE if EXISTS users;
DROP TABLE if EXISTS votes;
DROP TABLE if EXISTS posts;
DROP TABLE if EXISTS threads;
DROP TABLE if EXISTS forums;

CREATE TABLE IF NOT EXISTS "users" (
  id SERIAL NOT NULL PRIMARY KEY,
  nickname citext NOT NULL UNIQUE,
  fullname TEXT,
  email CITEXT NOT NULL UNIQUE,
  about TEXT
);

CREATE TABLE IF NOT EXISTS "forums" (
  id SERIAL NOT NULL PRIMARY KEY,
  posts BIGINT DEFAULT 0,
  slug CITEXT NOT NULL UNIQUE,
  threads INTEGER DEFAULT 0,
  title TEXT NOT NULL,
  author citext NOT NULL, --вместо user, т.к. user - зарезервировано
  FOREIGN KEY (author) REFERENCES "users"(nickname)
);


CREATE TABLE IF NOT EXISTS "threads" (
  id SERIAL NOT NULL PRIMARY KEY,
  author citext NOT NULL,
  created TIMESTAMP WITH TIME ZONE,
  forum citext,
  message TEXT NOT NULL,
  slug citext,
  title TEXT NOT NULL,
  votes INTEGER,
  FOREIGN KEY (author) REFERENCES "users" (nickname),
  FOREIGN KEY (forum) REFERENCES "forums" (slug)
);

CREATE TABLE IF NOT EXISTS "posts" (
  id SERIAL NOT NULL PRIMARY KEY,
  author TEXT NOT NULL,
  created TIMESTAMP WITH TIME ZONE,
  forum CITEXT,
  isEdited BOOLEAN NOT NULL,
  message TEXT NOT NULL,
  parent BIGINT,
  thread INTEGER REFERENCES "threads" (id)
);

CREATE TABLE IF NOT EXISTS "votes" (
  id SERIAL PRIMARY KEY,

  nickname citext NOT NULL ,
  voice INTEGER NOT NULL,
  threadid INTEGER REFERENCES threads(id),
  FOREIGN KEY (nickname) REFERENCES "users" (nickname)
);