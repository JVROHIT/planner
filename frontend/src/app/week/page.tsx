import { AppShell } from '@/components/layout';

/**
 * Week page - Intent Mode.
 * "What you're committing to this week."
 *
 * Source of truth: WeeklyPlan
 */
export default function WeekPage() {
  return (
    <AppShell>
      <div className="max-w-6xl mx-auto">
        <h2 className="text-2xl font-bold mb-4">Week</h2>
        <p className="text-muted-foreground">
          Intent mode - Your weekly plan will appear here.
        </p>
        {/* Phase 4 will implement the full Week screen */}
      </div>
    </AppShell>
  );
}
