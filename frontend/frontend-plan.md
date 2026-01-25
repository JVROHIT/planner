# FocusFlow Frontend Implementation Plan

This document defines the phased implementation plan for the FocusFlow frontend.
Each phase contains parallel agents that can operate independently.

**Philosophy:** "You may change the future without lying about the past."

**Tech Stack:** Next.js (App Router), TanStack Query, Tailwind CSS, Radix UI, Framer Motion

**App Type:** Progressive Web App (PWA) - Installable, offline-capable

---

## PWA Requirements

FocusFlow must be a fully functional Progressive Web App:

| Requirement | Implementation |
|-------------|----------------|
| Installable | Web App Manifest with icons, name, theme |
| Offline-capable | Service Worker with cache-first strategy |
| Fast loading | Precache critical assets |
| Add to Home Screen | Manifest + Service Worker |
| Push notifications | (Future) For planning reminders |
| Background sync | (Future) Sync offline mutations |

**PWA Core Features:**
- Install prompt on supported browsers
- Works offline for viewing cached data
- App-like experience (no browser chrome)
- Splash screen on launch
- Theme color matching app design

---

## Dependency Graph

```
Phase 0 (CORS)
    │
    ▼
Phase 1 (Foundation) ─────────────────────────────────┐
    │                                                  │
    ├── Agent 1.1 (Next.js Setup)                     │
    │       │                                          │
    │       ▼                                          │
    ├── Agent 1.2 (API Client) ◄── depends on 1.1     │
    │       │                                          │
    │       ▼                                          │
    ├── Agent 1.3 (Types) ◄── depends on 1.2          │
    │       │                                          │
    │       ▼                                          │
    ├── Agent 1.4 (Layout) ◄── depends on 1.1, 1.2    │
    │       │                                          │
    │       ▼                                          │
    └── Agent 1.5 (PWA Setup) ◄── depends on 1.1      │
            │                                          │
            ▼                                          │
Phase 2 (Auth) ◄── depends on Phase 1 complete ───────┘
    │
    ├── Agent 2.1 (Auth Hooks) ◄── depends on 1.2, 1.3
    │       │
    │       ▼
    └── Agent 2.2 (Auth Pages) ◄── depends on 2.1, 1.4
            │
            ▼
Phase 3 (Today) ◄── depends on Phase 2 complete
    │
    ├── Agent 3.1 (Today Hooks) ◄── depends on 1.2, 1.3, 2.1
    │       │
    │       ▼
    ├── Agent 3.2 (Today Page) ◄── depends on 3.1, 1.4
    │       │
    │       ▼
    └── Agent 3.3 (Today Widgets) ◄── depends on 3.1
            │
            ▼
Phase 4 (Week) ◄── depends on Phase 3 complete
    │
    ├── Agent 4.1 (Week Hooks) ◄── depends on 1.2, 1.3
    │       │
    │       ▼
    ├── Agent 4.2 (Week Page) ◄── depends on 4.1, 1.4
    │       │
    │       ▼
    └── Agent 4.3 (Week Tasks) ◄── depends on 4.1, 4.2
            │
            ▼
Phase 5 (Goals) ◄── depends on Phase 4 complete
    │
    ├── Agent 5.1 (Goals Hooks) ◄── depends on 1.2, 1.3
    │       │
    │       ▼
    ├── Agent 5.2 (Goals Page) ◄── depends on 5.1, 1.4
    │       │
    │       ▼
    └── Agent 5.3 (Key Results) ◄── depends on 5.1, 5.2
            │
            ▼
Phase 6 (History) ◄── depends on Phase 3 complete (reuses Today components)
    │
    ├── Agent 6.1 (History Hooks) ◄── depends on 1.2, 1.3
    │       │
    │       ▼
    └── Agent 6.2 (History Page) ◄── depends on 6.1, 3.2 (reuses TaskRow)
            │
            ▼
Phase 7 (Polish) ◄── depends on Phase 3, 4, 5, 6 complete
    │
    ├── Agent 7.1 (Error Handling) ◄── depends on 1.2
    │       │
    ├── Agent 7.2 (Loading States) ◄── depends on all pages
    │       │
    └── Agent 7.3 (Accessibility) ◄── depends on all pages
            │
            ▼
Phase 8 (Verification) ◄── depends on ALL phases complete
```

**Parallelization Rules:**
- Agents within a phase MAY run in parallel if their internal dependencies are met
- Phases MUST run sequentially (Phase N+1 cannot start until Phase N passes tests)
- Each phase has a gate: **all tests must pass before proceeding**

---

## Testing Strategy

Each phase includes mandatory testing. Tests validate changes before proceeding.

| Test Type | Tool | Purpose |
|-----------|------|---------|
| Unit Tests | Vitest | Test hooks, utilities, pure functions |
| Component Tests | React Testing Library | Test component behavior |
| Integration Tests | Vitest + MSW | Test API integration with mocked backend |
| E2E Tests | Playwright | Test full user flows |

**Test Commands:**
```bash
npm run test          # Unit + Component tests
npm run test:e2e      # Playwright E2E tests
npm run test:all      # All tests
```

---

## Phase 0: Backend CORS Configuration (If Needed)

**Single Agent - Backend Team**

### Agent 0.1 – CORS Configuration

**Scope:**
- `src/main/java/com/personal/planner/infra/security/SecurityConfig.java`

**Responsibilities:**
- Add CORS configuration to allow frontend origin (e.g., `http://localhost:3000`)
- Configure allowed methods: GET, POST, PUT, DELETE, OPTIONS
- Configure allowed headers: Authorization, Content-Type
- Enable credentials for cookie-based auth
- Do NOT weaken existing security rules

**Validation:**
- Frontend can make requests without CORS errors
- Preflight OPTIONS requests succeed

### Phase 0 Gate: Validation
```bash
# Backend test
./gradlew test

# Manual validation
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" -v
# Should return 200 with CORS headers
```

**Exit Criteria:**
- [ ] Backend tests pass
- [ ] CORS preflight returns 200
- [ ] Access-Control-Allow-Origin header present

---

## Phase 1: Project Foundation & Core Infrastructure

**Parallel Agents: 4**

### Agent 1.1 – Next.js Project Setup

**Scope:**
- Project initialization and configuration
- `package.json`, `tsconfig.json`, `next.config.js`
- `tailwind.config.js`, `postcss.config.js`
- `.env.example`, `.env.local` (gitignored)

**Responsibilities:**
- Initialize Next.js 14+ with App Router
- Configure TypeScript (strict mode)
- Set up Tailwind CSS with custom theme
- Configure path aliases (`@/components`, `@/lib`, etc.)
- Add environment variables:
  - `NEXT_PUBLIC_API_URL`
- Install core dependencies:
  - `@tanstack/react-query`
  - `@radix-ui/react-*` (dialog, dropdown, tooltip)
  - `framer-motion`
  - `clsx`, `tailwind-merge`
- Set up `.gitignore` (node_modules, .next, .env.local)

**Deliverables:**
- Working Next.js app with `npm run dev`
- Tailwind styles loading correctly
- TypeScript compiling without errors

---

### Agent 1.2 – API Client & Query Infrastructure

**Scope:**
- `src/lib/api/client.ts`
- `src/lib/api/types.ts`
- `src/providers/QueryProvider.tsx`

**Responsibilities:**
- Create typed API client with fetch wrapper
- Handle `ApiResponse<T>` wrapper from backend:
  ```typescript
  interface ApiResponse<T> {
    success: boolean;
    data: T | null;
    errorCode: string | null;
    message: string | null;
  }
  ```
- Automatic error extraction from response
- JWT token handling via cookies or headers
- Base URL configuration from environment
- Create TanStack Query provider with defaults:
  - `staleTime: 30000`
  - `refetchOnWindowFocus: false`
  - Error boundary integration

**API Client Features:**
- `api.get<T>(url)` → `Promise<T>`
- `api.post<T>(url, body)` → `Promise<T>`
- `api.put<T>(url, body)` → `Promise<T>`
- `api.delete(url)` → `Promise<void>`
- Automatic 401 redirect to `/login`
- Typed error handling

**Deliverables:**
- Type-safe API client
- Query provider wrapping app

---

### Agent 1.3 – Type Definitions

**Scope:**
- `src/types/domain.ts`
- `src/types/api.ts`

**Responsibilities:**
- Define all domain types matching backend:
  ```typescript
  // Auth
  interface AuthRequest { email: string; password: string; }
  interface AuthResponse { token: string; userId: string; }
  
  // Task
  interface Task {
    id: string;
    description: string;
    userId: string;
    goalId?: string;
    keyResultId?: string;
  }
  
  // Daily Plan
  interface DailyPlan {
    id: string;
    userId: string;
    day: string; // ISO date
    tasks: TaskExecution[];
    closed: boolean;
  }
  interface TaskExecution {
    taskId: string;
    completed: boolean;
    missed: boolean;
  }
  
  // Weekly Plan
  interface WeeklyPlan {
    id: string;
    userId: string;
    weekNumber: number;
    year: number;
    taskGrid: Record<string, string[]>; // DayOfWeek -> taskIds
  }
  
  // Goals
  interface Goal {
    id: string;
    title: string;
    description?: string;
    userId: string;
  }
  interface KeyResult {
    id: string;
    goalId: string;
    title: string;
    targetValue: number;
    currentValue: number;
    type: 'ACCUMULATIVE' | 'HABIT' | 'MILESTONE';
  }
  interface GoalSnapshot {
    id: string;
    goalId: string;
    date: string;
    actual: number;
    expected: number;
  }
  
  // Dashboard
  interface TodayDashboard {
    userId: string;
    todayPlan: DailyPlan | null;
    completionRatio: number;
    currentStreak: number;
    goalSummaries: GoalSummary[];
  }
  interface GoalSummary {
    goalId: string;
    title: string;
    averageProgress: number;
  }
  
  // Streak
  interface StreakResponse { currentStreak: number; }
  
  // Trend
  type Trend = 'UP' | 'FLAT' | 'DOWN';
  ```

**Deliverables:**
- Complete type definitions for all API contracts
- No `any` types

---

### Agent 1.4 – Layout & Navigation Shell

**Scope:**
- `src/app/layout.tsx`
- `src/components/layout/AppShell.tsx`
- `src/components/layout/Sidebar.tsx`
- `src/components/layout/Header.tsx`

**Responsibilities:**
- Root layout with providers (Query, Theme)
- App shell with sidebar navigation
- Navigation items:
  - Today (`/today`) - Execution mode
  - Week (`/week`) - Intent mode
  - Goals (`/goals`) - Direction mode
  - History (`/history`) - Truth mode
- Visual indication of current mode
- Logout functionality
- Responsive design (mobile sidebar collapse)
- Mode-aware visual styling:
  - Today = Active/Present
  - Week = Flexible/Future
  - History = Frozen/Past
  - Goals = Directional

**Deliverables:**
- Working navigation between routes
- Consistent layout across pages

---

### Agent 1.5 – PWA Configuration

**Scope:**
- `public/manifest.json`
- `public/icons/` (PWA icons)
- `src/app/manifest.ts` (Next.js 13+ manifest)
- `next.config.js` (PWA plugin config)
- `src/lib/pwa/register.ts`
- `src/components/pwa/InstallPrompt.tsx`
- `src/components/pwa/OfflineIndicator.tsx`

**Dependencies:**
- Install: `next-pwa` or `@ducanh2912/next-pwa`

**Responsibilities:**

1. **Web App Manifest** (`public/manifest.json`):
```json
{
  "name": "FocusFlow",
  "short_name": "FocusFlow",
  "description": "Time-layered intent system for personal productivity",
  "start_url": "/today",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#3b82f6",
  "orientation": "portrait-primary",
  "icons": [
    { "src": "/icons/icon-192.png", "sizes": "192x192", "type": "image/png" },
    { "src": "/icons/icon-512.png", "sizes": "512x512", "type": "image/png" },
    { "src": "/icons/icon-maskable-512.png", "sizes": "512x512", "type": "image/png", "purpose": "maskable" }
  ],
  "screenshots": [
    { "src": "/screenshots/today.png", "sizes": "1280x720", "type": "image/png" }
  ]
}
```

2. **PWA Icons:**
   - `icon-192.png` (192x192)
   - `icon-512.png` (512x512)
   - `icon-maskable-512.png` (512x512, maskable)
   - `apple-touch-icon.png` (180x180)
   - `favicon.ico`

3. **Next.js PWA Configuration** (`next.config.js`):
```javascript
const withPWA = require('next-pwa')({
  dest: 'public',
  register: true,
  skipWaiting: true,
  disable: process.env.NODE_ENV === 'development',
  runtimeCaching: [
    {
      urlPattern: /^https:\/\/api\./,
      handler: 'NetworkFirst',
      options: {
        cacheName: 'api-cache',
        expiration: { maxEntries: 50, maxAgeSeconds: 300 }
      }
    },
    {
      urlPattern: /\.(js|css|png|jpg|jpeg|svg|gif)$/,
      handler: 'CacheFirst',
      options: {
        cacheName: 'static-assets',
        expiration: { maxEntries: 100, maxAgeSeconds: 86400 }
      }
    }
  ]
})

module.exports = withPWA({
  // existing next config
})
```

4. **Service Worker Caching Strategy:**
   - **API calls:** NetworkFirst (try network, fallback to cache)
   - **Static assets:** CacheFirst (use cache, update in background)
   - **Pages:** StaleWhileRevalidate (show cached, fetch fresh)

5. **Install Prompt Component** (`InstallPrompt.tsx`):
   - Detect `beforeinstallprompt` event
   - Show install button when available
   - Track installation status
   - Dismiss and remember preference

6. **Offline Indicator** (`OfflineIndicator.tsx`):
   - Detect online/offline status
   - Show banner when offline
   - Indicate cached data being shown

7. **Meta Tags** (in `src/app/layout.tsx`):
```tsx
<head>
  <meta name="application-name" content="FocusFlow" />
  <meta name="apple-mobile-web-app-capable" content="yes" />
  <meta name="apple-mobile-web-app-status-bar-style" content="default" />
  <meta name="apple-mobile-web-app-title" content="FocusFlow" />
  <meta name="mobile-web-app-capable" content="yes" />
  <meta name="theme-color" content="#3b82f6" />
  <link rel="apple-touch-icon" href="/icons/apple-touch-icon.png" />
  <link rel="manifest" href="/manifest.json" />
</head>
```

**Offline Behavior:**
- Today screen: Show cached tasks (read-only indicator)
- Week screen: Show cached plan (mutations queued)
- Goals screen: Show cached goals (read-only)
- History screen: Show cached history
- Auth screens: Require network

**Deliverables:**
- App installable on desktop and mobile
- Works offline with cached data
- Install prompt shown to users
- Offline indicator visible
- Service worker registered

---

### Phase 1 Gate: Testing

**Test Setup (Agent 1.1 includes):**
```bash
# Install test dependencies
npm install -D vitest @testing-library/react @testing-library/jest-dom jsdom msw
```

**Tests Required:**

1. **API Client Tests** (`src/lib/api/__tests__/client.test.ts`)
```typescript
describe('API Client', () => {
  it('extracts data from successful ApiResponse')
  it('throws ApiError on success: false')
  it('handles 401 by redirecting to login')
  it('includes authorization header when token exists')
  it('constructs correct URLs from base URL')
})
```

2. **Type Validation Tests** (`src/types/__tests__/domain.test.ts`)
```typescript
describe('Domain Types', () => {
  it('Task type matches expected shape')
  it('DailyPlan type includes required fields')
  it('ApiResponse generic works correctly')
})
```

3. **Layout Tests** (`src/components/layout/__tests__/AppShell.test.tsx`)
```typescript
describe('AppShell', () => {
  it('renders navigation links')
  it('highlights current route')
  it('wraps children in providers')
})
```

4. **PWA Tests** (`src/__tests__/pwa.test.ts`)
```typescript
describe('PWA Configuration', () => {
  it('manifest.json exists and is valid JSON')
  it('manifest has required fields (name, short_name, icons, start_url)')
  it('icons exist at specified paths')
  it('theme_color matches app theme')
})

describe('InstallPrompt', () => {
  it('renders install button when beforeinstallprompt fired')
  it('hides when app is already installed')
  it('calls prompt() on button click')
})

describe('OfflineIndicator', () => {
  it('shows when navigator.onLine is false')
  it('hides when navigator.onLine is true')
  it('updates on online/offline events')
})
```

5. **PWA Lighthouse Audit** (manual or CI):
```bash
# Run Lighthouse PWA audit
npx lighthouse http://localhost:3000 --only-categories=pwa --output=json
```

**Run:**
```bash
npm run test
npm run build  # Verify service worker generates
```

**Exit Criteria:**
- [ ] `npm run dev` starts without errors
- [ ] TypeScript compiles with zero errors
- [ ] All Phase 1 tests pass
- [ ] Navigation works between placeholder routes
- [ ] PWA: manifest.json accessible at /manifest.json
- [ ] PWA: Service worker registers in production build
- [ ] PWA: App installable (Lighthouse PWA audit passes)
- [ ] PWA: Offline indicator shows when network disconnected

---

## Phase 2: Authentication Flow

**Parallel Agents: 2**

### Agent 2.1 – Auth Hooks & State

**Scope:**
- `src/hooks/useAuth.ts`
- `src/hooks/useLogin.ts`
- `src/hooks/useRegister.ts`
- `src/lib/auth/storage.ts`

**Responsibilities:**
- `useAuth()` hook:
  - Returns `{ user, isAuthenticated, isLoading, logout }`
  - Checks token validity
- `useLogin()` mutation:
  - POST `/api/auth/login`
  - Store token
  - Redirect to `/today`
- `useRegister()` mutation:
  - POST `/api/auth/register`
  - Store token
  - Redirect to `/today`
- Token storage (localStorage or httpOnly cookie via API)
- Automatic token refresh if needed

**API Endpoints:**
- `POST /api/auth/login` → `{ token, userId }`
- `POST /api/auth/register` → `{ token, userId }`

**Deliverables:**
- Working auth hooks
- Token persistence

---

### Agent 2.2 – Auth Pages

**Scope:**
- `src/app/login/page.tsx`
- `src/app/signup/page.tsx`
- `src/components/auth/LoginForm.tsx`
- `src/components/auth/SignupForm.tsx`

**Responsibilities:**
- Login page:
  - Email/password fields
  - Error display (401, 400)
  - Submit → redirect to `/today`
  - Link to signup
- Signup page:
  - Email/password fields
  - Validation errors (400, 409 duplicate)
  - Submit → redirect to `/today`
  - Link to login
- Form validation (client-side)
- Loading states
- Accessible forms (labels, aria)

**Error Mapping:**
- `401 UNAUTHORIZED` → "Invalid email or password"
- `400 BAD_REQUEST` → Show validation message
- `409 CONFLICT` → "Email already exists"

**Deliverables:**
- Functional login/signup flow
- Proper error handling

---

### Phase 2 Gate: Testing

**Tests Required:**

1. **Auth Hooks Tests** (`src/hooks/__tests__/useAuth.test.ts`)
```typescript
describe('useAuth', () => {
  it('returns isAuthenticated: false when no token')
  it('returns isAuthenticated: true when valid token exists')
  it('logout clears token and redirects')
})

describe('useLogin', () => {
  it('calls POST /api/auth/login with credentials')
  it('stores token on success')
  it('redirects to /today on success')
  it('returns error on 401')
})

describe('useRegister', () => {
  it('calls POST /api/auth/register')
  it('returns error on 409 duplicate email')
})
```

2. **Auth Component Tests** (`src/components/auth/__tests__/LoginForm.test.tsx`)
```typescript
describe('LoginForm', () => {
  it('renders email and password fields')
  it('shows validation error for empty email')
  it('shows error message on failed login')
  it('disables submit while loading')
  it('calls onSubmit with credentials')
})
```

3. **Auth E2E Test** (`e2e/auth.spec.ts`)
```typescript
describe('Authentication Flow', () => {
  it('redirects unauthenticated user to /login')
  it('allows user to register and land on /today')
  it('allows user to login and land on /today')
  it('shows error for invalid credentials')
  it('shows error for duplicate email')
})
```

**Run:**
```bash
npm run test
npm run test:e2e
```

**Exit Criteria:**
- [ ] All Phase 2 tests pass
- [ ] Can register new user via UI
- [ ] Can login existing user via UI
- [ ] Invalid login shows error message
- [ ] Unauthenticated routes redirect to /login

---

## Phase 3: Today Screen (Execution Mode)

**Parallel Agents: 3**

### Agent 3.1 – Today Data Hooks

**Scope:**
- `src/hooks/useTodayDashboard.ts`
- `src/hooks/useCompleteTask.ts`
- `src/hooks/useMissTask.ts`

**Responsibilities:**
- `useTodayDashboard()` query:
  - GET `/api/dashboard/today`
  - Returns `TodayDashboard`
  - Refetch on window focus
- `useCompleteTask()` mutation:
  - POST `/api/daily/{date}/tasks/{taskId}/complete`
  - Optimistic update
  - Invalidate today dashboard
- `useMissTask()` mutation:
  - POST `/api/daily/{date}/tasks/{taskId}/miss`
  - Optimistic update

**Invariants:**
- Never read from Task directly
- Never recompute progress
- Data comes only from DailyPlan

**Deliverables:**
- Working data layer for Today

---

### Agent 3.2 – Today Page & Components

**Scope:**
- `src/app/today/page.tsx`
- `src/components/today/TodayTaskList.tsx`
- `src/components/today/TaskRow.tsx`
- `src/components/today/TodaySummary.tsx`

**Responsibilities:**
- Today page layout:
  - Summary bar (completed/total, ratio, streak)
  - Task list
  - Read-only if day is closed
- TaskRow component:
  - Title
  - Status: pending | completed | missed
  - Complete/Miss actions
  - Visual feedback on action
- TodaySummary component:
  - Completion count
  - Percentage
  - Current streak
- Loading states
- Empty state ("No tasks for today")

**Visual Rules:**
- Closed day = frozen visual (grayed, no actions)
- Completed = success styling
- Missed = muted styling
- Pending = active styling

**Deliverables:**
- Fully functional Today screen

---

### Agent 3.3 – Today Streak & Goal Summary

**Scope:**
- `src/components/today/StreakBadge.tsx`
- `src/components/today/GoalSummaryCard.tsx`

**Responsibilities:**
- StreakBadge:
  - Display current streak count
  - Visual indicator (fire icon, etc.)
- GoalSummaryCard:
  - Goal title
  - Average progress bar
  - Link to goals page
- Both render backend-provided data
- No client-side computation

**Deliverables:**
- Streak and goal widgets for Today

---

### Phase 3 Gate: Testing

**Tests Required:**

1. **Today Hooks Tests** (`src/hooks/__tests__/useTodayDashboard.test.ts`)
```typescript
describe('useTodayDashboard', () => {
  it('fetches from /api/dashboard/today')
  it('returns TodayDashboard shape')
  it('handles loading state')
  it('handles error state')
})

describe('useCompleteTask', () => {
  it('calls POST /api/daily/{date}/tasks/{taskId}/complete')
  it('invalidates today dashboard on success')
  it('performs optimistic update')
  it('rolls back on error')
})

describe('useMissTask', () => {
  it('calls POST /api/daily/{date}/tasks/{taskId}/miss')
  it('invalidates today dashboard on success')
})
```

2. **Today Component Tests** (`src/components/today/__tests__/`)
```typescript
describe('TaskRow', () => {
  it('renders task title')
  it('shows complete button for pending task')
  it('hides actions when day is closed')
  it('applies completed styling when completed')
  it('applies missed styling when missed')
})

describe('TodaySummary', () => {
  it('displays completion count')
  it('displays percentage')
  it('displays current streak')
})

describe('TodayTaskList', () => {
  it('renders list of TaskRow components')
  it('shows empty state when no tasks')
  it('shows loading skeleton while loading')
})
```

3. **Today Page Tests** (`src/app/today/__tests__/page.test.tsx`)
```typescript
describe('TodayPage', () => {
  it('renders summary and task list')
  it('marks task complete on button click')
  it('disables actions when day is closed')
  it('never reads from Task directly (invariant)')
})
```

4. **Today E2E Test** (`e2e/today.spec.ts`)
```typescript
describe('Today Screen', () => {
  it('displays today tasks from API')
  it('marks task as complete')
  it('updates completion ratio after completing task')
  it('shows streak badge')
  it('read-only when day is closed')
})
```

**Run:**
```bash
npm run test
npm run test:e2e
```

**Exit Criteria:**
- [ ] All Phase 3 tests pass
- [ ] Today page loads with tasks from API
- [ ] Can complete a task
- [ ] Completion ratio updates
- [ ] Streak displays correctly
- [ ] Closed day is read-only

---

## Phase 4: Week Screen (Intent Mode)

**Parallel Agents: 3**

### Agent 4.1 – Week Data Hooks

**Scope:**
- `src/hooks/useWeekDashboard.ts`
- `src/hooks/useWeeklyPlan.ts`
- `src/hooks/useUpdateWeeklyPlan.ts`

**Responsibilities:**
- `useWeekDashboard(weekStart)` query:
  - GET `/api/dashboard/week?weekStart={date}`
  - Returns `DayProgress[]`
- `useWeeklyPlan(date)` query:
  - GET `/api/weekly-plan/{date}`
  - Returns `WeeklyPlan`
- `useUpdateWeeklyPlan()` mutation:
  - PUT `/api/weekly-plan`
  - Update task grid
  - Invalidate week queries

**Invariants:**
- Closed days are non-editable
- Only affect future days

**Deliverables:**
- Working data layer for Week

---

### Agent 4.2 – Week Page & Grid

**Scope:**
- `src/app/week/page.tsx`
- `src/components/week/WeeklyGrid.tsx`
- `src/components/week/DayColumn.tsx`

**Responsibilities:**
- Week page layout:
  - 7-day grid (Mon-Sun)
  - Week navigation (prev/next)
  - Current week indicator
- WeeklyGrid:
  - Render 7 DayColumns
  - Handle drag-and-drop between days
- DayColumn:
  - Date header
  - Task list
  - Add task action
  - Closed indicator (frozen visual)

**Visual Rules:**
- Past/closed days = frozen (grayed, no drag)
- Future days = editable (full color, draggable)
- Today = highlighted

**Deliverables:**
- Functional Week grid

---

### Agent 4.3 – Week Task Management

**Scope:**
- `src/components/week/WeekTaskItem.tsx`
- `src/components/week/AddTaskDialog.tsx`
- `src/hooks/useCreateTask.ts`

**Responsibilities:**
- WeekTaskItem:
  - Draggable task card
  - Remove action
  - Visual feedback
- AddTaskDialog:
  - Task description input
  - Optional goal/key result link
  - Create and add to day
- `useCreateTask()` mutation:
  - POST `/api/tasks`
  - Create new task

**Deliverables:**
- Task CRUD for Week view

---

### Phase 4 Gate: Testing

**Tests Required:**

1. **Week Hooks Tests** (`src/hooks/__tests__/useWeeklyPlan.test.ts`)
```typescript
describe('useWeekDashboard', () => {
  it('fetches from /api/dashboard/week with weekStart param')
  it('returns DayProgress[] shape')
})

describe('useWeeklyPlan', () => {
  it('fetches from /api/weekly-plan/{date}')
  it('returns WeeklyPlan shape')
})

describe('useUpdateWeeklyPlan', () => {
  it('calls PUT /api/weekly-plan')
  it('invalidates week queries on success')
})

describe('useCreateTask', () => {
  it('calls POST /api/tasks')
  it('returns created task')
})
```

2. **Week Component Tests** (`src/components/week/__tests__/`)
```typescript
describe('DayColumn', () => {
  it('renders date header')
  it('renders task list')
  it('shows add task button for future days')
  it('disables interactions for closed days')
  it('applies frozen styling to past days')
})

describe('WeeklyGrid', () => {
  it('renders 7 DayColumns')
  it('handles drag and drop between days')
  it('prevents drop on closed days')
})

describe('WeekTaskItem', () => {
  it('is draggable for future days')
  it('is not draggable for closed days')
  it('shows remove button')
})

describe('AddTaskDialog', () => {
  it('opens on add button click')
  it('creates task on submit')
  it('validates description is required')
})
```

3. **Week E2E Test** (`e2e/week.spec.ts`)
```typescript
describe('Week Screen', () => {
  it('displays 7-day grid')
  it('shows tasks in correct days')
  it('can add new task to a future day')
  it('can drag task between future days')
  it('cannot modify closed/past days')
  it('navigates to previous/next week')
})
```

**Run:**
```bash
npm run test
npm run test:e2e
```

**Exit Criteria:**
- [ ] All Phase 4 tests pass
- [ ] Week grid displays correctly
- [ ] Can add task to future day
- [ ] Can drag task between days
- [ ] Past/closed days are frozen
- [ ] Week navigation works

---

## Phase 5: Goals Screen (Direction Mode)

**Parallel Agents: 3**

### Agent 5.1 – Goals Data Hooks

**Scope:**
- `src/hooks/useGoalsDashboard.ts`
- `src/hooks/useGoals.ts`
- `src/hooks/useCreateGoal.ts`
- `src/hooks/useUpdateGoal.ts`

**Responsibilities:**
- `useGoalsDashboard()` query:
  - GET `/api/dashboard/goals`
  - Returns `GoalDetail[]` with snapshots and trends
- `useGoals()` query:
  - GET `/api/goals`
  - Returns `Goal[]`
- `useCreateGoal()` mutation:
  - POST `/api/goals`
- `useUpdateGoal()` mutation:
  - PUT `/api/goals/{id}`
- `useDeleteGoal()` mutation:
  - DELETE `/api/goals/{id}`

**Invariants:**
- Never recompute progress client-side
- Render snapshot-derived meaning

**Deliverables:**
- Working data layer for Goals

---

### Agent 5.2 – Goals Page & Cards

**Scope:**
- `src/app/goals/page.tsx`
- `src/components/goals/GoalCard.tsx`
- `src/components/goals/GoalProgress.tsx`

**Responsibilities:**
- Goals page layout:
  - List of goal cards
  - Add goal action
- GoalCard:
  - Title
  - Actual % vs Expected %
  - Status: Ahead / On-track / Behind
  - Trend indicator (↑ / → / ↓)
  - Expand to show key results
- GoalProgress:
  - Visual progress bar
  - Percentage labels

**Status Logic (from backend):**
- `actual > expected` → Ahead (green)
- `actual ≈ expected` → On-track (blue)
- `actual < expected` → Behind (orange)

**Deliverables:**
- Functional Goals page

---

### Agent 5.3 – Key Results Management

**Scope:**
- `src/components/goals/KeyResultList.tsx`
- `src/components/goals/KeyResultItem.tsx`
- `src/components/goals/AddKeyResultDialog.tsx`
- `src/hooks/useKeyResults.ts`

**Responsibilities:**
- KeyResultList:
  - List key results for a goal
  - Add key result action
- KeyResultItem:
  - Title
  - Progress (currentValue / targetValue)
  - Type indicator (Accumulative, Habit, Milestone)
  - Edit/delete actions
  - Complete milestone action
- AddKeyResultDialog:
  - Title, type, target value inputs
- Hooks:
  - `useCreateKeyResult()`
  - `useUpdateKeyResult()`
  - `useDeleteKeyResult()`
  - `useCompleteMilestone()`

**Deliverables:**
- Full key result management

---

### Phase 5 Gate: Testing

**Tests Required:**

1. **Goals Hooks Tests** (`src/hooks/__tests__/useGoals.test.ts`)
```typescript
describe('useGoalsDashboard', () => {
  it('fetches from /api/dashboard/goals')
  it('returns GoalDetail[] with snapshots and trends')
})

describe('useGoals', () => {
  it('fetches from /api/goals')
  it('returns Goal[] shape')
})

describe('useCreateGoal', () => {
  it('calls POST /api/goals')
  it('invalidates goals queries on success')
})

describe('useDeleteGoal', () => {
  it('calls DELETE /api/goals/{id}')
  it('invalidates goals queries on success')
})
```

2. **Key Result Hooks Tests** (`src/hooks/__tests__/useKeyResults.test.ts`)
```typescript
describe('useCreateKeyResult', () => {
  it('calls POST /api/goals/{goalId}/key-results')
})

describe('useCompleteMilestone', () => {
  it('calls POST /api/goals/key-results/{id}/complete')
})
```

3. **Goals Component Tests** (`src/components/goals/__tests__/`)
```typescript
describe('GoalCard', () => {
  it('renders goal title')
  it('displays actual vs expected percentage')
  it('shows Ahead/On-track/Behind status')
  it('displays trend indicator')
  it('expands to show key results')
  it('never computes progress client-side (invariant)')
})

describe('GoalProgress', () => {
  it('renders progress bar with correct width')
  it('displays percentage labels')
})

describe('KeyResultItem', () => {
  it('renders title and progress')
  it('shows type indicator')
  it('shows complete button for milestones')
})

describe('AddKeyResultDialog', () => {
  it('validates required fields')
  it('creates key result on submit')
})
```

4. **Goals E2E Test** (`e2e/goals.spec.ts`)
```typescript
describe('Goals Screen', () => {
  it('displays list of goals')
  it('shows goal progress from API (not computed)')
  it('can create new goal')
  it('can expand goal to see key results')
  it('can create key result')
  it('can complete milestone')
  it('displays trend indicator correctly')
})
```

**Run:**
```bash
npm run test
npm run test:e2e
```

**Exit Criteria:**
- [ ] All Phase 5 tests pass
- [ ] Goals page displays goals from API
- [ ] Progress is rendered, not computed
- [ ] Can create goal and key results
- [ ] Trend indicators display correctly
- [ ] Can complete milestone

---

## Phase 6: History Screen (Truth Mode)

**Parallel Agents: 2**

### Agent 6.1 – History Data Hooks

**Scope:**
- `src/hooks/useHistoryDay.ts`
- `src/hooks/useRecentHistory.ts`

**Responsibilities:**
- `useHistoryDay(date)` query:
  - GET `/api/daily/{date}`
  - Returns `DailyPlan`
- `useRecentHistory(limit)` query:
  - GET `/api/history/recent?limit={n}`
  - Returns recent audit events

**Invariants:**
- Read-only
- No mutations
- Immutable truth

**Deliverables:**
- Working data layer for History

---

### Agent 6.2 – History Page & Components

**Scope:**
- `src/app/history/page.tsx`
- `src/app/history/[date]/page.tsx`
- `src/components/history/HistoryDayView.tsx`
- `src/components/history/DatePicker.tsx`

**Responsibilities:**
- History index page:
  - Calendar or date picker
  - Recent days list
  - Navigate to specific date
- History day page:
  - Same layout as Today
  - Completely read-only
  - Shows entries, completion, ratio
- Visual distinction:
  - Frozen/archived appearance
  - "What actually happened" framing

**Visual Rules:**
- All interactions disabled
- Muted color palette
- Clear "Past" indicator

**Deliverables:**
- Functional History screen

---

### Phase 6 Gate: Testing

**Tests Required:**

1. **History Hooks Tests** (`src/hooks/__tests__/useHistory.test.ts`)
```typescript
describe('useHistoryDay', () => {
  it('fetches from /api/daily/{date}')
  it('returns DailyPlan shape')
  it('does NOT expose any mutation functions')
})

describe('useRecentHistory', () => {
  it('fetches from /api/history/recent with limit')
  it('returns audit events')
})
```

2. **History Component Tests** (`src/components/history/__tests__/`)
```typescript
describe('HistoryDayView', () => {
  it('renders same layout as Today')
  it('does NOT render any action buttons')
  it('applies frozen/archived styling')
  it('displays entries and completion ratio')
})

describe('DatePicker', () => {
  it('renders calendar or date input')
  it('navigates to selected date')
  it('prevents future date selection')
})
```

3. **History E2E Test** (`e2e/history.spec.ts`)
```typescript
describe('History Screen', () => {
  it('displays date picker')
  it('navigates to specific date')
  it('shows past day data read-only')
  it('has NO edit functionality (invariant)')
  it('displays completion stats')
  it('visual distinction from Today')
})
```

4. **Invariant Tests** (`src/__tests__/invariants.test.ts`)
```typescript
describe('History Immutability Invariant', () => {
  it('HistoryDayView has no onClick handlers on tasks')
  it('useHistoryDay returns no mutation functions')
  it('History page has no form elements')
})
```

**Run:**
```bash
npm run test
npm run test:e2e
```

**Exit Criteria:**
- [ ] All Phase 6 tests pass
- [ ] History page displays past days
- [ ] No edit functionality exposed
- [ ] Visual distinction from Today
- [ ] Date navigation works

---

## Phase 7: Polish & Integration

**Parallel Agents: 3**

### Agent 7.1 – Error Handling & Boundaries

**Scope:**
- `src/components/error/ErrorBoundary.tsx`
- `src/components/error/ApiError.tsx`
- `src/lib/errors.ts`

**Responsibilities:**
- Global error boundary
- API error component:
  - Map error codes to messages
  - 401 → Redirect to login
  - 403 → "Access denied"
  - 404 → "Not found"
  - 409 → Domain-specific message
  - 500 → "Something went wrong"
- Toast notifications for mutations
- No silent failures

**Error Messages (Domain-Aware):**
- `CONFLICT` with "closed" → "This day is closed"
- `FORBIDDEN` → "You don't have access to this"
- `NOT_FOUND` → "Could not find {entity}"

**Deliverables:**
- Consistent error handling

---

### Agent 7.2 – Loading States & Skeletons

**Scope:**
- `src/components/ui/Skeleton.tsx`
- `src/components/ui/LoadingSpinner.tsx`
- Update all pages with loading states

**Responsibilities:**
- Skeleton components matching content layout
- Loading spinners for mutations
- Page-level loading states
- Optimistic updates for mutations
- No blank screens during load

**Deliverables:**
- Polished loading experience

---

### Agent 7.3 – Accessibility & Responsiveness

**Scope:**
- All components

**Responsibilities:**
- Keyboard navigation:
  - Tab order
  - Enter/Space activation
  - Escape to close dialogs
- Screen reader support:
  - Proper labels
  - ARIA attributes
  - Live regions for updates
- Color contrast (WCAG AA)
- Mobile responsiveness:
  - Touch targets
  - Responsive layouts
  - Mobile navigation

**Deliverables:**
- Accessible, responsive app

---

### Phase 7 Gate: Testing

**Tests Required:**

1. **Error Handling Tests** (`src/lib/__tests__/errors.test.ts`)
```typescript
describe('Error Mapping', () => {
  it('maps 401 to redirect action')
  it('maps 403 to "Access denied" message')
  it('maps 404 to "Not found" message')
  it('maps 409 to domain-specific message')
  it('maps 500 to generic error message')
  it('extracts message from ApiResponse')
})
```

2. **Error Boundary Tests** (`src/components/error/__tests__/`)
```typescript
describe('ErrorBoundary', () => {
  it('catches render errors')
  it('displays fallback UI')
  it('provides retry mechanism')
})

describe('ApiError', () => {
  it('displays error code')
  it('displays error message')
  it('shows domain-aware messages')
})
```

3. **Loading State Tests** (`src/components/ui/__tests__/`)
```typescript
describe('Skeleton', () => {
  it('renders with correct dimensions')
  it('animates')
})

describe('LoadingSpinner', () => {
  it('renders spinning indicator')
  it('accepts size prop')
})
```

4. **Accessibility Tests** (`src/__tests__/accessibility.test.ts`)
```typescript
describe('Accessibility', () => {
  it('all pages pass axe accessibility checks')
  it('all interactive elements are keyboard focusable')
  it('dialogs trap focus')
  it('forms have associated labels')
  it('color contrast meets WCAG AA')
})
```

5. **Responsive Tests** (`e2e/responsive.spec.ts`)
```typescript
describe('Responsive Design', () => {
  it('mobile: sidebar collapses to hamburger menu')
  it('mobile: touch targets are 44px minimum')
  it('tablet: layout adapts correctly')
  it('desktop: full sidebar visible')
})
```

**Run:**
```bash
npm run test
npm run test:e2e
npx playwright test --project=mobile
```

**Exit Criteria:**
- [ ] All Phase 7 tests pass
- [ ] All pages pass accessibility audit
- [ ] Error messages are domain-aware
- [ ] Loading states present on all async operations
- [ ] Mobile layout works correctly

---

## Phase 8: Final Verification

**Sequential - Single Agent**

### Agent 8.1 – End-to-End Testing & Verification

**Scope:**
- Complete application

**Responsibilities:**
1. Verify all flows:
   - Register → Login → Today → Complete task
   - Week → Add task → View in Today
   - Goals → Create goal → Create key result
   - History → View past day
2. Verify invariants:
   - Closed days are read-only
   - No client-side progress computation
   - Errors display meaningful messages
   - All screens use single authoritative query
3. Verify code quality:
   - No `any` types
   - All components < 150 lines
   - Hooks for all data access
   - No business logic in components
   - Proper naming conventions
4. Performance check:
   - No unnecessary re-renders
   - Proper query caching
   - Optimistic updates working

**Deliverables:**
- Working, verified application

---

### Phase 8 Gate: Final Verification Suite

**All Tests Must Pass:**
```bash
# Run complete test suite
npm run test:all

# Run E2E suite
npm run test:e2e

# Run accessibility audit
npm run test:a11y

# Type check
npm run typecheck

# Lint
npm run lint
```

**Manual Verification Checklist:**

1. **Critical User Flows:**
   - [ ] Register → Login → See Today → Complete task → Verify in History
   - [ ] Create Goal → Add Key Result → Link task → Complete task → See progress update
   - [ ] Week planning → Add tasks to days → See them in Today
   - [ ] View closed day in History (immutable)

2. **Invariant Verification:**
   - [ ] Today screen uses ONLY `/api/dashboard/today`
   - [ ] Week screen uses ONLY `/api/dashboard/week` and `/api/weekly-plan`
   - [ ] Goals screen uses ONLY `/api/dashboard/goals` and `/api/goals`
   - [ ] History screen uses ONLY `/api/daily/{date}` and `/api/history`
   - [ ] NO client-side progress computation anywhere
   - [ ] Closed days are 100% read-only

3. **Error Handling Verification:**
   - [ ] Invalid login shows meaningful error
   - [ ] 403 shows "Access denied"
   - [ ] 409 on closed day shows "This day is closed"
   - [ ] Network error shows retry option
   - [ ] 500 shows generic error (no internal details)

4. **Code Quality Verification:**
   - [ ] Zero TypeScript errors
   - [ ] Zero ESLint errors
   - [ ] All components < 150 lines
   - [ ] All data access through hooks
   - [ ] No `any` types
   - [ ] No business logic in components

5. **Performance Verification:**
   - [ ] Lighthouse score > 90
   - [ ] No unnecessary re-renders (React DevTools)
   - [ ] Query caching working (no duplicate requests)
   - [ ] Optimistic updates feel instant

6. **PWA Verification:**
   - [ ] Lighthouse PWA audit passes (score 100)
   - [ ] App installable on Chrome desktop
   - [ ] App installable on Android Chrome
   - [ ] App installable on iOS Safari (Add to Home Screen)
   - [ ] Install prompt appears for new users
   - [ ] Offline indicator shows when network disconnected
   - [ ] Cached data displays when offline
   - [ ] Service worker registered and active
   - [ ] Manifest accessible at /manifest.json
   - [ ] App launches in standalone mode (no browser chrome)
   - [ ] Splash screen displays on launch
   - [ ] Theme color matches app design

**PWA Testing Commands:**
```bash
# Lighthouse PWA audit
npx lighthouse http://localhost:3000 --only-categories=pwa --view

# Check manifest
curl http://localhost:3000/manifest.json | jq

# Verify service worker (in browser console)
navigator.serviceWorker.getRegistrations().then(console.log)
```

**Exit Criteria:**
- [ ] All automated tests pass
- [ ] All manual checks pass
- [ ] PWA audit passes with 100 score
- [ ] Ready for production deployment

---

## API Endpoint Reference

| Screen | Endpoint | Method | Purpose |
|--------|----------|--------|---------|
| Auth | `/api/auth/login` | POST | Login |
| Auth | `/api/auth/register` | POST | Register |
| Today | `/api/dashboard/today` | GET | Today's dashboard |
| Today | `/api/daily/{date}/tasks/{taskId}/complete` | POST | Complete task |
| Today | `/api/daily/{date}/tasks/{taskId}/miss` | POST | Miss task |
| Week | `/api/dashboard/week` | GET | Week progress |
| Week | `/api/weekly-plan/{date}` | GET | Weekly plan |
| Week | `/api/weekly-plan` | POST | Create weekly plan |
| Goals | `/api/dashboard/goals` | GET | Goals dashboard |
| Goals | `/api/goals` | GET/POST | List/Create goals |
| Goals | `/api/goals/{id}` | PUT/DELETE | Update/Delete goal |
| Goals | `/api/goals/{goalId}/key-results` | POST | Create key result |
| Goals | `/api/goals/key-results/{id}` | PUT/DELETE | Update/Delete KR |
| Goals | `/api/goals/key-results/{id}/complete` | POST | Complete milestone |
| History | `/api/daily/{date}` | GET | Day's plan |
| History | `/api/history/recent` | GET | Recent events |
| Tasks | `/api/tasks` | GET/POST | List/Create tasks |
| Tasks | `/api/tasks/{id}` | PUT/DELETE | Update/Delete task |
| Streak | `/api/streak` | GET | Current streak |
| Preferences | `/api/preferences` | GET/POST | User preferences |

---

## Summary

| Phase | Agents | Focus | Depends On | Gate |
|-------|--------|-------|------------|------|
| 0 | 1 | CORS (if needed) | None | CORS test |
| 1 | 5 | Foundation + PWA | Phase 0 | Unit tests + build + PWA audit |
| 2 | 2 | Authentication | Phase 1 | Auth E2E tests |
| 3 | 3 | Today Screen | Phase 2 | Today E2E tests |
| 4 | 3 | Week Screen | Phase 3 | Week E2E tests |
| 5 | 3 | Goals Screen | Phase 4 | Goals E2E tests |
| 6 | 2 | History Screen | Phase 3 | History E2E + invariants |
| 7 | 3 | Polish | Phases 3-6 | A11y + responsive tests |
| 8 | 1 | Verification | ALL | Full test suite + PWA verification |

**Total Agents:** 23 (parallelizable within phases)

**Dependency Rules:**
- Each phase MUST complete all tests before next phase starts
- Agents within a phase CAN run in parallel if internal dependencies are met
- Phase 6 (History) can run in parallel with Phase 4/5 since it only depends on Phase 3
- Phase 7 requires all feature phases (3-6) complete

**Parallel Execution Timeline:**
```
Time →
────────────────────────────────────────────────────────────────
Phase 0: [████]
Phase 1:       [████████████████]
Phase 2:                         [████████]
Phase 3:                                   [████████████]
Phase 4:                                                 [████████████]
Phase 5:                                                               [████████████]
Phase 6:                                   [████████] (parallel with 4-5)
Phase 7:                                                                             [████████]
Phase 8:                                                                                       [████]
```

---

## Code Principles Enforcement Checklist

Every agent must verify:

- [ ] Screen has exactly ONE authoritative query
- [ ] No screen mixes domains
- [ ] No client-side derivation of meaning
- [ ] Closed days are visually and functionally frozen
- [ ] All data access through hooks
- [ ] Components < 150 lines
- [ ] No business logic in components
- [ ] Proper error messages (not generic)
- [ ] Loading states for all async operations
- [ ] Keyboard accessible
- [ ] Mobile responsive
- [ ] PWA: Works offline with cached data
- [ ] PWA: Install prompt functional
- [ ] PWA: Offline indicator visible when disconnected

---

## PWA Checklist (All Phases)

Every agent that touches pages must verify:

| Requirement | Description | Verified |
|-------------|-------------|----------|
| Manifest | `/manifest.json` exists and valid | [x] |
| Icons | All required icon sizes present | [x] |
| Service Worker | Registers in production build | [x] |
| Installable | Lighthouse installable check passes | [x] |
| Offline-capable | Pages work with cached data | [x] |
| Standalone | App launches without browser chrome | [x] |
| Theme color | Matches app design | [x] |
| Start URL | Points to `/today` | [x] |
| Caching | API responses cached appropriately | [x] |
| Install prompt | Shows for new users | [x] |
| Offline indicator | Shows when network unavailable | [x] |
| Background sync | (Future) Queued mutations sync | [ ] |

---

*This plan preserves the ontology: Intent ≠ Execution ≠ Meaning*
*FocusFlow is a Progressive Web App - installable, offline-capable, app-like.*
