# **FocusFlow -- Core Architecture & Domain Model Specification**

This document captures the complete mental model, architecture, and
domain behavior of the FocusFlow system as discussed. It is the
authoritative reference for:

-   How intent becomes execution

-   How planning works

-   How goals, key results, and progress are modeled

-   How events drive system behavior

-   How analytics, streaks, and trends are derived

-   How all models relate to one another

This is not UI design. This is the *cognitive engine* of the product.

## **1. Core Product Philosophy**

FocusFlow is not a "todo app."\
It is a **time-layered intent system** built around:

Goal (Direction)

↓

Weekly Plan (Commitment)

↓

Task (Unit of intent)

↓

Daily Plan (Execution truth)

↓

Analytics / Streaks / Trends (Meaning)

The system enforces a crucial separation:

  -------------------------------------------------------------------------
**Layer**       **Nature**    **Mutable?**   **Purpose**
  --------------- ------------- -------------- ----------------------------
Goal / Key      Directional   Yes            "Where am I going?"
Result

WeeklyPlan      Intent        Yes            "What do I commit to this
week?"

Task            Intent unit   Yes            "What work exists?"

DailyPlan       Execution     No (after      "What actually happened
truth         close)         today?"

Snapshots /     History       No             "What does this mean over
Stats                                        time?"
  -------------------------------------------------------------------------

This separation ensures:

-   You can change the future without rewriting the past

-   Streaks and analytics remain truthful

-   Behavior over time is preserved

-   Goals remain aspirational, not brittle

## **2. Core Domain Models**

### **2.1 Task (Intent Unit)**

{

\"id\": \"t1\",

\"userId\": \"u1\",

\"title\": \"Solve 5 array problems\",

\"notes\": \"\",

\"categoryId\": \"learning\",

\"priority\": \"MEDIUM\",

\"startDate\": \"2026-01-20\",

\"endDate\": \"2026-01-20\",

\"source\": \"weekly-plan \| daily-add \| quick-add\",

\"goalId\": \"g1\", // optional

\"keyResultId\": \"kr1\", // optional

\"contribution\": 5, // for accumulative KRs

\"createdAt\": \"\...\",

\"updatedAt\": \"\...\"

}

-   Tasks represent **intent**, not execution history.

-   They do not store per-day outcomes.

### **2.2 WeeklyPlan (Editable Intent Grid)**

There is exactly **one WeeklyPlan per user per week**.

{

\"id\": \"wp-2026-W04\",

\"userId\": \"u1\",

\"weekStart\": \"2026-01-20\",

\"grid\": {

\"2026-01-20\": \[\"t1\", \"t2\"\],

\"2026-01-21\": \[\"t3\"\],

\"2026-01-22\": \[\],

\"2026-01-23\": \[\"t4\"\]

},

\"updatedAt\": \"\...\"

}

-   Represents *current intent* for a horizon.

-   Editable at any time.

-   Changes only affect **non-closed days**.

### **2.3 DailyPlan (Execution Truth)**

{

\"id\": \"dp-2026-01-20\",

\"userId\": \"u1\",

\"date\": \"2026-01-20\",

\"entries\": \[

{

\"taskId\": \"t1\",

\"title\": \"Solve 5 array problems\",

\"status\": \"completed\" // pending \| completed \| missed

},

{

\"taskId\": \"t2\",

\"title\": \"Gym\",

\"status\": \"missed\"

}

\],

\"total\": 2,

\"completed\": 1,

\"ratio\": 0.5,

\"closed\": true

}

-   Authoritative truth for a date.

-   Once closed = true, it is immutable.

-   Not recomputed from tasks.

### **2.4 Goals & Key Results**

#### **Goal**

{

\"id\": \"g1\",

\"userId\": \"u1\",

\"title\": \"Get strong at LeetCode\",

\"horizon\": \"MONTH \| QUARTER \| YEAR\",

\"startDate\": \"2026-01-01\",

\"endDate\": \"2026-03-31\",

\"status\": \"ACTIVE\"

}

#### **KeyResult**

{

\"id\": \"kr1\",

\"goalId\": \"g1\",

\"title\": \"Solve 300 problems\",

\"type\": \"ACCUMULATIVE \| HABIT \| MILESTONE\",

\"startValue\": 0,

\"targetValue\": 300,

\"currentValue\": 42,

\"weight\": 1.0

}

## **3. Planning Service**

The Planning Service is **structural**, not analytical.

It answers one question:

> "Given the current intent, what does each day structurally contain?"

Responsibilities:

1.  Materialize WeeklyPlan → DailyPlan

2.  Inject ad-hoc tasks into open days

3.  Maintain per-day structure

4.  Close days and emit DayClosed

It does **not**:

-   Compute streaks

-   Compute percentages for goals

-   Interpret success/failure

-   Aggregate history

## **4. Events**

Domain events are *facts*, not interpretations.

  ------------------------------------------------------------------------
**Event**              **Emitted When**         **Meaning**
  ---------------------- ------------------------ ------------------------
TaskCreated            Task is created          New intent exists

TaskCompleted          Task marked complete     Effort occurred

WeeklyPlanUpdated      Weekly grid edited       Future intent changed

DailyPlanUpdated       DailyPlan modified       Day structure changed

DayClosed              End of day               History boundary
------------------------------------------------------------------------

Spring in-process events are sufficient.

## **5. Interpretation Layer**

Downstream services listen to events and derive meaning.

### **5.1 Streak & Daily Analytics**

Triggered by:

-   DayClosed

Behavior:

-   Read closed DailyPlan

-   Evaluate streak rules

-   Update:

    -   StreakState

    -   Optional DailyStats

## **6. Goals: Evaluation Strategies**

Each KeyResult defines how it reacts to events.

### **6.1 Accumulative KR**

Example: "Solve 300 problems"

-   On TaskCompleted:

    -   If task.keyResultId == kr.id:

        -   currentValue += task.contribution

-   On DayClosed: no-op

Progress:

currentValue / targetValue

### **6.2 Habit KR**

Example: "Gym ≥ 3x/week for 12 weeks"

{

\"type\": \"HABIT\",

\"metric\": \"gym\",

\"targetDays\": 36,

\"currentDays\": 14

}

-   On DayClosed:

    -   Inspect that day's DailyPlan

    -   If at least one matching entry is completed:

        -   currentDays += 1

Progress:

currentDays / targetDays

### **6.3 Milestone KR**

Example: "Pass learner's test"

{

\"type\": \"MILESTONE\",

\"current\": 0,

\"target\": 1

}

-   On TaskCompleted (or manual action):

    -   Set current = 1

-   On DayClosed: no-op

Progress:

current / target // 0 or 1

## **7. Goal Snapshots**

A GoalSnapshot captures trajectory:

{

\"goalId\": \"g1\",

\"date\": \"2026-01-20\",

\"actual\": 0.22,

\"expected\": 0.33

}

On DayClosed:

1.  For each active goal:

    -   Compute actual progress

    -   Compute expected progress:

expected =

(today - startDate) / (endDate - startDate)

2.  Persist GoalSnapshot

This provides:

-   History

-   Trend computation

-   "Ahead / On-track / Behind" logic

## **8. Trends**

Trend is derived from snapshots.

delta = progress_today - progress_N_days_ago

if delta \> +2% → UP

if -2% ≤ delta ≤ +2% → FLAT

if delta \< -2% → DOWN

Trend answers:

-   "Which way am I moving?"

## **9. Dashboards**

### **Daily View**

-   Reads DailyPlan(today)

-   Shows:

    -   Tasks

    -   Progress

    -   Streak

### **Weekly Dashboard**

-   Reads:

    -   7 DailyPlans

-   Shows:

    -   Per-day completion

    -   Consistency

    -   Week trend

### **Goals Dashboard**

-   Reads:

    -   Goals

    -   KeyResults

    -   GoalSnapshots

-   Shows:

    -   Actual %

    -   Expected %

    -   Status: Ahead / On-track / Behind

    -   Trend

    -   Recent contributions

## **10. System Contract**

-   Planning Service:

    -   Intent → Structure

-   Analytics:

    -   Structure → Meaning

-   Goals:

    -   Events → Directional progress

-   History is never rewritten

-   The past is immutable

-   The future is always editable

This architecture encodes:

> "You may change the future without lying about the past."

That is the essence of FocusFlow.
