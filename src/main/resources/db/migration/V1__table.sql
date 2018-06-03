CREATE EXTENSION IF NOT EXISTS CITEXT;

DROP TABLE IF EXISTS "allUsers" CASCADE;
DROP TABLE if EXISTS posts CASCADE;
DROP TABLE if EXISTS votes CASCADE;
DROP TABLE if EXISTS threads CASCADE;
DROP TABLE if EXISTS forums CASCADE;
DROP TABLE if EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS "users" (
  id SERIAL NOT NULL PRIMARY KEY,
  nickname citext COLLATE "ucs_basic" NOT NULL,
  fullname TEXT,
  email CITEXT NOT NULL,
  about TEXT
);

DROP INDEX IF EXISTS usersNicknameIx;
CREATE UNIQUE INDEX usersNicknameIx on users(nickname);

DROP INDEX IF EXISTS usersEmailIx;
CREATE UNIQUE INDEX usersEmailIx on users(email);

DROP INDEX IF EXISTS lowNickUsersIx;
CREATE INDEX lowNickUsersIx on users(lower(nickname));

DROP INDEX IF EXISTS lowEmailUsersIx;
CREATE INDEX lowEmailUsersIx on users(lower(email));


CREATE TABLE IF NOT EXISTS "forums" (
  id SERIAL NOT NULL PRIMARY KEY,
  posts BIGINT DEFAULT 0,
  slug CITEXT NOT NULL,
  threads INTEGER DEFAULT 0,
  title TEXT NOT NULL,
  author citext NOT NULL, --вместо user, т.к. user - зарезервировано
  FOREIGN KEY (author) REFERENCES "users"(nickname)
);

DROP INDEX IF EXISTS slugForumIx;
CREATE UNIQUE INDEX slugForumIx on forums(slug);

DROP INDEX IF EXISTS lowSlugForumIx;
CREATE INDEX lowSlugForumIx on forums(lower(slug));


CREATE TABLE IF NOT EXISTS "threads" (
  id SERIAL NOT NULL PRIMARY KEY,
  author citext COLLATE "ucs_basic" NOT NULL,
  created TIMESTAMP WITH TIME ZONE,
  forum citext,
  forumid INTEGER,
  message TEXT NOT NULL,
  slug citext,
  title TEXT NOT NULL,
  votes INTEGER,
  FOREIGN KEY (author) REFERENCES "users" (nickname),
  FOREIGN KEY (forum) REFERENCES "forums" (slug)
);

DROP INDEX IF EXISTS slugThreadIx;
CREATE UNIQUE INDEX slugThreadIx on threads(slug);

DROP INDEX IF EXISTS lowSlugThreadIx;
CREATE INDEX lowSlugThreadIx on threads(lower(slug));

DROP INDEX IF EXISTS lowSlugThreadIx;
CREATE INDEX onT on threads(forumid, created);


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

DROP INDEX IF EXISTS flatTwoIx;
CREATE INDEX flatTwoIx on posts(thread, created, id);

drop index if EXISTS sortPostsTree;
CREATE INDEX sortPostsTree ON posts(thread, path, id);

DROP INDEX IF EXISTS comlicatedParentIx;
CREATE INDEX comlicatedParentIx on posts(thread, parent, (path[1]), path);

DROP INDEX IF EXISTS parentIx;
CREATE INDEX parentIx on posts(thread, parent, path, (path[1]));


CREATE TABLE IF NOT EXISTS "votes" (
  id SERIAL PRIMARY KEY,
  nickname citext NOT NULL ,
  voice INTEGER NOT NULL,
  threadid INTEGER REFERENCES "threads" (id),
  FOREIGN KEY (nickname) REFERENCES "users" (nickname)
);

DROP INDEX IF EXISTS votesIx;
CREATE INDEX votesIx on votes(threadid, lower(nickname));


CREATE TABLE IF NOT EXISTS "allUsers" (
  nickname citext COLLATE "ucs_basic" NOT NULL,
  fullname TEXT,
  email CITEXT NOT NULL ,
  about citext,
  forumid INTEGER
);

drop INDEX IF EXISTS allUsersIx;
CREATE UNIQUE INDEX allUsersIx on "allUsers"(forumid, nickname);
DROP INDEX IF EXISTS uniqueForum;
CREATE INDEX uniqueForum on "allUsers"(forumid);
-- CLUSTER "allUsers" USING allUsersIx;
CLUSTER "allUsers" USING uniqueForum;
DROP INDEX IF EXISTS lowAllUsersNickIx;
CREATE INDEX lowAllUsersNickIx on "allUsers"(lower(nickname));

DROP INDEX IF EXISTS usualAllusersIx;
CREATE INDEX usualAllusersIx on "allUsers"(forumid, lower(nickname));








