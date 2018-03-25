package api.db.DAO;


import api.db.Models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;

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
                "forum, slug, votes) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
//        jdbc.update(sql, body.getAuthor(), body.getCreated(), body.getMessage(), body.getTitle(),
//                body.getForum(), body.getSlug(), body.getVotes());
        final int id = jdbc.queryForObject(sql, new Object[] {body.getAuthor(), body.getCreated(),
                body.getMessage(), body.getTitle(), body.getForum(), body.getSlug(), body.getVotes()} ,
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



    private static class ThreadMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            Thread thread = new Thread();
            thread.setAuthor(rs.getString("author"));
            thread.setCreated(rs.getDate("created"));
            thread.setForum(rs.getString("forum"));
            thread.setId(rs.getInt("id"));
            thread.setMessage(rs.getString("message"));
            thread.setSlug(rs.getString("slug"));
            thread.setTitle(rs.getString("title"));
            thread.setVotes(rs.getInt("votes"));
            return thread;
        }

    }
}
