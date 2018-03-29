package api.db.DAO;


import api.db.Models.Forum;
import api.db.Models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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

    public void createThread(Thread body) {
        String sql = "INSERT INTO threads (author, created, message, title, " +
                "forum, forumid, slug, votes) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
//        jdbc.update(sql, body.getAuthor(), body.getCreated(), body.getMessage(), body.getTitle(),
//                body.getForum(), body.getSlug(), body.getVotes());
        final int id = jdbc.queryForObject(sql, new Object[] {body.getAuthor(), body.getCreated(),
                body.getMessage(), body.getTitle(), body.getForum(), body.getForumid(), body.getSlug(), body.getVotes()} ,
                Integer.class);
        body.setId(id);


        //Возможно, для скорости следует заменить на инкремент
        String sql_for_forums = "UPDATE forums SET threads = (" +
                "  SELECT count(*) FROM threads" +
                "  WHERE forum = (?)" +
                ")" +
                "FROM threads WHERE forums.slug = (?)";
        jdbc.update(sql_for_forums, body.getForum(), body.getForum());
    }

    public Thread getThreadBySlug(String slug) {
        String sql = "SELECT * FROM threads WHERE lower(slug)=lower(?)";
        return jdbc.queryForObject(sql, threadMapper, slug);
    }


    public List<Thread> getAllThreadsOfForum(Forum forum, Integer limit, String since, Boolean desc) {
        List<Object> insertionArr = new ArrayList<>();
        String sql = "SELECT * FROM threads WHERE forumid = (?)";
        insertionArr.add(forum.getId());

        //TODO:: переписать так как хотел
        if (since != null) {
            if (desc != null && desc) {
                sql += " AND created <= (?)::timestamptz";
            }
            else {
                sql += " AND created >= (?)::timestamptz";
            }
            insertionArr.add(since);
        }
        sql += " ORDER BY created";
        if (desc != null && desc) {
            sql += " DESC";
        }

        if (limit != null) {
            sql += " LIMIT (?)";
            insertionArr.add(limit);
        }

        return jdbc.query(sql, insertionArr.toArray(), threadMapper);

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
