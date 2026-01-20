package com.personal.planner.domain.common;

import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Persistence boundary for AuditEvent.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * - Supports only creation and read operations.
 * </p>
 */
@Repository
public interface AuditRepository {
    AuditEvent save(AuditEvent event);

    List<AuditEvent> findByUserIdOrderByOccurredAtDesc(String userId);
    // // findByDate logic would go here in implementation
}
