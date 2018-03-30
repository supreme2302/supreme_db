package api.db.DAO;


import api.db.Models.Post;
import api.db.Models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        //TODO:: ВРЕМЯ
//        List<Integer> resId = new ArrayList<>();
//        List<Post> res = new ArrayList<>();
       // try {
            for (Post post : posts) {
                post.setForum(thread.getForum());
                post.setThread((long) thread.getId());
                post.setCreated(posts.get(0).getCreated());
                System.out.println("ttt  " + posts.get(0));
                post.setForum(thread.getForum());

                System.out.println("AAA  -  " + post.getParent());
                Post buff = getPostById(post.getParent());

                System.out.println("AAA  -  " + buff);
//                if ((buff == null && post.getParent() != 0) ||
//                        (buff != null &&
//                        !buff.getThread().equals(post.getThread()))) {
//                    return 409;
//                }
                System.out.println("BBB");

                String sql = "INSERT INTO posts (author, message, parent, created) " +
                        "VALUES (?, ?, ?, ?::timestamptz) RETURNING id";


                Integer id = jdbc.queryForObject(sql, new Object[] {post.getAuthor(),
                        post.getMessage(), post.getParent(), post.getCreated()},
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






    private static final class PostMapper implements RowMapper<Post> {
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            Post post = new Post();
            post.setAuthor(rs.getString("author"));
            post.setCreated(rs.getString("created"));
            post.setEdited(rs.getBoolean("isedited"));
            post.setForum(rs.getString("forum"));
            post.setId(rs.getLong("id"));
            post.setMessage(rs.getString("message"));
            post.setParent(rs.getLong("parent"));
            post.setThread(rs.getLong("thread"));
            return post;
        }
    }
}
