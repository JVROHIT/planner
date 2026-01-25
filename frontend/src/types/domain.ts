/**
 * FocusFlow Domain Types
 *
 * These types map directly to backend domain entities.
 * Philosophy: "You may change the future without lying about the past."
 *
 * Time Layers:
 * - Intent (Week) → What you plan to do
 * - Execution (Today) → What you're actually doing
 * - Truth (History) → What actually happened
 * - Direction (Goals) → Where you're heading
 */

// ===========================================
// Authentication
// ===========================================

export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
}

export interface User {
  id: string;
  email: string;
}

// ===========================================
// Tasks
// ===========================================

/**
 * A Task is a reusable unit of work.
 * Tasks exist independently and can be scheduled across days/weeks.
 */
export interface Task {
  id: string;
  description: string;
  userId: string;
  goalId?: string;
  keyResultId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  description: string;
  goalId?: string;
  keyResultId?: string;
}

export interface UpdateTaskRequest {
  description?: string;
  goalId?: string;
  keyResultId?: string;
}

// ===========================================
// Daily Plan (Execution Layer)
// ===========================================

/**
 * TaskExecution represents the state of a task within a DailyPlan.
 * This is the execution truth - what actually happened.
 */
export interface TaskExecution {
  taskId: string;
  task: Task;
  completed: boolean;
  missed: boolean;
}

/**
 * DailyPlan is the source of truth for a single day.
 * Once closed, it is immutable - the past cannot be changed.
 */
export interface DailyPlan {
  id: string;
  userId: string;
  day: string; // ISO date (YYYY-MM-DD)
  tasks: TaskExecution[];
  closed: boolean;
  createdAt: string;
  updatedAt: string;
}

// ===========================================
// Weekly Plan (Intent Layer)
// ===========================================

/**
 * Day of week enum matching backend.
 */
export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

/**
 * WeeklyPlan represents intent - what you plan to do.
 * Only future days can be modified.
 */
export interface WeeklyPlan {
  id: string;
  userId: string;
  weekNumber: number;
  year: number;
  weekStartDate: string;
  taskGrid: Record<DayOfWeek, string[]>; // DayOfWeek -> taskIds
  createdAt: string;
  updatedAt: string;
}

export interface UpdateWeeklyPlanRequest {
  taskGrid: Record<DayOfWeek, string[]>;
}

// ===========================================
// Goals (Direction Layer)
// ===========================================

/**
 * Goal represents a high-level objective.
 */
export interface Goal {
  id: string;
  userId: string;
  title: string;
  description?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateGoalRequest {
  title: string;
  description?: string;
}

export interface UpdateGoalRequest {
  title?: string;
  description?: string;
  active?: boolean;
}

/**
 * KeyResult types.
 * - ACCUMULATIVE: Progress increases over time (e.g., "Read 12 books")
 * - HABIT: Regular practice (e.g., "Exercise 3x per week")
 * - MILESTONE: Binary completion (e.g., "Launch MVP")
 */
export type KeyResultType = 'ACCUMULATIVE' | 'HABIT' | 'MILESTONE';

/**
 * KeyResult is a measurable outcome under a Goal.
 */
export interface KeyResult {
  id: string;
  goalId: string;
  title: string;
  type: KeyResultType;
  targetValue: number;
  currentValue: number;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateKeyResultRequest {
  title: string;
  type: KeyResultType;
  targetValue: number;
}

export interface UpdateKeyResultRequest {
  title?: string;
  targetValue?: number;
  currentValue?: number;
}

// ===========================================
// Snapshots & Analytics (Meaning Layer)
// ===========================================

/**
 * GoalSnapshot captures the state of a goal at a point in time.
 * Snapshots are append-only - they represent derived meaning.
 */
export interface GoalSnapshot {
  id: string;
  goalId: string;
  date: string;
  actual: number;
  expected: number;
  createdAt: string;
}

/**
 * Trend indicates direction of progress.
 */
export type Trend = 'UP' | 'FLAT' | 'DOWN';

/**
 * Status relative to expected progress.
 */
export type ProgressStatus = 'AHEAD' | 'ON_TRACK' | 'BEHIND';

// ===========================================
// Dashboard Types
// ===========================================

/**
 * Summary of a goal for dashboard display.
 */
export interface GoalSummary {
  goalId: string;
  title: string;
  averageProgress: number;
  status: ProgressStatus;
  trend: Trend;
}

/**
 * Today's dashboard - the execution view.
 */
export interface TodayDashboard {
  userId: string;
  todayPlan: DailyPlan | null;
  completionRatio: number;
  currentStreak: number;
  goalSummaries: GoalSummary[];
}

/**
 * Day progress for week view.
 */
export interface DayProgress {
  date: string;
  dayOfWeek: DayOfWeek;
  completed: number;
  total: number;
  closed: boolean;
}

/**
 * Week dashboard data.
 */
export interface WeekDashboard {
  weekNumber: number;
  year: number;
  weekStartDate: string;
  days: DayProgress[];
}

/**
 * Goal detail for goals dashboard.
 */
export interface GoalDetail {
  goal: Goal;
  keyResults: KeyResult[];
  latestSnapshot: GoalSnapshot | null;
  status: ProgressStatus;
  trend: Trend;
  actualPercent: number;
  expectedPercent: number;
}

/**
 * Goals dashboard data.
 */
export interface GoalsDashboard {
  goals: GoalDetail[];
}

// ===========================================
// Streak
// ===========================================

export interface StreakResponse {
  currentStreak: number;
  longestStreak: number;
}

// ===========================================
// User Preferences
// ===========================================

export interface UserPreferences {
  userId: string;
  weekStartDay: DayOfWeek;
  timezone: string;
  notificationsEnabled: boolean;
}

export interface UpdatePreferencesRequest {
  weekStartDay?: DayOfWeek;
  timezone?: string;
  notificationsEnabled?: boolean;
}
// ===========================================
// Audit & History
// ===========================================

/**
 * AuditEvent types matching backend.
 */
export type AuditEventType =
  | 'TASK_CREATED'
  | 'TASK_COMPLETED'
  | 'DAY_CLOSED'
  | 'WEEKLY_PLAN_UPDATED';

/**
 * AuditEvent represents a historical fact.
 */
export interface AuditEvent {
  id: string;
  userId: string;
  type: AuditEventType;
  payload: Record<string, unknown>;
  occurredAt: string; // ISO timestamp
}
