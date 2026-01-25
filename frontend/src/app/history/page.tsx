import { AppShell } from '@/components/layout';

/**
 * History page - Truth Mode.
 * "What actually happened."
 *
 * Source of truth: DailyPlan(date) - Read only
 */
export default function HistoryPage() {
  return (
    <AppShell>
      <div className="max-w-4xl mx-auto">
        <h2 className="text-2xl font-bold mb-4">History</h2>
        <p className="text-muted-foreground">
          Truth mode - Your past days will appear here (read-only).
        </p>
        {/* Phase 6 will implement the full History screen */}
      </div>
    </AppShell>
  );
}
