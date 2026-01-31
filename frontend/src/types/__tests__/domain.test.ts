import { describe, it, expect } from 'vitest';
import type {
  Task,
  DailyPlan,
  WeeklyPlan,
  Goal,
  KeyResult,
  TodayDashboard,
  GoalSummary,
  DailyPlanEntry,
  DayOfWeek,
  KeyResultType,
  Trend,
  ProgressStatus,
} from '../domain';

/**
 * Type validation tests.
 * These tests verify that our domain types match expected shapes.
 * They compile-time check type correctness.
 */
describe('Domain Types', () => {
  describe('Task type', () => {
    it('Task type matches expected shape', () => {
      const task: Task = {
        id: '123',
        title: 'Test task',
        userId: 'user-1',
        goalId: 'goal-1',
        keyResultId: 'kr-1',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      expect(task.id).toBe('123');
      expect(task.title).toBe('Test task');
      expect(task.userId).toBe('user-1');
      expect(task.goalId).toBe('goal-1');
      expect(task.keyResultId).toBe('kr-1');
    });

    it('Task type allows optional goalId and keyResultId', () => {
      const task: Task = {
        id: '123',
        title: 'Test task',
        userId: 'user-1',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      expect(task.goalId).toBeUndefined();
      expect(task.keyResultId).toBeUndefined();
    });
  });

  describe('DailyPlan type', () => {
    it('DailyPlan type includes required fields', () => {
      const mockTask: Task = {
        id: 't1',
        title: 'Task',
        userId: 'u1',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      const taskExecution: DailyPlanEntry = {
        taskId: 't1',
        title: mockTask.title,
        status: 'COMPLETED',
      };

      const dailyPlan: DailyPlan = {
        id: 'dp-1',
        userId: 'user-1',
        day: '2024-01-15',
        entries: [taskExecution],
        closed: false,
      };

      expect(dailyPlan.id).toBe('dp-1');
      expect(dailyPlan.day).toBe('2024-01-15');
      expect(dailyPlan.closed).toBe(false);
      expect(dailyPlan.entries).toHaveLength(1);
      expect(dailyPlan.entries[0].status).toBe('COMPLETED');
    });
  });

  describe('WeeklyPlan type', () => {
    it('WeeklyPlan taskGrid maps days to task IDs', () => {
      const weeklyPlan: WeeklyPlan = {
        id: 'wp-1',
        userId: 'user-1',
        weekStart: '2024-01-15',
        taskGrid: {
          '2024-01-15': ['t1', 't2'],
          '2024-01-16': ['t3'],
          '2024-01-17': [],
          '2024-01-18': ['t4'],
        },
        updatedAt: '2024-01-01T00:00:00Z',
      };

      expect(weeklyPlan.taskGrid['2024-01-15']).toEqual(['t1', 't2']);
      expect(weeklyPlan.weekStart).toBe('2024-01-15');
    });
  });

  describe('Goal and KeyResult types', () => {
    it('Goal type has required fields', () => {
      const goal: Goal = {
        id: 'g1',
        userId: 'u1',
        title: 'Learn TypeScript',
        horizon: 'MONTH',
        startDate: '2024-01-01',
        endDate: '2024-02-01',
        status: 'ACTIVE',
      };

      expect(goal.title).toBe('Learn TypeScript');
      expect(goal.status).toBe('ACTIVE');
    });

    it('KeyResult type supports all KeyResultType values', () => {
      const types: KeyResultType[] = ['ACCUMULATIVE', 'HABIT', 'MILESTONE'];

      types.forEach((type) => {
        const kr: KeyResult = {
          id: 'kr1',
          goalId: 'g1',
          title: 'Test KR',
          type,
          startValue: 0,
          targetValue: 100,
          currentValue: 50,
          weight: 1,
        };
        expect(kr.type).toBe(type);
      });
    });
  });

  describe('Dashboard types', () => {
    it('TodayDashboard includes all required fields', () => {
      const summary: GoalSummary = {
        goalId: 'g1',
        title: 'Goal 1',
        averageProgress: 0.75,
        status: 'ON_TRACK',
        trend: 'UP',
      };

      const dashboard: TodayDashboard = {
        userId: 'u1',
        todayPlan: null,
        completionRatio: 0.8,
        currentStreak: 5,
        goalSummaries: [summary],
      };

      expect(dashboard.completionRatio).toBe(0.8);
      expect(dashboard.currentStreak).toBe(5);
      expect(dashboard.goalSummaries[0].status).toBe('ON_TRACK');
    });

    it('Trend type supports all values', () => {
      const trends: Trend[] = ['UP', 'FLAT', 'DOWN'];
      trends.forEach((t) => expect(t).toBeDefined());
    });

    it('ProgressStatus type supports all values', () => {
      const statuses: ProgressStatus[] = ['AHEAD', 'ON_TRACK', 'BEHIND'];
      statuses.forEach((s) => expect(s).toBeDefined());
    });
  });

  describe('DayOfWeek type', () => {
    it('DayOfWeek includes all days', () => {
      const days: DayOfWeek[] = [
        'MONDAY',
        'TUESDAY',
        'WEDNESDAY',
        'THURSDAY',
        'FRIDAY',
        'SATURDAY',
        'SUNDAY',
      ];
      expect(days).toHaveLength(7);
    });
  });
});
