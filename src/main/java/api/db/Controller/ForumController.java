package api.db.Controller;

import api.db.DAO.UserDAO;
import api.db.Models.User;
import api.db.DAO.ForumDAO;
import api.db.Models.Forum;
import api.db.Models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api/forum")
public class ForumController {
    private ForumDAO forumDAO;
    private UserDAO userDAO;
    @Autowired
    ForumController(ForumDAO forumDAO, UserDAO userDAO) {
        this.forumDAO = forumDAO;
        this.userDAO = userDAO;
    }

    @PostMapping(path="/create")
    public ResponseEntity create(@RequestBody Forum forum) {

        if (userDAO.getProfileUser(forum.getAuthor()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user"));
        }
        try {
            forumDAO.createForum(forum);
        }
        catch (DuplicateKeyException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("exists"));
            //Исправить в соответсвии с тз
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(forum);
    }

}
