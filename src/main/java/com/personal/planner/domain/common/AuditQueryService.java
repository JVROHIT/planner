package com.personal.planner.domain.common;

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

    public AuditQueryService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public List<AuditEvent> getRecent(String userId, int limit) {
        return auditRepository.findByUserIdOrderByOccurredAtDesc(userId).stream()
                .limit(limit)
                .toList();
    }

    public List<AuditEvent> getForDate(String userId, LocalDate date) {
        return auditRepository.findByUserIdOrderByOccurredAtDesc(userId).stream()
                .filter(e -> e.getOccurredAt().atZone(ZoneId.systemDefault()).toLocalDate().equals(date))
                .toList();
    }
}
