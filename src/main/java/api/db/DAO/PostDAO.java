package api.db.DAO;


import api.db.Models.Post;
import api.db.Models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.StyledEditorKit;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class PostDAO {

    private JdbcTemplate jdbc;

    @Autowired
    public PostDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static PostMapper postMapper = new PostMapper();

    public Integer createPost (List<Post> posts, Thread thread) {
        //TODO:: фиксить
//        List<Integer> resId = new ArrayList<>();
//        List<Post> res = new ArrayList<>();
       // try {
            for (Post post : posts) {
                post.setForum(thread.getForum());
                post.setThread((long) thread.getId());
                post.setCreated(posts.get(0).getCreated());

                post.setForum(thread.getForum());


                Post buff = getPostById(post.getParent());


//                if ((buff == null && post.getParent() != 0) ||
//                        (buff != null &&
//                        !buff.getThread().equals(post.getThread()))) {
//                    return 409;
//                }


                String sql = "INSERT INTO posts (author, message, parent, created, thread, forum) " +
                        "VALUES (?, ?, ?, ?::timestamptz, ?, ?) RETURNING id";


                Integer id = jdbc.queryForObject(sql, new Object[] {post.getAuthor(),
                        post.getMessage(), post.getParent(), post.getCreated(),
                                thread.getId(), thread.getForum()},
                        Integer.class);
                post.setId((long)id);
            }
            return 201;
        //}
//        catch (Exception error) {
//            return 404;
//        }
    }

    public Post getPostById (Long id) {
        try {
            String sql = "SELECT * FROM posts WHERE id = (?)";
            return jdbc.queryForObject(sql, postMapper, id);
        }
        catch (DataAccessException error) {
            return null;
        }

    }

    public List<Post> getPostsOfThread(Thread thread, Integer limit, Integer since,
                                       String sort, Boolean desc) {
        if (sort.equals("flat")) {
            List<Object> insertionArr = new ArrayList<>();
            String sql = "SELECT * FROM posts WHERE thread = (?)";
            insertionArr.add(thread.getId());

            if (since != null) {
                sql += " AND id > (?)";
                insertionArr.add(since);
            }
//            sql += (sort.equals("flat")) ? " ORDER BY created" : " ORDER BY parent, created";
            sql += " ORDER BY created";
            if (desc != null && desc) {
                sql += " DESC";
            }
            if (limit != null) {
                sql += " LIMIT (?)";
                insertionArr.add(limit);
            }
            return jdbc.query(sql, insertionArr.toArray(), postMapper);
        }
        return null;
    }






    private static final class PostMapper implements RowMapper<Post> {
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            String author = rs.getString("author");
            Timestamp created = rs.getTimestamp("created");
            Boolean isEdited = rs.getBoolean("isedited");
            String forum = rs.getString("forum");
            Long id = rs.getLong("id");
            String message = rs.getString("message");
            Long parent = rs.getLong("parent");
            Long thread = rs.getLong("thread");
            return new Post(id, author, forum, message, created, isEdited, parent, thread);
        }
    }
}
