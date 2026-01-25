FocusFlow -- Frontend Code Principles\
(Editable Text Document)

This document defines how all frontend code in FocusFlow must be
written.\
These are not style preferences. They are architectural laws that
protect the product's philosophy:

"You may change the future without lying about the past."

The frontend is not a "view layer."\
It is a cognitive surface over a time-layered system.

Any frontend code that collapses intent, execution, and meaning into one
mental plane is wrong.

────────────────────────────────────────\
CORE FRONTEND PHILOSOPHY\
────────────────────────────────────────

1.  The frontend must preserve time as a first-class concept

2.  Intent ≠ Execution ≠ Meaning

3.  The past is immutable

4.  The future is always editable

5.  History is never recomputed

6.  Meaning is derived, not inferred

7.  The UI must never lie about what happened

Every component, hook, and screen must respect these truths.

────────────────────────────────────────\
ARCHITECTURAL RULES\
────────────────────────────────────────

1.  Screen Ownership

-   Each screen has exactly ONE authoritative query

-   No screen may stitch data from multiple domains

-   No screen may recompute backend meaning

2.  Data Flow

-   Query = Projection of truth or intent

-   Mutation = Expression of intent

-   Invalidation = "A new fact exists"

3.  Layer Isolation

-   Today screen: ONLY DailyPlan

-   Week screen: ONLY WeeklyPlan

-   Goals screen: ONLY Goals + Snapshots

-   History screen: ONLY DailyPlan(date)

Never mix these.

4.  No Client-Side Derivation

-   Do not compute streaks in UI

-   Do not compute progress in UI

-   Do not infer "ahead/behind" in UI

-   Render what backend asserts

5.  Time Semantics

-   UI must treat closed days as immutable

-   Past is read-only everywhere

-   Future is always editable

-   Visual language must reflect this

────────────────────────────────────────\
COMPONENT DESIGN RULES\
────────────────────────────────────────

1.  No God Components

-   A component should not exceed \~150 lines

-   If logic grows, split by responsibility

2.  One Responsibility Per Component\
    > Examples:

-   TaskRow → renders a task entry

-   DayColumn → renders a day in Week view

-   GoalCard → renders one goal

3.  No Business Logic in Components

-   Components render data

-   Hooks orchestrate data

-   Backend defines truth

4.  Hooks Over Helpers

-   All data access goes through hooks

    -   useTodayPlan

    -   useWeeklyPlan

    -   useGoals

No raw fetch in components.

5.  Stateless by Default

-   Components should be pure

-   Local state only for UI affordances (open/close, drag, hover)

-   Never mirror backend state in local state

────────────────────────────────────────\
STATE MANAGEMENT RULES\
────────────────────────────────────────

1.  Use TanStack Query as the Source of Truth

-   Server state lives in queries

-   Local state is ephemeral

2.  Never Duplicate Server State

-   Do not copy query data into useState

-   Do not maintain parallel stores

3.  Mutations Are Intent

-   Every mutation represents "I want this to change"

-   UI must optimistically reflect intent, then reconcile with truth

4.  Cache Invalidation Is Meaningful

-   Invalidate only what changed

-   Do not globally refetch everything

────────────────────────────────────────\
NAMING CONVENTIONS\
────────────────────────────────────────

-   Screens: TodayPage, WeekPage, GoalsPage, HistoryPage

-   Hooks: useTodayPlan, useWeeklyPlan, useGoals

-   Components: TaskRow, DayColumn, GoalCard

-   Mutations: useCompleteTask, useUpdateWeek

Names must reflect domain meaning, not UI mechanics.

Bad:

-   useDashboardData

-   TaskItem2

-   DataFetcher

Good:

-   useDailyPlan

-   WeeklyGrid

-   GoalSnapshotCard

────────────────────────────────────────\
ERROR HANDLING\
────────────────────────────────────────

1.  Errors are Domain Signals

-   401 → Auth boundary

-   403 → Ownership violation

-   409 → Domain violation

-   400 → Invalid intent

-   500 → System fault

2.  UI Must Reflect Meaning

-   "This day is closed" is not a generic error

-   "You don't own this task" is not "Something went wrong"

3.  No Silent Failures

-   Every error must be visible or logged

-   Never swallow API failures

────────────────────────────────────────\
ANTI-PATTERNS (FORBIDDEN)\
────────────────────────────────────────

-   Computing streaks in UI

-   Reconstructing history from tasks

-   Editing past days

-   Mixing WeeklyPlan and DailyPlan

-   Local shadow copies of server state

-   "Smart" components that derive meaning

-   Global mutable stores for domain state

-   "Just recompute on client" shortcuts

These break the ontology of FocusFlow.

────────────────────────────────────────\
MODERN UX & HCI STANDARDS (MANDATORY)\
────────────────────────────────────────

All frontend implementation must follow modern UX and HCI principles
that are industry standards.\
This is not optional polish---it is part of the product's correctness.

1.  Cognitive Load Minimization

-   Every screen must answer exactly one mental question

-   No screen should require the user to "hold state in their head"

-   Progressive disclosure is mandatory

-   Do not show future layers prematurely (e.g., analytics inside
    > execution view)

2.  Feedback & Affordance

-   Every user action must produce immediate, visible feedback

-   Mutations must feel intentional:

    -   Loading → "Intent in progress"

    -   Success → "Fact recorded"

    -   Failure → "Intent rejected"

-   Disabled or immutable elements must look immutable

3.  Consistency Over Cleverness

-   Same action must look and behave the same across the app

-   No hidden gestures or magic interactions

-   Predictability beats novelty

4.  Error as Guidance

-   Errors are part of the conversation

-   Messages must explain what boundary was hit:

    -   "This day is closed"

    -   "You can't edit the past"

    -   "This task belongs to another user"

-   Never show generic "Something went wrong" unless truly unknown

5.  Temporal Legibility

-   The UI must visually encode time:

    -   Past = frozen

    -   Present = active

    -   Future = flexible

-   Users should feel the difference between:

    -   "What I did"

    -   "What I'm doing"

    -   "What I intend"

6.  Industry Standards\
    > All UI must conform to:

-   Fitts's Law (primary actions easily reachable)

-   Hick's Law (limit visible choices per screen)

-   Nielsen's Heuristics:

    -   Visibility of system status

    -   Match between system and real world

    -   User control and freedom

    -   Error prevention

    -   Recognition over recall

    -   Consistency and standards

    -   Aesthetic and minimalist design

-   Accessibility:

    -   Keyboard navigable

    -   Screen-reader friendly

    -   Color contrast compliant

Any implementation that:

-   Feels cluttered

-   Requires memorization

-   Hides system state

-   Makes time ambiguous

-   Treats errors as noise

is architecturally incorrect, even if it "works".

FocusFlow's UI must feel:

-   Calm

-   Honest

-   Predictable

-   Time-aware

The frontend does not decide.\
It reveals intent, renders truth, and displays meaning.

That is its only job.
