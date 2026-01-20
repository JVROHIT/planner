package com.personal.planner.infra.mongo;

import com.personal.planner.domain.nudge.Nudge;
import com.personal.planner.domain.nudge.NudgeRepository;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the NudgeRepository for end-to-end reality check.
 */
@Component
public class MongoNudgeRepository implements NudgeRepository {

    private final Map<String, Nudge> store = new ConcurrentHashMap<>();

    @Override
    public Nudge save(Nudge nudge) {
        if (nudge.getId() == null) {
            // // Reflection-based ID injection
            try {
                java.lang.reflect.Field field = Nudge.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(nudge, java.util.UUID.randomUUID().toString());
            } catch (Exception e) {
                // ...
            }
        }
        store.put(nudge.getId(), nudge);
        return nudge;
    }

    @Override
    public List<Nudge> findByUserIdAndStatus(String userId, Nudge.Status status) {
        return store.values().stream()
                .filter(n -> n.getUserId().equals(userId) && n.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Nudge> findByStatusAndScheduledForBefore(Nudge.Status status, Instant now) {
        return store.values().stream()
                .filter(n -> n.getStatus() == status && n.getScheduledFor().isBefore(now))
                .collect(Collectors.toList());
    }
}
