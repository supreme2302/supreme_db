package api.db.DAO;

import api.db.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        String sql = "UPDATE users SET fullname=?, email=?, about=? WHERE lower(nickname)=LOWER(?)";
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

