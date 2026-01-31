package com.personal.planner.domain.user;

import com.personal.planner.domain.common.constants.TimeConstants;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneId;

/**
 * Resolves the effective timezone for a user.
 * Defaults to Asia/Kolkata when no user timezone is configured.
 */
@Service
public class UserTimeZoneService {

    private final UserRepository userRepository;

    public UserTimeZoneService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ZoneId resolveZone(String userId) {
        if (!StringUtils.hasText(userId)) {
            return TimeConstants.ZONE_ID;
        }

        return userRepository.findById(userId)
                .map(User::getTimeZone)
                .filter(StringUtils::hasText)
                .map(zone -> {
                    try {
                        return ZoneId.of(zone);
                    } catch (Exception e) {
                        return TimeConstants.ZONE_ID;
                    }
                })
                .orElse(TimeConstants.ZONE_ID);
    }
}
