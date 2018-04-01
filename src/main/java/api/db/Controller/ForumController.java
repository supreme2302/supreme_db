package api.db.Controller;

import api.db.DAO.ThreadDAO;
import api.db.DAO.UserDAO;
import api.db.Models.Thread;
import api.db.DAO.ForumDAO;
import api.db.Models.Forum;
import api.db.Models.Message;
import api.db.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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

        User user;
        user = userDAO.getProfileUser(forum.getUser());
        if (userDAO.getProfileUser(forum.getUser()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user"));
        }
        try {
            forum.setUser(user.getNickname());
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
        body.setForumid(forum.getId());
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

    @GetMapping(path="/{slug}/details")
    public ResponseEntity getInfoAboutForum(@PathVariable("slug")String slug) {
        Forum forum = forumDAO.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum"));
        }
        return ResponseEntity.ok(forum);
    }

    @GetMapping(path="/{slug}/threads")
    public ResponseEntity getThreads(@PathVariable("slug")String slug,
                                     @RequestParam(name="limit", required = false) Integer limit,
                                     @RequestParam(name="since", required = false) String since,
                                     @RequestParam(name="desc", required = false) Boolean desc) {
        Forum forum = forumDAO.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum"));
        }
        try {
            List<Thread> forumThreads = threadDAO.getAllThreadsOfForum(forum, limit, since, desc);
            return ResponseEntity.ok(forumThreads);
        }
        catch (DataAccessException error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find data"));
        }

    }


    //TODO::todo
//    @GetMapping(path="/{slug}/users")
//    public ResponseEntity getUsers(@PathVariable("slug") String slug,
//                                   @RequestParam(name="limit", required = false) Integer limit,
//                                   @RequestParam(name="since", required = false) String since,
//                                   @RequestParam(name="desc", required = false) Boolean desc) {
//        Forum forum = forumDAO.getForumBySlug(slug);
//        if (forum == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum"));
//        }
//    }

}
