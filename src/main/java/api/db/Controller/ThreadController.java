package api.db.Controller;

import api.db.DAO.ForumDAO;
import api.db.DAO.PostDAO;
import api.db.DAO.ThreadDAO;
import api.db.DAO.UserDAO;
import api.db.Models.*;
import api.db.Models.Thread;
import javafx.geometry.Pos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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

    @GetMapping(path="/{slug_or_id}/details")
    public ResponseEntity getDetails(@PathVariable("slug_or_id") String slug_or_id) {
        if (CheckSlugOrId(slug_or_id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cannot find thread"));
        }
        return ResponseEntity.ok(CheckSlugOrId(slug_or_id));
    }

    @PostMapping(path="/{slug_or_id}/vote")
    public ResponseEntity Vote(@PathVariable("slug_or_id") String slug,
                               @RequestBody Vote vote) {
        Thread thread;
        thread = CheckSlugOrId(slug);
        User user = userDAO.getProfileUser(vote.getNickname());
        if (thread == null || user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Message("Cannot find thread or user"));
        }
        Vote existVote = threadDAO.getVoteByNick(vote.getNickname());
        if (existVote == null) {
            vote.setThreadid(thread.getId());
            threadDAO.createVote(vote);
        }
        else {
            int newVote = 0;
            if (existVote.getVoice() > 0 && vote.getVoice() < 0) {
                newVote = -2;
            }
            else if (existVote.getVoice() < 0 && vote.getVoice() > 0) {
                newVote = 2;
            }
            threadDAO.updateVote(vote, existVote, newVote);
        }

        thread = CheckSlugOrId(slug);

        return ResponseEntity.ok(thread);
    }

    @GetMapping(path="/{slug_or_id}/posts")
    public ResponseEntity getPosts(@PathVariable("slug_or_id") String slug_or_id,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "since", required = false) String since,
                                   @RequestParam(name = "sort", required = false) String sort,
                                   @RequestParam(name = "desc", required = false) Boolean desc) {

        Thread thread;

        thread = CheckSlugOrId(slug_or_id);
        System.out.println(thread.getId());
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cannot find thread"));
        }

        List<Post> postListOfThread = postDAO.getPostsOfThread(thread, limit, since, sort, desc);
        return ResponseEntity.ok(postListOfThread);
    }

}
