package com.personal.planner.domain.preference;

import java.util.Optional;

public interface UserPreferenceRepository {
    UserPreference save(UserPreference preference);

    Optional<UserPreference> findByUserId(String userId);
}
