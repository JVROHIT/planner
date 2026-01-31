package com.personal.planner.domain.common;

import com.personal.planner.domain.user.UserTimeZoneService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Read-only access to factual history.
 */
@Service
public class AuditQueryService {

    private final AuditRepository auditRepository;
    private final UserTimeZoneService timeZoneService;

    public AuditQueryService(AuditRepository auditRepository, UserTimeZoneService timeZoneService) {
        this.auditRepository = auditRepository;
        this.timeZoneService = timeZoneService;
    }

    public List<AuditEvent> getRecent(String userId, int limit) {
        return auditRepository.findByUserIdOrderByOccurredAtDesc(userId).stream()
                .limit(limit)
                .toList();
    }

    public List<AuditEvent> getForDate(String userId, LocalDate date) {
        ZoneId userZone = timeZoneService.resolveZone(userId);
        return auditRepository.findByUserIdOrderByOccurredAtDesc(userId).stream()
                .filter(e -> e.getOccurredAt().atZone(userZone).toLocalDate().equals(date))
                .toList();
    }
}
