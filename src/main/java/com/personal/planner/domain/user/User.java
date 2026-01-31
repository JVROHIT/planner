package com.personal.planner.domain.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * User entity representing an authenticated user in the system.
 * <p>
 * The User entity provides identity scoping and authentication capabilities.
 * It serves as the root entity that owns all other domain entities (Tasks, Goals,
 * Plans, etc.) through the userId field present in those entities.
 * </p>
 * <p>
 * Security Considerations:
 * <ul>
 *   <li>Password is stored as a hash, never in plain text.</li>
 *   <li>Password hash can be updated via setPasswordHash() for password changes.</li>
 * </ul>
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "user")
public class User {
    /** Unique identifier for this user. */
    @Id
    private String id;

    /** Email address used for authentication and communication. */
    private String email;

    /**
     * Hashed password for authentication.
     * Never store plain text passwords. This field should contain a secure hash.
     */
    private String passwordHash;

    /** Timestamp when this user account was created. */
    private Instant createdAt;

    /** IANA time zone ID for this user. Defaults to Asia/Kolkata if not set. */
    private String timeZone;

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
