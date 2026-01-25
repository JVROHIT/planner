package com.personal.planner.events;

import lombok.*;
import java.time.Instant;

/**
 * Domain event representing the creation of a new user account.
 *
 * <p><strong>When emitted:</strong></p>
 * <p>This event is emitted immediately after a new user account is successfully
 * created and persisted through the authentication/registration process.
 * It represents the immutable fact that a new user now exists in the system.</p>
 *
 * <p><strong>Event fields:</strong></p>
 * <ul>
 *   <li><strong>userId:</strong> The unique identifier of the newly created user</li>
 *   <li><strong>createdAt:</strong> The exact moment the user account was created</li>
 * </ul>
 *
 * <p><strong>What downstream systems should assume:</strong></p>
 * <ul>
 *   <li>A user account with the given userId now exists</li>
 *   <li>The user has completed the registration process</li>
 *   <li>The user is eligible for system features and data creation</li>
 *   <li>Default preferences and settings should be initialized</li>
 * </ul>
 *
 * <p><strong>What downstream systems must NOT infer:</strong></p>
 * <ul>
 *   <li>The user's email, name, or other profile information (not in event)</li>
 *   <li>The user's authentication method or credentials</li>
 *   <li>The user's intended usage patterns or goals</li>
 *   <li>Any business rules about user onboarding or validation</li>
 * </ul>
 *
 * <p><strong>Typical consumers:</strong></p>
 * <ul>
 *   <li>Preference service (to create default user preferences)</li>
 *   <li>Scheduling service (to set up initial planning triggers)</li>
 *   <li>Analytics service (for user registration metrics)</li>
 *   <li>Audit service (for account creation logging)</li>
 *   <li>Notification service (for welcome messages or onboarding)</li>
 * </ul>
 *
 * <p><strong>Bootstrap implications:</strong></p>
 * <p>This event typically triggers the creation of supporting data structures
 * like user preferences, initial planning schedules, and analytics baselines.
 * Consumers should handle this event idempotently in case of replay.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class UserCreated implements DomainEvent {
    /** Unique identifier for this event instance. */
    private String id;
    
    /** The unique identifier of the newly created user. */
    private String userId;
    
    /** The exact moment when the user account was created. */
    private Instant createdAt;

    /**
     * Returns the timestamp when this user creation occurred.
     *
     * @return the instant when the user was created
     */
    @Override
    public Instant occurredAt() {
        return createdAt;
    }

    /**
     * Returns the unique identifier for this event instance.
     *
     * @return the event ID
     */
    @Override
    public String eventId() {
        return id;
    }

    /**
     * Returns the ID of the newly created user.
     *
     * @return the user ID
     */
    @Override
    public String userId() {
        return userId;
    }
}
