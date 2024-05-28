package com.example.chatengine.serverspring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

/**
 * Entity class representing a user in the system.
 */
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The username of the user. */
    @Column(name = "username")
    private String username;

    /** The secret (password) of the user. */
    private String secret;

    /** The email address of the user. */
    private String email;

    /** The first name of the user. */
    private String firstName;

    /** The last name of the user. */
    private String lastName;

    /** The verification token associated with the user's email verification. */
    @Column(name = "verification_token")
    private String verificationToken;

    /** Flag indicating whether the user's email has been verified. */
    private boolean verified;
}