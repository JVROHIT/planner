package com.personal.planner.domain.task;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository service layer for Task.
 */
@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
}
