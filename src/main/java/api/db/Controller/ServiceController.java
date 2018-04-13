package api.db.Controller;


import api.db.DAO.*;
import api.db.Models.Status;
import api.db.Models.User;
import api.db.Models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/api/service")
public class ServiceController {
    private ForumDAO forumDAO;
    private UserDAO userDAO;
    private ThreadDAO threadDAO;
    private PostDAO postDAO;
    private ServiceDAO serviceDAO;

    @Autowired
    ServiceController (UserDAO userDAO, ForumDAO forumDAO, ThreadDAO threadDAO,
                       PostDAO postDAO, ServiceDAO serviceDAO) {
        this.userDAO = userDAO;
        this.forumDAO = forumDAO;
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
        this.serviceDAO = serviceDAO;
    }

    @GetMapping(path="/status")
    public ResponseEntity getStatus() {
        Status status = new Status();
        status.setForum(forumDAO.getAllForums());
        status.setPost(postDAO.getAllPosts());
        status.setThread(threadDAO.getAllThreads());
        status.setUser(userDAO.getAllUsers());
        return ResponseEntity.ok(status);
    }

    @PostMapping(path="/clear")
    public ResponseEntity clearDataBase() {
        serviceDAO.clearDataBase();
        return ResponseEntity.ok(new Message("Well done!"));
    }

}
