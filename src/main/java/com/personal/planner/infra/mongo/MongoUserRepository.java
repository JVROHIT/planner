package com.personal.planner.infra.mongo;

import com.personal.planner.domain.user.User;
import com.personal.planner.domain.user.UserRepository;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

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
}
