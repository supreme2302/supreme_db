package api.db.DAO;


import api.db.Models.Post;
import api.db.Models.Thread;
import api.db.Models.User;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Generated;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

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
    public Integer createPost (List<Post> posts, Thread thread, UserDAO userDAO, String created_date) throws SQLException {
        //try {
//            connection = DriverManager.getConnection(
//                    "jdbc:postgresql://localhost:5432/docker",
//                    "docker",
//                    "docker"
//            );
            Connection connection = jdbc.getDataSource().getConnection();
            String sql = "INSERT INTO posts (author, message, parent, created, thread, forum, id, path) " +
                    "VALUES (?, ?, ?, ?::TIMESTAMP WITH TIME ZONE , ?, ?, ?, ?)";
            List<Post> postsArr = new ArrayList<>();
//            try (PreparedStatement ps = connection.prepareStatement(sql,
//                    Statement.NO_GENERATED_KEYS)) {
//                final List<Long> ids = jdbc.queryForList("select nextval('posts_id_seq')" +
//                        " from generate_series(1, ?)", Long.class, posts.size());

        // int i = 0;
        PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.NO_GENERATED_KEYS);
                for (Post post : posts) {
                    post.setForum(thread.getForum());
                    post.setThread((long) thread.getId());
                    post.setCreated(created_date);

                    User postAuthor = userDAO.getProfileUser(post.getAuthor());
                    if (postAuthor == null) {
                        return 404;
                    }

                    Post parentPost = getPostById(post.getParent());
                    postsArr.add(parentPost);
                    if (parentPost == null && post.getParent() != 0 ||
                            (parentPost != null && !parentPost.getThread().equals(post.getThread()))) {
                        return 409;
                    }
                    final Long idNextEntry = jdbc.queryForObject("select nextval('posts_id_seq')",
                            Long.class);
                    post.setId(idNextEntry);
                    ArrayList insertionObj = setIdNextEntry(post, parentPost, idNextEntry);
                    ps.setString(1, post.getAuthor());
                    ps.setString(2, post.getMessage());
                    ps.setLong(3, post.getParent());
                    ps.setString(4, created_date);
                    ps.setLong(5, thread.getId());
                    ps.setString(6, thread.getForum());
                    ps.setLong(7, post.getId());
                    ps.setArray(8, connection.createArrayOf("int", insertionObj.toArray()));
                    ps.addBatch();
                   // ++i;
//                    setPathOfPost(parentPost, post);
                }
                ps.executeBatch();
                connection.close();

//                ResultSet rs = ps.getGeneratedKeys();
//                int i = 0;
//                for (Post post : posts) {
//                    if (rs.next()) {
//                        Long key = rs.getLong(1);
//                        post.setId(key);
//
//                    }
////                    Post parentPost = getPostById(post.getParent());
//
//                    ++i;
//                }


//            } catch (Exception error) {
//                error.printStackTrace();
//            }
//            finally {
//                connection.close();
//            }

//        } catch (Exception error) {
//            error.printStackTrace();
//        }
//        connection.close();

        String updateForumSql = "UPDATE forums SET posts = posts + ? WHERE slug = ?";
        jdbc.update(updateForumSql, posts.size(), thread.getForum());
        return 201;
    }

    private ArrayList setIdNextEntry(Post post, Post parentPost, Long id) {
        if (post.getParent() == 0) {
            ArrayList getPath = new ArrayList<>(Arrays.asList(id));
            return getPath;
        }
        else {
            ArrayList getPath = new ArrayList<>(Arrays.asList(parentPost.getPath()));
            getPath.add(id);
            return getPath;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateAllUsers(List<User> users, String forum, UserDAO userDAO) {
        String updateAllUsers = "INSERT INTO \"allUsers\"(about, fullname, email, nickname, forum) VALUES (?,?,?,?,?)  ON CONFLICT (nickname, forum) DO NOTHING ";
        jdbc.batchUpdate(updateAllUsers, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setString(1, users.get(i).getAbout());
                preparedStatement.setString(2, users.get(i).getFullname());
                preparedStatement.setString(3, users.get(i).getEmail());
                preparedStatement.setString(4, users.get(i).getNickname());
                preparedStatement.setString(5, forum);
            }

            @Override
            public int getBatchSize() {
                return users.size();
            }
        });
    }

    public Post getPostById (Long id) {
        //System.out.println("id   " + id);
        try {
            String sql = "SELECT * FROM posts WHERE id = (?)";
            return jdbc.queryForObject(sql, postMapper, id);
        }
        catch (DataAccessException error) {
            return null;
        }
    }

    public Long checkExistOfThePost(Long id) {
        try {
            String sql = "SELECT id FROM posts WHERE id = (?)";
            //return jdbc.queryForObject(sql, new Object[]{id}, Long.class);
            return jdbc.queryForObject(sql, Long.class, id);
        } catch (DataAccessException error) {
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
