package api.db.DAO;


import api.db.Models.Post;
import api.db.Models.Thread;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.StyledEditorKit;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    public Integer createPost (List<Post> posts, Thread thread, UserDAO userDAO) {


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


                String sql = "INSERT INTO posts (author, message, parent, created, thread, forum) " +
                        "VALUES (?, ?, ?, ?::timestamptz, ?, ?) RETURNING id";


                Integer id = jdbc.queryForObject(sql, new Object[] {post.getAuthor(),
                        post.getMessage(), post.getParent(), post.getCreated(),
                                thread.getId(), thread.getForum()},
                        Integer.class);
                post.setId((long)id);
                setPathOfPost(parentPost, post);

                String updateForumSql = "UPDATE forums SET posts = posts + 1 WHERE slug = ?";
                jdbc.update(updateForumSql, post.getForum());
            }
            return 201;
    }

    public void setPathOfPost(Post parent, Post body) {

        //TODO:: TODO TODO TODO

        jdbc.update(con -> {
            PreparedStatement pst = con.prepareStatement(
                    "update posts set" +
                            "  path = ? " +
                            "where id = ?");
            if (body.getParent() == 0) {
                pst.setArray(1, con.createArrayOf("INT", new Object[]{body.getId()}));
            } else {
                ArrayList arr = new ArrayList<Object>(Arrays.asList(parent.getPath()));
                arr.add(body.getId());
                pst.setArray(1, con.createArrayOf("INT", arr.toArray()));
            }
            pst.setLong(2, body.getId());
            return pst;
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
        //TODO: like parent
        List<Object> insertionArr = new ArrayList<>();
        desc = (desc != null) && desc;
        String sql = "SELECT * FROM posts WHERE thread = (?)";
        insertionArr.add(thread.getId());
        if (since != null) {
            if (!desc) {
                sql += " AND id > (?)";
            }
            else {
                sql += " AND id < (?)";
            }

            insertionArr.add(since);
        }
        sql += " ORDER BY created::timestamptz";
        if (desc) {
            sql += " DESC, id DESC";
        } else {
            sql += " , id ";
        }
        if (limit != null) {
            sql += " LIMIT (?)";
            insertionArr.add(limit);
        }
        return jdbc.query(sql, insertionArr.toArray(), postMapper);


    }
    public List<Post> getPostsOfThreadByTree(Thread thread, Integer limit, Integer since,
                                             Boolean desc) {
        //TODO: like parent
        List<Object> insertionArr = new ArrayList<>();
        desc = (desc != null) && desc;
        String sql = "SELECT * FROM posts WHERE thread = (?)";
        insertionArr.add(thread.getId());

            if (since != null) {
                if (desc) {
                    sql += " AND path < (SELECT path from posts WHERE id = (?))";
                }
                else {
                    sql += " AND path > (SELECT path from posts WHERE id = (?))";
                }
                insertionArr.add(since);
            }

            sql += " ORDER BY path";

            if (desc != null && desc) {
                sql += " DESC, id DESC ";
            }
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

        //TODO:: подумать и упростить
        String sql = "SELECT * FROM posts ";
        desc = (desc != null) && desc;
        if (since == null) {
            if (desc) {
                if (limit != null) {
                    sql += " JOIN (SELECT id FROM posts WHERE parent = 0 AND thread = ? " +
                            " ORDER BY path desc LIMIT ? ) AS selected ON (selected.id = path[1] AND thread = ?) ORDER BY path[1] DESC, path";
                    insertionArr.add(thread.getId());
                    insertionArr.add(limit);
                    insertionArr.add(thread.getId());
                }
            }
            else {
                if (limit != null) {
                    sql += " JOIN (SELECT id FROM posts WHERE parent = 0 AND thread = ? " +
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
                    sql += " JOIN (SELECT id FROM posts WHERE parent = 0 AND thread = ?" +
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
                    sql += " JOIN (SELECT id FROM posts WHERE parent = 0 AND thread = ?" +
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
        String sql = "UPDATE posts SET message = COALESCE(?, message), " +
                "isedited = COALESCE(true, isedited) WHERE id = ?";
        try {
            jdbc.update(sql, post.getMessage(), post.getId());
        }
        catch (DuplicateKeyException error) {
            System.out.println("kaka");
        }

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
