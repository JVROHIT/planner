# FocusFlow Architecture: The Constitution

## Layer Model
- **Intent**: User's desires and plans (Task, WeeklyPlan).
- **Structure**: The framework of execution (DailyPlan).
- **Truth**: Irreversible facts and historical data (Domain Events, GoalSnapshot).
- **Meaning**: Derived interpretations and progress metrics (StreakState, TrendCalculator).

## Immutability Rules
- **DailyPlan**: Immutable once `closed == true`. No modifications allowed to historical execution truth.
- **History**: Historical records and snapshots are never recomputed. They represent facts at a point in time.
- **WeeklyPlan**: Edits to a `WeeklyPlan` only affect future or current open days. Never alters `closed` DailyPlans.

## Package Contracts
- `task`: Represents **intent units**. Individual items of work.
- `plan`: Represents **structure & execution truth**. How tasks are scheduled and performed.
- `goal`: Represents **directional evaluation**. Long-term objectives and progress tracking.
- `streak`: Represents **behavioral continuity**. Derived status of consistency.
- `analytics`: Represents **interpretation over time**. Meaning extracted from historical truth.

## Event Flow Map
- `TaskCompleted` → `KeyResultEvaluator`: Completed work updates goal progress.
- `DayClosed` → `StreakService`: Closing a day calculates continuity.
- `DayClosed` → `SnapshotService`: Closing a day freezes current progress for history.
- `WeeklyPlanUpdated` → `PlanningService`: Plan changes trigger daily structure adjustments.

## Core Philosophy
> “You may change the future without lying about the past.”

Every modification to the system must respect historical truth. The past is a sequence of immutable events.
