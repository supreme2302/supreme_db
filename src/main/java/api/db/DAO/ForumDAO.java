package api.db.DAO;


import api.db.Models.Forum;
import api.db.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Transactional
public class ForumDAO {
    private JdbcTemplate jdbc;
    @Autowired
    public ForumDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static ForumMapper forumMapper = new ForumMapper();


    public void createForum(Forum forum) {
        String sql = "INSERT INTO forums (slug, title, author) VALUES (?, ?, ?)";
        jdbc.update(sql, forum.getSlug(), forum.getTitle(), forum.getAuthor());
    }

    public Forum existingForum(Forum forum) {
        String sql = "SELECT * from forums where lower(slug) = lower(?)";
        try{
            return jdbc.queryForObject(sql, forumMapper, forum.getSlug());
        }
        catch(EmptyResultDataAccessException error) {
            return null;
        }

    }

    public Forum getForumBySlug(String slug) {
        String sql ="SELECT * FROM forums WHERE lower(slug) = lower(?)";
        try {
            return jdbc.queryForObject(sql, forumMapper, slug);
        }
        catch (EmptyResultDataAccessException error) {
            return null;
        }
    }


    private static final class ForumMapper implements RowMapper<Forum> {
        public Forum mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Forum forum = new Forum();
            forum.setId(rs.getInt("id"));
            forum.setSlug(rs.getString("slug"));
            forum.setAuthor(rs.getString("author"));
            forum.setPosts(rs.getLong("posts"));
            forum.setThreads(rs.getInt("threads"));
            forum.setTitle(rs.getString("title"));
            return forum;
        }
    }

}
