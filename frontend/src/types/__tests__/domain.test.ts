import { describe, it, expect } from 'vitest';
import type {
  Task,
  DailyPlan,
  WeeklyPlan,
  Goal,
  KeyResult,
  TodayDashboard,
  GoalSummary,
  TaskExecution,
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
        description: 'Test task',
        userId: 'user-1',
        goalId: 'goal-1',
        keyResultId: 'kr-1',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      expect(task.id).toBe('123');
      expect(task.description).toBe('Test task');
      expect(task.userId).toBe('user-1');
      expect(task.goalId).toBe('goal-1');
      expect(task.keyResultId).toBe('kr-1');
    });

    it('Task type allows optional goalId and keyResultId', () => {
      const task: Task = {
        id: '123',
        description: 'Test task',
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
        description: 'Task',
        userId: 'u1',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      const taskExecution: TaskExecution = {
        taskId: 't1',
        task: mockTask,
        completed: true,
        missed: false,
      };

      const dailyPlan: DailyPlan = {
        id: 'dp-1',
        userId: 'user-1',
        day: '2024-01-15',
        tasks: [taskExecution],
        closed: false,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      expect(dailyPlan.id).toBe('dp-1');
      expect(dailyPlan.day).toBe('2024-01-15');
      expect(dailyPlan.closed).toBe(false);
      expect(dailyPlan.tasks).toHaveLength(1);
      expect(dailyPlan.tasks[0].completed).toBe(true);
    });
  });

  describe('WeeklyPlan type', () => {
    it('WeeklyPlan taskGrid maps days to task IDs', () => {
      const weeklyPlan: WeeklyPlan = {
        id: 'wp-1',
        userId: 'user-1',
        weekNumber: 3,
        year: 2024,
        weekStartDate: '2024-01-15',
        taskGrid: {
          MONDAY: ['t1', 't2'],
          TUESDAY: ['t3'],
          WEDNESDAY: [],
          THURSDAY: ['t4'],
          FRIDAY: [],
          SATURDAY: [],
          SUNDAY: [],
        },
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      expect(weeklyPlan.taskGrid.MONDAY).toEqual(['t1', 't2']);
      expect(weeklyPlan.weekNumber).toBe(3);
      expect(weeklyPlan.year).toBe(2024);
    });
  });

  describe('Goal and KeyResult types', () => {
    it('Goal type has required fields', () => {
      const goal: Goal = {
        id: 'g1',
        userId: 'u1',
        title: 'Learn TypeScript',
        description: 'Become proficient in TS',
        active: true,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      expect(goal.title).toBe('Learn TypeScript');
      expect(goal.active).toBe(true);
    });

    it('KeyResult type supports all KeyResultType values', () => {
      const types: KeyResultType[] = ['ACCUMULATIVE', 'HABIT', 'MILESTONE'];

      types.forEach((type) => {
        const kr: KeyResult = {
          id: 'kr1',
          goalId: 'g1',
          title: 'Test KR',
          type,
          targetValue: 100,
          currentValue: 50,
          completed: false,
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z',
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
