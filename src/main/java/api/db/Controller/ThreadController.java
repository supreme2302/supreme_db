package api.db.Controller;

import api.db.DAO.ForumDAO;
import api.db.DAO.PostDAO;
import api.db.DAO.ThreadDAO;
import api.db.DAO.UserDAO;
import api.db.Models.*;
import api.db.Models.Thread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Thread GetThreadBySlugOrId(String slug_or_id) {
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

        thread = GetThreadBySlugOrId(slug_or_id);

        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cannot find thread"));
        }

        Integer res = postDAO.createPost(posts, thread, userDAO);
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
        if (GetThreadBySlugOrId(slug_or_id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cannot find thread"));
        }
        return ResponseEntity.ok(GetThreadBySlugOrId(slug_or_id));
    }

    @PostMapping(path="/{slug_or_id}/details")
    public ResponseEntity changeThread(@PathVariable("slug_or_id") String slug_or_id,
                                       @RequestBody Thread body) {
        Thread thread = GetThreadBySlugOrId(slug_or_id);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cannot find thread"));
        }

        if (body.getMessage() == null && body.getTitle() == null) {
            return ResponseEntity.ok(thread);
        }
        thread.setMessage(body.getMessage());
        thread.setTitle(body.getTitle());
        threadDAO.changeThread(thread);
        Thread result = GetThreadBySlugOrId(slug_or_id);
        return ResponseEntity.ok(result);

    }

    @PostMapping(path="/{slug_or_id}/vote")
    public ResponseEntity Vote(@PathVariable("slug_or_id") String slug,
                               @RequestBody Vote vote) {
        Thread thread;
        thread = GetThreadBySlugOrId(slug);
        User user = userDAO.getProfileUser(vote.getNickname());
        if (thread == null || user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Message("Cannot find thread or user"));
        }
        Vote existVote = threadDAO.getVoteByNick(vote.getNickname(), thread.getId());
        if (existVote == null) {
            vote.setThreadid(thread.getId());
            threadDAO.createVote(vote);
        }
        else {
            int newVote;
            if (existVote.getVoice().equals(vote.getVoice())) {
                return ResponseEntity.ok(GetThreadBySlugOrId(slug));
            }
            else if (existVote.getVoice() > 0 && vote.getVoice() < 0) {
                newVote = -2;
                threadDAO.updateVote(vote, existVote, newVote);
            }
            else if (existVote.getVoice() < 0 && vote.getVoice() > 0) {
                newVote = 2;
                threadDAO.updateVote(vote, existVote, newVote);
            }
        }

        thread = GetThreadBySlugOrId(slug);

        return ResponseEntity.ok(thread);
    }


//    //todo
//    @PostMapping(path = "/{slug_or_id}/vote")
//    public ResponseEntity setVote(@PathVariable(name = "slug_or_id") String slug_or_id,
//                                  @RequestBody Vote vote) {
//
//        Thread thread;
//        thread = GetThreadBySlugOrId(slug_or_id);
//        if (thread == null || userDAO.getProfileUser(vote.getNickname()) == null)
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find thread"));
//
//        Vote checkVote = threadDAO.getVotebyVote(vote, thread);//голосовал ли он ранее
//
//        if (checkVote == null) {
//            threadDAO.createVotePasha(thread, vote.getVoice()/*, flag*/);//прибавление голоса в ветке или уменьшение
//            threadDAO.insert_or_update_Vote(vote, thread);
//        }
//
//        if (checkVote != null && (vote.getVoice()).equals(checkVote.getVoice())) {
//            return ResponseEntity.ok(threadDAO.getThreadById((int)thread.getId()));
//        }
//
//        if (checkVote != null && !(vote.getVoice().equals(checkVote.getVoice()))) {
//            threadDAO.createVotePasha(thread, (vote.getVoice() * 2)/*, flag*/);//прибавление голоса в ветке или уменьшение
//            threadDAO.updateVote(vote, thread);
//        }
//        return ResponseEntity.ok(GetThreadBySlugOrId(slug_or_id));
//    }


    @GetMapping(path="/{slug_or_id}/posts")
    public ResponseEntity getPosts(@PathVariable("slug_or_id") String slug_or_id,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "since", required = false) Integer since,
                                   @RequestParam(name = "sort", required = false) String sort,
                                   @RequestParam(name = "desc", required = false) Boolean desc) {

        Thread thread;
        if (sort == null) {
            sort = "flat";
        }

        thread = GetThreadBySlugOrId(slug_or_id);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cannot find thread"));
        }
        List<Post> postListOfThread;
        if (sort.equals("flat")) {
            postListOfThread = postDAO.getPostsOfThreadByFlat(thread, limit, since, desc);
        }
        else if (sort.equals("tree")) {
            postListOfThread = postDAO.getPostsOfThreadByTree(thread, limit, since, desc);
        }
        else {
            postListOfThread = postDAO.getPostsOfThreadByParent(thread, limit, since, desc);
        }

        return ResponseEntity.ok(postListOfThread);
    }

}
