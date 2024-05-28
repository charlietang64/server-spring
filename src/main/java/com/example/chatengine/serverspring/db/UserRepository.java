package com.example.chatengine.serverspring.db;

import com.example.chatengine.serverspring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for User entities, providing CRUD operations.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Find a user by verification token.
     *
     * @param verificationToken the verification token to search for.
     * @return an Optional containing the user if found, otherwise empty.
     */
    Optional<User> findByVerificationToken(String verificationToken);

    /**
     * Find a user by username.
     *
     * @param username the username to search for.
     * @return the user with the specified username.
     */
    User findByUsername(String username);

    /**
     * Check if a user exists by username.
     *
     * @param username the username to check.
     * @return true if a user with the specified username exists, otherwise false.
     */
    boolean existsByUsername(String username);
}
