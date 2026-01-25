import { AppShell } from '@/components/layout';

/**
 * Today page - Execution Mode.
 * "What you're actually doing today."
 *
 * Source of truth: DailyPlan(today)
 */
export default function TodayPage() {
  return (
    <AppShell>
      <div className="max-w-4xl mx-auto">
        <h2 className="text-2xl font-bold mb-4">Today</h2>
        <p className="text-muted-foreground">
          Execution mode - Your tasks for today will appear here.
        </p>
        {/* Phase 3 will implement the full Today screen */}
      </div>
    </AppShell>
  );
}
