package com.personal.planner.infra.mongo;

import com.personal.planner.domain.preference.UserPreference;
import com.personal.planner.domain.preference.UserPreferenceRepository;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MongoUserPreferenceRepository implements UserPreferenceRepository {
    private final Map<String, UserPreference> store = new ConcurrentHashMap<>();

    @Override
    public UserPreference save(UserPreference preference) {
        store.put(preference.getUserId(), preference);
        return preference;
    }

    @Override
    public Optional<UserPreference> findByUserId(String userId) {
        return Optional.ofNullable(store.get(userId));
    }
}
