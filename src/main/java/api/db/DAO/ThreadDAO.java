package api.db.DAO;


import api.db.Models.Forum;
import api.db.Models.Thread;
import api.db.Models.User;
import api.db.Models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ThreadDAO {

    private JdbcTemplate jdbc;
    private static ThreadMapper threadMapper = new ThreadMapper();

    @Autowired
    ThreadDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createThread(Thread body, UserDAO userDAO) {
        String sql = "INSERT INTO threads (author, created, message, title, " +
                "forum, forumid, slug, votes) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
//        jdbc.update(sql, body.getAuthor(), body.getCreated(), body.getMessage(), body.getTitle(),
//                body.getForum(), body.getSlug(), body.getVotes());
        final int id = jdbc.queryForObject(sql, new Object[] {body.getAuthor(), body.getCreated(),
                body.getMessage(), body.getTitle(), body.getForum(), body.getForumid(), body.getSlug(), body.getVotes()} ,
                Integer.class);
        body.setId(id);


        //Возможно, для скорости следует заменить на инкремент
//        String sql_for_forums = "UPDATE forums SET threads = (" +
//                "  SELECT count(*) FROM threads" +
//                "  WHERE forum = (?)" +
//                ")" +
//                "FROM threads WHERE forums.slug = (?)";
//        jdbc.update(sql_for_forums, body.getForum(), body.getForum());
        User threadAuthor = userDAO.getProfileUser(body.getAuthor());

            String updateAllUsers = "INSERT INTO \"allUsers\"(about, fullname, email, nickname, forum) VALUES (?,?,?,?,?) ON CONFLICT (forum, nickname) DO NOTHING ";
            jdbc.update(updateAllUsers, threadAuthor.getAbout(), threadAuthor.getFullname(), threadAuthor.getEmail(), threadAuthor.getNickname(), body.getForum());


        String sql_for_forums = "UPDATE forums SET threads = threads + 1 WHERE forums.slug = (?)";
        jdbc.update(sql_for_forums, body.getForum());
    }

    public Thread getThreadBySlug(String slug) {
        String sql = "SELECT * FROM threads WHERE lower(slug)=lower(?)";
        try {
            return jdbc.queryForObject(sql, threadMapper, slug);
        }
        catch (EmptyResultDataAccessException error) {
            return null;
        }

    }
    public Thread getThreadById(Integer id) {
        String sql = "SELECT * FROM threads WHERE id = (?)";
        try {
            return jdbc.queryForObject(sql, threadMapper, id);
        }
        catch (EmptyResultDataAccessException error) {
            return null;
        }

    }
    public Thread getThreadByIdLong(Long id) {
        String sql = "SELECT * FROM threads WHERE id = (?)";
        return jdbc.queryForObject(sql, threadMapper, id);
    }

    public void changeThread(Thread thread) {
        String sql = "UPDATE threads SET message = COALESCE(?, message), title = COALESCE(?, title) WHERE id = ?";
        jdbc.update(sql, thread.getMessage(), thread.getTitle(), thread.getId());
    }


    public List<Thread> getAllThreadsOfForum(Forum forum, Integer limit, String since, Boolean desc) {
        List<Object> insertionArr = new ArrayList<>();
        String sql = "SELECT * FROM threads WHERE forumid = (?)";
        insertionArr.add(forum.getId());

        if (since != null) {
            if (desc != null && desc) {
                sql += " AND created <= (?) :: timestamptz " +
                        "ORDER BY created DESC";
            }
            else {
                sql += " AND created >= (?) :: timestamptz " +
                        "ORDER BY created";
            }
            insertionArr.add(since);
        }
        else {
            sql += " ORDER BY created";
            if (desc != null && desc) {
                sql += " DESC";
            }
        }
        if (limit != null) {
            sql += " LIMIT (?)";
        }
        insertionArr.add(limit);


        return jdbc.query(sql, insertionArr.toArray(), threadMapper);

    }

    public void createVote(Vote vote) {
        String sql = "INSERT INTO votes (nickname, voice, threadid) VALUES (?, ?, ?)";
        jdbc.update(sql, vote.getNickname(), vote.getVoice(),
        vote.getThreadid());

        String update_sql = "UPDATE threads SET votes = votes + ? WHERE id=(?)";
        jdbc.update(update_sql, vote.getVoice(), vote.getThreadid());

    }


    public void updateVote(Vote vote, Vote existVote, int newVote) {
        String sql = "UPDATE votes SET voice = ? WHERE id = (?)";
        String update_sql = "UPDATE threads SET votes = votes + ? WHERE id=(?)";

        jdbc.update(sql, vote.getVoice(), existVote.getId());
        jdbc.update(update_sql, newVote, existVote.getThreadid());
    }



    public Vote getVoteByNick(String nickname, Integer threadId) {
        String sql = "SELECT * FROM votes WHERE lower(nickname) = lower(?) AND threadid = (?)";
        try {
            return jdbc.queryForObject(sql, new RowMapper<Vote>() {
                @Override
                public Vote mapRow(ResultSet resultSet, int i) throws SQLException {
                    return new Vote(resultSet.getInt("id"),
                            resultSet.getString("nickname"),
                            resultSet.getInt("voice"),
                            resultSet.getInt("threadid"));
                }
            }, nickname, threadId);
        }
        catch (EmptyResultDataAccessException error) {
            return null;
        }

    }

    public Integer getAllThreads() {
        String sql = "SELECT count(*) FROM threads";
        return jdbc.queryForObject(sql, Integer.class);
    }


    private static class ThreadMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            Thread thread = new Thread();
            thread.setAuthor(rs.getString("author"));
            thread.setCreated(rs.getTimestamp("created"));
            thread.setForum(rs.getString("forum"));
            thread.setId(rs.getInt("id"));
            thread.setMessage(rs.getString("message"));
            thread.setSlug(rs.getString("slug"));
            thread.setTitle(rs.getString("title"));
            thread.setVotes(rs.getInt("votes"));
            thread.setForumid(rs.getInt("forumid"));
            return thread;
        }

    }
}
