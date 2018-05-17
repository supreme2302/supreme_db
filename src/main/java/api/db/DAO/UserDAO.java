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
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class UserDAO {
    private final JdbcTemplate jdbc;

    @Autowired
    public UserDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final UserMapper userMapper = new UserMapper();

    public void createUser(User user) {
        final String sql = "INSERT INTO users (nickname, fullname, email, about) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout());
    }

    public User getProfileUser(String nick) {
        try {
            final String sql = "SELECT * FROM users WHERE lower(nickname) = LOWER(?)";
            return jdbc.queryForObject(sql, userMapper, nick);
        }
        catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET " +
                "fullname=COALESCE(?, fullname), " +
                "email=COALESCE(?, email), " +
                "about=COALESCE(?, about)  WHERE lower(nickname)=LOWER(?)";
        jdbc.update(sql, user.getFullname(), user.getEmail(), user.getAbout(), user.getNickname());
    }

    public List<User> getExistUser(User user) {
        try {
            String sql = "SELECT * FROM users WHERE lower(nickname)=LOWER(?) OR lower(email)=LOWER(?)";
            return jdbc.query(sql, new Object[] {user.getNickname(), user.getEmail()}, userMapper);
        }
        catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    public List<User> getUsersOfForum(Forum forum, Integer limit, String since, Boolean desc) {
        //TODO:Доделать
        desc = (desc != null) && desc;
        List<Object> insertionArr = new ArrayList<>();
        String sql = "SELECT about, fullname, email, nickname  FROM \"allUsers\"";
        if (since == null) {
            sql += " WHERE forum = ? ORDER BY nickname ";
            insertionArr.add(forum.getSlug());
            if (desc) {
                sql += " DESC ";
            }
            if (limit != null) {
                sql += " LIMIT ?";
                insertionArr.add(limit);
            }
        }
        else {
            sql += " WHERE forum = ? ";
            insertionArr.add(forum.getSlug());
            if (desc) {
                sql += " AND lower(nickname) < lower(?)  ORDER BY nickname DESC";
            }
            else {
                sql += " AND lower(nickname) > lower(?)  ORDER BY nickname ";

            }
            insertionArr.add(since);
            if (limit != null) {
                sql += " LIMIT ?";
                insertionArr.add(limit);
            }
        }
//        String sql = "SELECT about, fullname, email, nickname COLLATE \"ucs_basic\" FROM ( ";
//        if (since == null) {
//            sql += "(SELECT about, fullname, email, nickname FROM users AS u" +
//                    " JOIN threads AS t " +
//                    " ON u.nickname = t.author " +
//                    " AND t.forum = ?" +
//                    " UNION " +
//                    " SELECT about, fullname, email, nickname FROM users AS u " +
//                    " JOIN posts AS p " +
//                    " ON u.nickname = p.author " +
//                    " AND p.forum = ?)" +
//                    " ) as result" +
//                    " ORDER BY nickname";
//            insertionArr.add(forum.getSlug());
//            insertionArr.add(forum.getSlug());
//            if (desc) {
//                sql += " DESC";
//            }
//            if (limit != null) {
//                sql += " LIMIT ?";
//                insertionArr.add(limit);
//            }
//
//        }
//        else {
//            sql += "(SELECT about, fullname, email, nickname FROM users AS u" +
//                    " JOIN threads AS t " +
//                    " ON u.nickname = t.author " +
//                    " AND t.forum = ?" +
//                    " UNION " +
//                    " SELECT about, fullname, email, nickname FROM users AS u " +
//                    " JOIN posts AS p " +
//                    " ON u.nickname = p.author " +
//                    " AND p.forum = ?)" +
//                    " ) as result ";
//
//            insertionArr.add(forum.getSlug());
//            insertionArr.add(forum.getSlug());
//            if (desc) {
//                sql += " WHERE lower(nickname) < lower(?) COLLATE \"ucs_basic\" ORDER BY nickname DESC";
//            }
//            else {
//                sql += " WHERE lower(nickname) > lower(?) COLLATE \"ucs_basic\" ORDER BY nickname ";
//
//            }
//            insertionArr.add(since);
//
//            if (limit != null) {
//                sql += " LIMIT ?";
//                insertionArr.add(limit);
//            }
//
//        }
        return jdbc.query(sql, insertionArr.toArray(), userMapper);
    }

    public Integer getAllUsers() {
        String sql = "SELECT count(*) FROM users";
        return jdbc.queryForObject(sql, Integer.class);
    }




    private static final class UserMapper implements RowMapper<User> {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            final User user = new User();
            user.setNickname(rs.getString("nickname"));
            user.setFullname(rs.getString("fullname"));
            user.setEmail(rs.getString("email"));
            user.setAbout(rs.getString("about"));
            return user;
        }
    }
}

