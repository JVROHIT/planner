'use client';

import { useRouter } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { useRecentHistory } from '@/hooks';
import { format, parseISO } from 'date-fns';
import { cn } from '@/lib/utils';
import type { AuditEvent } from '@/types/domain';

/**
 * History Index Page
 * 
 * Shows a log of recent audit events.
 * Provides a entry point to truth mode.
 */
export default function HistoryPage() {
  const router = useRouter();
  const { data: events, isLoading, error } = useRecentHistory(20);
  const today = format(new Date(), 'yyyy-MM-dd');

  return (
    <AppShell>
      <div className="max-w-4xl mx-auto space-y-8">
        <header className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Activity History</h1>
            <p className="text-muted-foreground">Immutable audit log of all system events.</p>
          </div>
          <button
            onClick={() => router.push(`/history/${today}`)}
            className="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors"
          >
            Go to Daily View
          </button>
        </header>

        <section className="bg-card border rounded-xl shadow-sm overflow-hidden">
          <div className="p-4 border-b bg-muted/30">
            <h2 className="font-semibold">Recent Events</h2>
          </div>

          {isLoading ? (
            <div className="p-8 space-y-4">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="h-10 bg-muted animate-pulse rounded"></div>
              ))}
            </div>
          ) : error ? (
            <div className="p-12 text-center">
              <p className="text-destructive font-medium">Failed to load activity log</p>
            </div>
          ) : events && events.length > 0 ? (
            <div className="divide-y">
              {events.map((event) => (
                <div key={event.id} className="p-4 flex items-start justify-between hover:bg-muted/10 transition-colors">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className={cn(
                        "text-[10px] font-bold px-1.5 py-0.5 rounded",
                        getEventTypeColor(event.type)
                      )}>
                        {event.type.replace('_', ' ')}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {format(parseISO(event.occurredAt), 'MMM d, p')}
                      </span>
                    </div>
                    <p className="text-sm">
                      {getEventDescription(event)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-12 text-center">
              <p className="text-muted-foreground">No recent events found.</p>
            </div>
          )}
        </section>
      </div>
    </AppShell>
  );
}

function getEventTypeColor(type: string) {
  switch (type) {
    case 'TASK_CREATED': return 'bg-blue-100 text-blue-700';
    case 'TASK_COMPLETED': return 'bg-success/20 text-success';
    case 'DAY_CLOSED': return 'bg-amber-100 text-amber-700';
    case 'WEEKLY_PLAN_UPDATED': return 'bg-purple-100 text-purple-700';
    default: return 'bg-muted text-muted-foreground';
  }
}

function getEventDescription(event: AuditEvent) {
  const { type, payload } = event;
  const p = payload as Record<string, string | number | undefined>;

  switch (type) {
    case 'TASK_CREATED':
      return `New task created: "${p.description || '...'}"`;
    case 'TASK_COMPLETED':
      return `Task completed: "${p.description || p.taskId || '...'}"`;
    case 'DAY_CLOSED':
      return `Day closed for ${p.day || '...'}`;
    case 'WEEKLY_PLAN_UPDATED':
      return `Weekly plan updated for week ${p.weekNumber || '...'}`;
    default:
      return `System event: ${type}`;
  }
}
