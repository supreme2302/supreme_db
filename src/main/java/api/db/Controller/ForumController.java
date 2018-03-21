package api.db.Controller;

import api.db.DAO.ThreadDAO;
import api.db.DAO.UserDAO;
import api.db.Models.Thread;
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
    private ThreadDAO threadDAO;
    @Autowired
    ForumController(ForumDAO forumDAO, UserDAO userDAO, ThreadDAO threadDAO) {
        this.forumDAO = forumDAO;
        this.userDAO = userDAO;
        this.threadDAO = threadDAO;
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
            Forum exForum = forumDAO.existingForum(forum);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exForum);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(forum);
    }

    @PostMapping(path="/{slug}/create")
    public ResponseEntity createSlug(@PathVariable("slug") String slug,
                                     @RequestBody Thread body) {

        Forum forum = forumDAO.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum"));
        }
        body.setForum(forum.getSlug());
        //body.setSlug(slug);
        if (userDAO.getProfileUser(body.getAuthor()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user"));
        }

        try {
            threadDAO.createThread(body);
        }
        catch (DuplicateKeyException error) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadDAO.getThreadBySlug(body.getSlug()));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

}
