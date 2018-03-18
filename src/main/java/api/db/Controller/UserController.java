package api.db.Controller;


import api.db.DAO.UserDAO;
import api.db.Models.Message;
import api.db.Models.User;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/api/user")
public class UserController {
    private final UserDAO userDAO;

    @Autowired
    UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    @PostMapping(path="/{nickname}/create")
    public ResponseEntity create(@PathVariable(name="nickname")String nickname,
                                 @RequestBody User user) {

        user.setNickname(nickname);
        try {
            userDAO.createUser(user);
        }
        catch (DuplicateKeyException error) {
            List<User> existUser = userDAO.getExistUser(user);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(existUser);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    @GetMapping(path="/{nickname}/profile")
    public ResponseEntity getProfile(@PathVariable(name="nickname")String nickname) {
        User user = userDAO.getProfileUser(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user"));
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping(path="/{nickname}/profile")
    public ResponseEntity updateUser(@PathVariable(name="nickname")String nickname,
                                     @RequestBody User user) {
        if (userDAO.getProfileUser(nickname) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user"));
        }
        user.setNickname(nickname);
        try {
            userDAO.updateUser(user);
        }
        catch (DuplicateKeyException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Can't find user"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
