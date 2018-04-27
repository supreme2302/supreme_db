package api.db.DAO;


import api.db.Models.Post;
import api.db.Models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

@Service
@Transactional
public class PostDAO {

    private JdbcTemplate jdbc;
    private Connection connection;

    @Autowired
    public PostDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static PostMapper postMapper = new PostMapper();

    public Integer createPost (List<Post> posts, Thread thread, UserDAO userDAO) {
        int count = 0;

        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/docker",
                    "docker",
                    "docker"
            );
            String sql = "INSERT INTO posts (author, message, parent, created, thread, forum) " +
                    "VALUES (?, ?, ?, ?::timestamptz, ?, ?)";


            try (PreparedStatement ps = connection.prepareStatement(sql,
                    Statement.NO_GENERATED_KEYS)) {
                for (Post post : posts) {
                    post.setForum(thread.getForum());
                    post.setThread((long) thread.getId());
                    post.setCreated(posts.get(0).getCreated());

                    Post parentPost = getPostById(post.getParent());

                    if (userDAO.getProfileUser(post.getAuthor()) == null) {
                        return 404;
                    }

                    if (parentPost == null && post.getParent() != 0 ||
                            (parentPost != null && !parentPost.getThread().equals(post.getThread()))) {
                        return 409;
                    }
                    ps.setString(1, post.getAuthor());
                    ps.setString(2, post.getMessage());
                    ps.setLong(3, post.getParent());
                    ps.setString(4, post.getCreated());
                    ps.setLong(5, thread.getId());
                    ps.setString(6, thread.getForum());
                    ps.addBatch();
                    Long id = jdbc.queryForObject("SELECT nextval('posts_id_seq')", Long.class);
                    post.setId(id);

//                Integer id = jdbc.queryForObject(sql, new Object[] {post.getAuthor(),
//                        post.getMessage(), post.getParent(), post.getCreated(),
//                                thread.getId(), thread.getForum()},
//                        Integer.class);
//                post.setId((long)id);
                    setPathOfPost(parentPost, post);
                    ++count;
                }
                ps.executeBatch();
                connection.close();
            } catch (Exception error) {
                error.printStackTrace();
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        String updateForumSql = "UPDATE forums SET posts = posts + ? WHERE slug = ?";
        System.out.println("count:  -  " + count);
        jdbc.update(updateForumSql, posts.size(), thread.getForum());
        return 201;
    }



    private void setPathOfPost(Post parent, Post body) {
        String sql = "UPDATE posts SET path = ? WHERE id = ?";
        jdbc.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (body.getParent() == 0) {
                Array arrayPath = connection.createArrayOf("int",
                        new Object[]{body.getId()});
                preparedStatement.setArray(1, arrayPath);
                preparedStatement.setLong(2, body.getId());
            }
            else {
                ArrayList innerArr = new ArrayList<>(Arrays.asList(parent.getPath()));
                innerArr.add(body.getId());
                Array innerArrPath = connection.createArrayOf("int",
                        innerArr.toArray());
                preparedStatement.setArray(1, innerArrPath);
                preparedStatement.setLong(2, body.getId());
            }
            return preparedStatement;
        });

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


    public List<Post> getPostsOfThreadByFlat(Thread thread, Integer limit, Integer since,
                                             Boolean desc) {

        List<Object> insertionArr = new ArrayList<>();
        desc = (desc != null) && desc;
        String sql = "SELECT * FROM posts WHERE thread = (?)";
        insertionArr.add(thread.getId());
        if (since == null) {
            if (desc) {
                sql += " ORDER BY created::timestamptz DESC, id DESC ";
            }
            else {
                sql += " ORDER BY created::timestamptz, id ";
            }

        }
        else {
            if (desc) {
                sql += " AND id < (?) ORDER BY created::timestamptz DESC, id DESC ";
            }
            else {
                sql += " AND id > (?) ORDER BY created::timestamptz, id ";
            }
            insertionArr.add(since);
        }
        if (limit != null) {
            sql += " LIMIT (?) ";
            insertionArr.add(limit);
        }
        return jdbc.query(sql, insertionArr.toArray(), postMapper);


    }
    public List<Post> getPostsOfThreadByTree(Thread thread, Integer limit, Integer since,
                                             Boolean desc) {
        List<Object> insertionArr = new ArrayList<>();
        desc = (desc != null) && desc;
        String sql = "SELECT * FROM posts WHERE thread = (?)";
        insertionArr.add(thread.getId());

        if (since == null) {
            if (desc) {
                sql += " ORDER BY path DESC, id DESC";
            }
            else {
                sql += " ORDER BY path, id";
            }
        }
        else {
            if (desc) {
                sql += " AND path < (SELECT path FROM posts WHERE id = (?))" +
                        "  ORDER BY path DESC, id DESC";
            }
            else {
                sql += " AND path > (SELECT path FROM posts WHERE id = (?)) " +
                        "  ORDER BY path, id";
            }
            insertionArr.add(since);
        }

        //возможно не нужна
        sql += ", created::timestamptz, id";
        if (limit != null) {
            sql += " LIMIT (?)";
            insertionArr.add(limit);
        }
            return jdbc.query(sql, insertionArr.toArray(), postMapper);
    }
    public List<Post> getPostsOfThreadByParent(Thread thread, Integer limit, Integer since,
                                         Boolean desc) {
        List<Object> insertionArr = new ArrayList<>();

        String sql = "SELECT * FROM posts JOIN ";
        desc = (desc != null) && desc;
        if (since == null) {
            if (desc) {
                if (limit != null) {
                    sql += " (SELECT id FROM posts WHERE parent = 0 AND thread = ? " +
                            " ORDER BY path desc LIMIT ? ) AS selected ON (selected.id = path[1] AND thread = ?) ORDER BY path[1] DESC, path";
                    insertionArr.add(thread.getId());
                    insertionArr.add(limit);
                    insertionArr.add(thread.getId());
                }
            }
            else {
                if (limit != null) {
                    sql += " (SELECT id FROM posts WHERE parent = 0 AND thread = ? " +
                            " ORDER BY id LIMIT ? ) AS selected ON (thread = ? AND selected.id = path[1]) ORDER BY path";
                    insertionArr.add(thread.getId());
                    insertionArr.add(limit);
                    insertionArr.add(thread.getId());
                }
            }
        }
        else {
            if (desc) {
                if (limit != null) {
                    sql += "  (SELECT id FROM posts WHERE parent = 0 AND thread = ?" +
                            "AND path[1] < (SELECT path[1] FROM posts WHERE id = ?)" +
                            "ORDER BY path DESC, thread DESC LIMIT ?)" +
                            "AS selected ON (thread = ? AND selected.id = path[1])" +
                            "ORDER BY path[1] DESC, path";
                    insertionArr.add(thread.getId());
                    insertionArr.add(since);
                    insertionArr.add(limit);
                    insertionArr.add(thread.getId());
                }
            }
            else {
                if (limit != null) {
                    sql += "  (SELECT id FROM posts WHERE parent = 0 AND thread = ?" +
                            "AND path > (SELECT path FROM posts WHERE id = ?) ORDER BY id LIMIT ?)" +
                            "AS selected ON (thread = ? AND selected.id = path[1]) ORDER BY path";
                    insertionArr.add(thread.getId());
                    insertionArr.add(since);
                    insertionArr.add(limit);
                    insertionArr.add(thread.getId());
                }
            }
        }

        return jdbc.query(sql, insertionArr.toArray(), postMapper);
    }

    public void changePost(Post post) {
        //TODO:: coalesce
        String sql = "UPDATE posts SET message = COALESCE(?, message), " +
                "isedited = COALESCE(true, isedited) WHERE id = ?";
        try {
            jdbc.update(sql, post.getMessage(), post.getId());
        }
        catch (DuplicateKeyException error) {
            System.out.println("something goes wrong");
        }

    }

    public Integer getAllPosts() {
        String sql = "SELECT count(*) FROM posts";
        return jdbc.queryForObject(sql, Integer.class);
    }






    private static final class PostMapper implements RowMapper<Post> {
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            String author = rs.getString("author");
            Timestamp created = rs.getTimestamp("created");
            Boolean isEdited = rs.getBoolean("isEdited");
            String forum = rs.getString("forum");
            Long id = rs.getLong("id");
            String message = rs.getString("message");
            Long parent = rs.getLong("parent");
            Long thread = rs.getLong("thread");
            Array path = rs.getArray("path");
            return new Post(id, author, forum, message, created, isEdited,
                    parent, thread, (Object[])path.getArray());
        }
    }
}
