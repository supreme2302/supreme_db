package api.db.Controller;

import api.db.DAO.ForumDAO;
import api.db.DAO.PostDAO;
import api.db.DAO.ThreadDAO;
import api.db.DAO.UserDAO;
import api.db.Models.Forum;
import api.db.Models.Message;
import api.db.Models.Post;
import api.db.Models.Thread;
import api.db.Models.User;
import javafx.geometry.Pos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping(path="/api/thread")
public class ThreadController {
    private ForumDAO forumDAO;
    private UserDAO userDAO;
    private ThreadDAO threadDAO;
    private PostDAO postDAO;

    @Autowired
    ThreadController(ForumDAO forumDAO, UserDAO userDAO, ThreadDAO threadDAO, PostDAO postDAO) {
        this.forumDAO = forumDAO;
        this.userDAO = userDAO;
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
    }

    //TODO:: возможно переделать на один метод из DAO
    public Thread CheckSlugOrId (String slug_or_id) {
        if (slug_or_id.matches("\\d+")) {
            Integer id = Integer.parseInt(slug_or_id);
            return threadDAO.getThreadById(id);
        }
        else {
            return threadDAO.getThreadBySlug(slug_or_id);
        }
    }

    @PostMapping(path="/{slug_or_id}/create")
    public ResponseEntity createThread(@PathVariable("slug_or_id") String slug_or_id,
                                       @RequestBody List<Post> posts) {
        //TODO:: 404
        Thread thread;

        thread = CheckSlugOrId(slug_or_id);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cannot find thread"));
        }
        Integer res = postDAO.createPost(posts, thread);
        if (res == 409) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Conflict"));
        }
        else if (res == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cannot find something"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(posts);

    }

}
