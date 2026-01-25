import { AppShell } from '@/components/layout';

/**
 * Goals page - Direction Mode.
 * "Where you're heading and how you're doing."
 *
 * Source of truth: Goals + Snapshots
 */
export default function GoalsPage() {
  return (
    <AppShell>
      <div className="max-w-4xl mx-auto">
        <h2 className="text-2xl font-bold mb-4">Goals</h2>
        <p className="text-muted-foreground">
          Direction mode - Your goals and progress will appear here.
        </p>
        {/* Phase 5 will implement the full Goals screen */}
      </div>
    </AppShell>
  );
}
