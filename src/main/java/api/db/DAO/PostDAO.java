package api.db.DAO;


import api.db.Models.Post;
import api.db.Models.Thread;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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

    public Integer createPost (List<Post> posts, Thread thread) {


            for (Post post : posts) {
                post.setForum(thread.getForum());
                post.setThread((long) thread.getId());
                post.setCreated(posts.get(0).getCreated());

                Post parentPost = getPostById(post.getParent());


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
        List<Object> insertionArr = new ArrayList<>();

            String sql = "SELECT * FROM posts WHERE thread = (?)";
            insertionArr.add(thread.getId());

            if (since != null) {
                sql += " AND id > (?)";
                insertionArr.add(since);
            }
            sql += " ORDER BY created::timestamptz";

            if (desc != null && desc) {
                sql += " DESC, id DESC";
            }
            else {
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
        List<Object> insertionArr = new ArrayList<>();
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
//        String sql = "SELECT * FROM posts JOIN";
//        if (since != null) {
//            sql += " (SELECT id FROM posts WHERE thread = (?) AND parent = 0 AND  ";
//            if (desc != null && desc) {
//                sql += " path < (SELECT path FROM posts WHERE id = ?) ORDER BY path DESC, thread DESC LIMIT ?) ";
//            }
//            else {
//                sql += " path > (SELECT path FROM posts WHERE id = ?) ORDER BY path, thread LIMIT ?) ";
//            }
//            sql += " AS selected ON thread = ? AND selected.id = path[1]";
//
//            int threadId = thread.getId();
//            insertionArr.add(threadId);
//            insertionArr.add(since);
//            insertionArr.add(limit);
//            insertionArr.add(threadId);
//        }
//        else {
//            sql += " (SELECT id FROM posts WHERE parent = 0 AND thread = ?  ";
//            if (desc != null && desc) {
//                sql += " ORDER BY path DESC, thread DESC LIMIT ?) AS selected ON thread = ? AND selected.id = path[1] ";
//            }
//            else {
//                sql += " ORDER BY path , thread  LIMIT ?) AS selected ON thread = ? AND selected.id = path[1] ";
//            }
//            int threadId = thread.getId();
//            insertionArr.add(threadId);
//            insertionArr.add(limit);
//            insertionArr.add(threadId);
//        }
//
//        sql += " ORDER BY path ";
////        if (desc != null && desc) {
////            sql += " DESC ";
////        }
//        sql += " , thread";
////        if (desc != null && desc) {
////            sql += " DESC ";
////        }
//
//
//        return jdbc.query(sql, insertionArr.toArray(), postMapper);
        String sql = "SELECT * FROM posts ";
        desc = (desc != null) && desc;
        if (desc) {
            if (limit != null) {
                sql += " JOIN (SELECT id FROM posts WHERE parent = 0 AND thread = ? " +
                        " ORDER BY id DESC LIMIT ? ) AS selected ON (thread = ? AND selected.id = path[1]) ";
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
        return jdbc.query(sql, insertionArr.toArray(), postMapper);
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
            Array path = rs.getArray("path");
            return new Post(id, author, forum, message, created, isEdited,
                    parent, thread, (Object[])path.getArray());
        }
    }
}
