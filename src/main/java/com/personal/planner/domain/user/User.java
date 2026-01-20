package com.personal.planner.domain.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * User entity for authentication and identity scoping.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "user")
public class User {
    @Id
    private String id;
    private String email;
    private String passwordHash;
    private Instant createdAt;

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
