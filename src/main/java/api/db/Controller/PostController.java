package api.db.Controller;

import api.db.DAO.ForumDAO;
import api.db.DAO.PostDAO;
import api.db.DAO.ThreadDAO;
import api.db.DAO.UserDAO;
import api.db.Models.Post;
import api.db.Models.Message;
import api.db.Models.PostFull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api/post")
public class PostController {
    private ForumDAO forumDAO;
    private UserDAO userDAO;
    private ThreadDAO threadDAO;
    private PostDAO postDAO;

    @Autowired
    PostController(ForumDAO forumDAO, UserDAO userDAO, ThreadDAO threadDAO, PostDAO postDAO) {
        this.forumDAO = forumDAO;
        this.userDAO = userDAO;
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
    }

    @PostMapping(path="/{id}/details")
    public ResponseEntity changePost(@PathVariable("id") Long id,
                                     @RequestBody Post body) {
        Post post = postDAO.getPostById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cannot find"));
        }
        if (post.getMessage().equals(body.getMessage()) || body.getMessage() == null) {
            return ResponseEntity.ok(post);
        }
        post.setMessage(body.getMessage());
        postDAO.changePost(post);
        return ResponseEntity.ok(postDAO.getPostById(id));
    }

    @GetMapping(path="/{id}/details")
    public ResponseEntity getPostDetails(@PathVariable("id") Long id,
                                         @RequestParam(value = "related", required = false) String[] related) {

        Post post = postDAO.getPostById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cannot find"));
        }
        PostFull postFull = new PostFull();
        if (related != null) {
            for (String object_type: related) {
                if (object_type.equals("user")) {
                    postFull.setAuthor(userDAO.getProfileUser(post.getAuthor()));
                }
                if (object_type.equals("forum")) {
                    postFull.setForum(forumDAO.getForumBySlug(post.getForum()));
                }
                if (object_type.equals("thread")) {
                    postFull.setThread(threadDAO.getThreadByIdLong(post.getThread()));
                }
            }
        }
        postFull.setPost(post);
        return ResponseEntity.ok(postFull);

    }
}
