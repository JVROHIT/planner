package com.personal.planner.infra.mongo;

import com.personal.planner.domain.user.User;
import com.personal.planner.domain.user.UserRepository;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * In-memory implementation of the UserRepository.
 *
 * <p>Stores user entities in a ConcurrentHashMap for thread-safe access.
 * This is a temporary implementation for development/testing - production
 * should use actual MongoDB collections.</p>
 *
 * <p>User entities contain authentication information (email, password hash)
 * and are the root of all user-scoped data in the system.</p>
 *
 * <p>Custom queries:
 * <ul>
 *   <li>findByEmail: Returns user by email address (case-insensitive)</li>
 *   <li>findAll: Returns all users in the system</li>
 * </ul>
 * </p>
 *
 * <p>SECURITY: Password hashes are stored, never plain text passwords.</p>
 */
@Component
public class MongoUserRepository implements UserRepository {
    private final Map<String, User> store = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            try {
                java.lang.reflect.Field field = User.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(user, UUID.randomUUID().toString());
            } catch (Exception e) {
                // ...
            }
        }
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return store.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public java.util.List<User> findAll() {
        return java.util.List.copyOf(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
