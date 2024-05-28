package com.example.chatengine.serverspring.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.chatengine.serverspring.model.User;
import com.example.chatengine.serverspring.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing users.
 */
@RestController
public class UserController {
    private UserService service;

    /**
     * Constructor for UserController.
     *
     * @param service the UserService to be used by the controller.
     */
    public UserController(UserService service) {
        this.service = service;
    }

    /**
     * Endpoint for user login.
     *
     * @param request a map containing the username and secret.
     * @return a ResponseEntity indicating the result of the login attempt.
     */
    @CrossOrigin
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseEntity getLogin(@RequestBody HashMap<String, String> request) {
        String username = request.get("username");
        String secret = request.get("secret");
        return service.loginUser(username, secret);
    }

    /**
     * Endpoint for user sign-up.
     *
     * @param userData a map containing user data for sign-up.
     * @return a ResponseEntity indicating the result of the sign-up attempt.
     */
    @CrossOrigin
    @RequestMapping(path = "/signup", method = RequestMethod.POST)
    public ResponseEntity signUpUser(@RequestBody Map<String, String> userData) {
        return service.signUpUser(userData);
    }

    /**
     * Endpoint to retrieve all users.
     *
     * @return a list of users.
     */
    @CrossOrigin
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return service.getAllUsersFromChatEngine();
    }

    /**
     * Endpoint to delete a user by ID.
     *
     * @param userId the ID of the user to be deleted.
     * @return a ResponseEntity indicating the result of the deletion.
     */
    @CrossOrigin
    @DeleteMapping("/users/{userId}")
    public ResponseEntity deleteUser(@PathVariable int userId) {
        ResponseEntity<?> responseEntity = service.deleteUser(userId);
        HttpStatus statusCode = (HttpStatus) responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to delete user", statusCode);
        }
    }

    /**
     * Endpoint to verify an email address using a token.
     *
     * @param token the token to verify the email.
     * @return a ResponseEntity indicating the result of the verification.
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        return service.verifyEmail(token);
    }
}