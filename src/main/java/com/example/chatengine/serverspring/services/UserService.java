package com.example.chatengine.serverspring.services;

import com.example.chatengine.serverspring.db.UserRepository;
import com.example.chatengine.serverspring.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService {

    @Value("${CHAT_ENGINE_PROJECT_ID}")
    private String CHAT_ENGINE_PROJECT_ID;
    @Value("${CHAT_ENGINE_API_KEY}")
    private String CHAT_ENGINE_PRIVATE_KEY;

    @Value("${EMAIL_PASS}")
    private String EMAIL_PASSWORD;
    private final UserRepository repo;

    /**
     * Constructor for UserService.
     *
     * @param repo the UserRepository to be used by the service.
     */
    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    /**
     * Handles user login.
     *
     * @param username the username of the user.
     * @param secret   the secret (password) of the user.
     * @return a ResponseEntity indicating the result of the login attempt.
     */
    public ResponseEntity loginUser(String username, String secret) {
        HttpURLConnection con = null;
        try {
            // First, check if the user exists in the repository
            User user = repo.findByUsername(username);

            URL url = new URL("https://api.chatengine.io/users/me");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Project-ID", CHAT_ENGINE_PROJECT_ID);
            con.setRequestProperty("User-Name", username);
            con.setRequestProperty("User-Secret", secret);

            if (user == null) {
                return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
            }

            // Check if the provided secret matches the user's secret
            if (!user.getSecret().equals(secret)) {
                return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
            }

            if (!user.isVerified()) {
                return new ResponseEntity<>("User not verified. Please check your email for the verification link.", HttpStatus.UNAUTHORIZED);
            }

            StringBuilder responseStr = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseStr.append(responseLine.trim());
                }
            }

            Map<String, Object> response = new Gson().fromJson(responseStr.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * Handles user sign-up.
     *
     * @param userData a map containing user data for sign-up.
     * @return a ResponseEntity indicating the result of the sign-up attempt.
     */
    public ResponseEntity signUpUser(Map<String, String> userData) {
        HttpURLConnection con = null;
        try {
            String username = userData.get("username");
            if (ifUsernameExists(username)) {
                return new ResponseEntity<>("Username already exists", HttpStatus.BAD_REQUEST);
            }

            String verificationToken = UUID.randomUUID().toString();

            URL url = new URL("https://api.chatengine.io/users/");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Private-Key", CHAT_ENGINE_PRIVATE_KEY);
            con.setDoOutput(true);

            userData.put("secret", userData.get("secret"));  // Ensure 'secret' is included
            String jsonInputString = new JSONObject(userData).toString();

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            System.out.println("Response code: " + responseCode); // Add this line
            if (responseCode == HttpURLConnection.HTTP_CREATED) { // Check if response is HTTP_CREATED
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        responseStr.append(responseLine.trim());
                    }
                }
                System.out.println("Response body: " + responseStr.toString()); // Add this line
                Map<String, Object> response = new Gson().fromJson(responseStr.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());

                User newUser = new User();
                newUser.setUsername(userData.get("username"));
                newUser.setSecret(userData.get("secret"));
                newUser.setEmail(userData.get("email"));
                newUser.setFirstName(userData.get("first_name"));
                newUser.setLastName(userData.get("last_name"));
                newUser.setVerificationToken(verificationToken);
                newUser.setVerified(false);
                repo.save(newUser);

                sendVerificationEmail(newUser.getEmail(), verificationToken);

                return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
            } else {
                // Read error response
                StringBuilder errorResponseStr = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponseStr.append(responseLine.trim());
                    }
                }
                System.err.println("Error response: " + errorResponseStr.toString()); // Debug log for error response
                return new ResponseEntity<>(errorResponseStr.toString(), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * Verifies user email using a verification token.
     *
     * @param token the verification token.
     * @return a ResponseEntity indicating the result of the verification.
     */
    public ResponseEntity verifyEmail(String token) {
        try {
            Optional<User> optionalUser = repo.findByVerificationToken(token);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setVerified(true);
                user.setVerificationToken(null);
                repo.save(user);

                return new ResponseEntity<>("Email verified successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid verification token", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sends a verification email to the user.
     *
     * @param email            the user's email address.
     * @param verificationToken the verification token.
     */
    private void sendVerificationEmail(String email, String verificationToken) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.port", "587");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("no-reply.studenthousing@outlook.com", EMAIL_PASSWORD);
            }
        };

        Session session = Session.getInstance(props, auth);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply.studenthousing@outlook.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Verify your email address");
            message.setText("Please click the following link to verify your email address: https://chat-app-v84a.onrender.com/verify?token=" + verificationToken);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all users from the Chat Engine API.
     *
     * @return a list of user maps retrieved from the Chat Engine API.
     */
    public List<Map<String, Object>> getAllUsersFromChatEngine() {
        List<Map<String, Object>> users = new ArrayList<>();
        HttpURLConnection con = null;
        try {
            // Create GET request to Chat Engine API
            URL url = new URL("https://api.chatengine.io/users");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            // Set request headers
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Private-Key", CHAT_ENGINE_PRIVATE_KEY);
            // Read response from Chat Engine API
            StringBuilder responseStr = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseStr.append(responseLine.trim());
                }
            }
            // Convert response to a list of user maps
            users = new Gson().fromJson(
                    responseStr.toString(), new TypeToken<List<Map<String, Object>>>() {}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return users;
    }

    /**
     * Deletes a user by their ID from the Chat Engine API.
     *
     * @param userId the ID of the user to be deleted.
     * @return a ResponseEntity indicating the result of the deletion.
     */
    public ResponseEntity deleteUser(int userId) {
        HttpURLConnection con = null;
        try {
            // Create DELETE request to Chat Engine API
            URL url = new URL("https://api.chatengine.io/users/" + userId);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            // Set request headers
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("PRIVATE-KEY", CHAT_ENGINE_PRIVATE_KEY); // Set private key header
            // Read response from Chat Engine API
            int responseCode = con.getResponseCode();
            System.out.println("Response code: " + responseCode); // Add this line
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // If the response code is OK, user deleted successfully
                return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
            } else {
                // If the response code is not OK, handle the error
                return new ResponseEntity<>(null, HttpStatus.valueOf(responseCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * Checks if a username already exists in the repository.
     *
     * @param username the username to check.
     * @return true if the username exists, false otherwise.
     */
    private boolean ifUsernameExists(String username) {
        return repo.existsByUsername(username);
    }
}