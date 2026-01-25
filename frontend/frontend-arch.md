FocusFlow -- Frontend Architecture & Interaction Model\
(Editable Text Document)

This document defines the frontend cognitive architecture for
FocusFlow.\
It is not visual design. It is the interaction model that preserves the
system's core philosophy:

"You may change the future without lying about the past."

The frontend must not collapse intent, execution, and meaning into a
single mental surface.\
Each screen corresponds to a different layer of time and truth.

────────────────────────────────────────\
CORE PRINCIPLES\
────────────────────────────────────────

The UI must enforce these invariants:

1.  Intent ≠ Execution ≠ Meaning

2.  The past is immutable

3.  The future is always editable

4.  History is never recomputed

5.  Meaning is derived, not asserted

6.  No screen mixes layers

Violating these turns FocusFlow into "just another todo app."

────────────────────────────────────────\
SCREEN MAP (BY DOMAIN LAYER)\
────────────────────────────────────────

Login\
Route: /login\
Source of Truth: Auth API\
Mental Mode: Access

Signup\
Route: /signup\
Source of Truth: Auth API\
Mental Mode: Onboarding

Today\
Route: /today\
Source of Truth: DailyPlan(today)\
Mental Mode: Execution

Week\
Route: /week\
Source of Truth: WeeklyPlan\
Mental Mode: Intent

Goals\
Route: /goals\
Source of Truth: Goals + Snapshots\
Mental Mode: Direction

History\
Route: /history/:date\
Source of Truth: DailyPlan(date)\
Mental Mode: Truth

Each screen has exactly one authoritative query.\
No screen stitches multiple domains ad-hoc.

────────────────────────────────────────\
SCREENS\
────────────────────────────────────────

1.  Login / Signup (Access Mode)

Routes: /login, /signup\
Purpose: Identity + trust boundary.

UI:

-   Email / Password fields

-   Error states (401, 409, 400)

-   Success redirects to /today

Rules:

-   Stateless forms

-   No domain data loaded

-   JWT stored via httpOnly cookies

-   All errors mapped directly from API

2.  Today (Execution Mode)

Route: /today\
Source: GET /api/dashboard/today → DailyPlan(today)

Purpose:\
"What actually exists today, and what did I do?"

UI:

-   List of DailyPlan.entries

-   Each entry shows:

    -   Title

    -   Status: pending / completed / missed

-   Summary bar:

    -   completed / total

    -   Ratio

    -   Current streak

-   Actions:

    -   Mark complete

    -   Add ad-hoc task (into open DailyPlan)

Rules:

-   If closed = true → read-only

-   Never read from Task or WeeklyPlan

-   Never recompute

-   This screen is execution truth

3.  Week (Intent Mode)

Route: /week\
Source: GET /api/dashboard/week → WeeklyPlan

Purpose:\
"What am I committing to this week?"

UI:

-   7-column grid (Mon--Sun)

-   Each cell shows task titles

-   Drag tasks between days

-   Add/remove tasks

-   Closed days visually frozen

Rules:

-   Edits affect future days only

-   Closed days are non-editable

-   Mutations emit WeeklyPlanUpdated

-   This screen is editable intent

4.  Goals (Direction Mode)

Route: /goals\
Source: GET /api/dashboard/goals

Purpose:\
"Where am I going, and how am I doing?"

UI:

-   Goal cards showing:

    -   Title

    -   Actual %

    -   Expected %

    -   Status: Ahead / On-track / Behind

    -   Trend: ↑ / → / ↓

-   Expand:

    -   KeyResults

    -   Progress bars

    -   Recent contributions

Rules:

-   Render snapshot-derived meaning

-   Never recompute progress client-side

-   Never infer from Tasks

-   This screen is interpretation

5.  History (Truth Mode)

Route: /history/:date\
Source: GET /api/history/:date → DailyPlan(date)

Purpose:\
"What actually happened that day?"

UI:

-   Same layout as Today

-   Read-only

-   Shows:

    -   Entries

    -   Completion

    -   Ratio

-   Optional overlay:

    -   "You were Ahead / On-track / Behind"

Rules:

-   Immutable

-   No editing

-   No reshuffling

-   This screen is archival truth

────────────────────────────────────────\
TRANSITIONS & MENTAL FLOW\
────────────────────────────────────────

Goals (Direction)\
↓\
Week (Intent)\
↓\
Today (Execution)\
↓\
History (Truth)\
↓\
Goals (Meaning)

From → To \| Mental Shift\
Goals → Week \| Direction → Commitment\
Week → Today \| Commitment → Action\
Today → History \| Action → Fact\
History → Goals \| Fact → Meaning

Each transition must feel like a mode change.

────────────────────────────────────────\
FRONTEND STACK\
────────────────────────────────────────

Framework: Next.js (App Router)\
Data: TanStack Query\
UI: Tailwind + Radix / Headless UI\
Charts: Recharts or Visx\
Auth: JWT via httpOnly cookies\
Animation: Framer Motion

Rationale:

-   Server Components for read-heavy dashboards

-   Client Components for interaction

-   TanStack Query mirrors backend semantics:

    -   Query = projection

    -   Mutation = intent

    -   Invalidation = "new fact arrived"

────────────────────────────────────────\
DATA CONTRACTS (PER SCREEN)\
────────────────────────────────────────

/today

-   Query: GET /api/dashboard/today

-   Mutations:

    -   POST /api/tasks/{id}/complete

    -   POST /api/daily/add

/week

-   Query: GET /api/dashboard/week

-   Mutation:

    -   PUT /api/plan/week

/goals

-   Query: GET /api/dashboard/goals

-   Read-only

/history/:date

-   Query: GET /api/history/:date

-   Read-only

No screen mixes domains.

────────────────────────────────────────\
UI LAWS (NON-NEGOTIABLE)\
────────────────────────────────────────

1.  Today never reads from Tasks

2.  History is always read-only

3.  WeeklyPlan never mutates closed days

4.  Goals never recompute progress

5.  No screen mixes Intent and Truth

6.  No derived meaning is computed client-side

These are architectural invariants, not UX preferences.

FocusFlow's backend encodes time as truth.\
The frontend must render that ontology, not flatten it.