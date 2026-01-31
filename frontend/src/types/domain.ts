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
  timeZone?: string;
  weekStart?: string;
  goals?: CreateGoalRequest[];
}

export interface AuthResponse {
  token: string;
  userId: string;
}

export interface User {
  id: string;
  email: string;
  timeZone?: string;
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
  title: string;
  notes?: string;
  categoryId?: string;
  priority?: TaskPriority;
  startDate?: string;
  endDate?: string;
  source?: TaskSource;
  userId: string;
  goalId?: string;
  keyResultId?: string;
  contribution?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  title: string;
  notes?: string;
  categoryId?: string;
  priority?: TaskPriority;
  startDate?: string;
  endDate?: string;
  source?: TaskSource;
  goalId?: string;
  keyResultId?: string;
  contribution?: number;
}

export interface UpdateTaskRequest {
  title?: string;
  notes?: string;
  categoryId?: string;
  priority?: TaskPriority;
  startDate?: string;
  endDate?: string;
  source?: TaskSource;
  goalId?: string;
  keyResultId?: string;
  contribution?: number;
}

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TaskSource = 'WEEKLY_PLAN' | 'DAILY_ADD' | 'QUICK_ADD';

// ===========================================
// Daily Plan (Execution Layer)
// ===========================================

/**
 * DailyPlanEntry represents the state of a task within a DailyPlan.
 * This is the execution truth - what actually happened.
 */
export interface DailyPlanEntry {
  taskId: string;
  title?: string;
  status: 'PENDING' | 'COMPLETED' | 'MISSED';
}

/**
 * DailyPlan is the source of truth for a single day.
 * Once closed, it is immutable - the past cannot be changed.
 */
export interface DailyPlan {
  id: string;
  userId: string;
  day: string; // ISO date (YYYY-MM-DD)
  entries: DailyPlanEntry[];
  closed: boolean;
  total?: number;
  completed?: number;
  ratio?: number;
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
  weekStart: string;
  taskGrid: Record<string, string[]>; // date -> taskIds
  updatedAt: string;
}

export interface UpdateWeeklyPlanRequest {
  weekStart: string;
  taskGrid: Record<string, string[]>;
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
  horizon: 'MONTH' | 'QUARTER' | 'YEAR';
  startDate: string;
  endDate: string;
  status: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
}

export interface CreateGoalRequest {
  title: string;
  horizon?: 'MONTH' | 'QUARTER' | 'YEAR';
  startDate?: string;
  endDate?: string;
  status?: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
}

export interface UpdateGoalRequest {
  title?: string;
  horizon?: 'MONTH' | 'QUARTER' | 'YEAR';
  startDate?: string;
  endDate?: string;
  status?: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
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
  startValue: number;
  targetValue: number;
  currentValue: number;
  weight: number;
}

export interface CreateKeyResultRequest {
  title: string;
  type: KeyResultType;
  startValue?: number;
  targetValue: number;
  currentValue?: number;
  weight?: number;
}

export interface UpdateKeyResultRequest {
  title?: string;
  startValue?: number;
  targetValue?: number;
  currentValue?: number;
  weight?: number;
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
  weekStart: string;
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
