CREATE EXTENSION IF NOT EXISTS CITEXT;

DROP TABLE IF EXISTS "allUsers" CASCADE;
DROP TABLE if EXISTS posts CASCADE;
DROP TABLE if EXISTS votes CASCADE;
DROP TABLE if EXISTS threads CASCADE;
DROP TABLE if EXISTS forums CASCADE;
DROP TABLE if EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS "users" (
  id SERIAL NOT NULL PRIMARY KEY,
  nickname citext COLLATE "ucs_basic" NOT NULL UNIQUE,
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
  author citext COLLATE "ucs_basic" NOT NULL,
  created TIMESTAMP WITH TIME ZONE,
  forum citext,
  forumid INTEGER,
  message TEXT NOT NULL,
  slug citext UNIQUE ,
  title TEXT NOT NULL,
  votes INTEGER,
  FOREIGN KEY (author) REFERENCES "users" (nickname),
  FOREIGN KEY (forum) REFERENCES "forums" (slug)
);

CREATE TABLE IF NOT EXISTS "posts" (
  id SERIAL NOT NULL PRIMARY KEY,
  author citext COLLATE "ucs_basic" NOT NULL,
  created TIMESTAMP WITH TIME ZONE,
  forum CITEXT REFERENCES "forums" (slug),
  isEdited BOOLEAN,
  message TEXT NOT NULL,
  parent BIGINT DEFAULT 0,
  thread INTEGER REFERENCES "threads" (id),
  path INTEGER[]

);

CREATE TABLE IF NOT EXISTS "votes" (
  id SERIAL PRIMARY KEY,

  nickname citext NOT NULL ,
  voice INTEGER NOT NULL,
  threadid INTEGER REFERENCES "threads" (id),
  FOREIGN KEY (nickname) REFERENCES "users" (nickname)
);

CREATE TABLE IF NOT EXISTS "allUsers" (
  id SERIAL PRIMARY KEY,
  nickname citext COLLATE "ucs_basic" NOT NULL,
  fullname TEXT,
  email CITEXT NOT NULL ,
  about citext,
  forum CITEXT,
  UNIQUE (nickname, forum)
);


drop index if EXISTS sortPostsTree;
drop INDEX IF EXISTS allUsersIx;
DROP INDEX IF EXISTS onT;
DROP INDEX IF EXISTS forumSlugIx;
DROP INDEX IF EXISTS allThreadsAx;
DROP INDEX IF EXISTS votesIx;

DROP INDEX IF EXISTS threadSlugIx;
DROP INDEX if EXISTS flatIx;
CREATE INDEX sortPostsTree ON posts(thread, path, id);
CREATE INDEX allUsersIx on "allUsers"(forum, nickname);
CREATE INDEX onT on threads(forumid, created);
CREATE INDEX forumSlugIx on forums(slug);
CREATE INDEX allThreadsAx on threads(forumid, created);
CREATE INDEX votesIx on votes(nickname, threadid);
CREATE INDEX threadSlugIx on threads(slug);
CREATE INDEX userIx on users(nickname);

CREATE INDEX flatIx on posts(thread, created);




