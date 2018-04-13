package api.db.DAO;

import api.db.Models.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceDAO {
    private JdbcTemplate jdbc;

    @Autowired
    ServiceDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void clearDataBase() {
        String sql = "TRUNCATE users, forums, threads, posts, votes CASCADE ";
        try {
            jdbc.update(sql);
        }
        catch (DataAccessException error) {
            System.out.println("something goes wrong");
        }

    }



}
