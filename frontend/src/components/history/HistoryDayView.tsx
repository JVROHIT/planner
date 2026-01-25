'use client';

import type { DailyPlan, TaskExecution } from '@/types/domain';
import { cn } from '@/lib/utils';

/**
 * HistoryDayView Component
 * 
 * Renders a frozen, read-only view of a specific day's daily plan.
 * No actions allowed. Visuals reflect "Truth Mode" (archived/past).
 * 
 * @param plan The DailyPlan to display
 */

interface HistoryDayViewProps {
    plan: DailyPlan;
}

export function HistoryDayView({ plan }: HistoryDayViewProps) {
    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h2 className="text-xl font-bold">Task Execution Log</h2>
                <div className={cn(
                    "px-3 py-1 rounded-full text-xs font-medium",
                    plan.closed ? "bg-muted text-muted-foreground" : "bg-primary/20 text-primary"
                )}>
                    {plan.closed ? "FROZEN" : "OPEN"}
                </div>
            </div>

            {plan.tasks.length === 0 ? (
                <div className="text-center py-12 border-2 border-dashed rounded-xl bg-muted/30">
                    <p className="text-muted-foreground font-medium">No tasks recorded for this day.</p>
                </div>
            ) : (
                <div className="space-y-3">
                    {plan.tasks.map((task) => (
                        <HistoryTaskRow key={task.taskId} task={task} />
                    ))}
                </div>
            )}
        </div>
    );
}

function HistoryTaskRow({ task }: { task: TaskExecution }) {
    const isPending = !task.completed && !task.missed;

    return (
        <div
            className={cn(
                'flex items-center gap-3 p-3 rounded-lg border bg-muted/10 opacity-80',
                task.completed && 'border-success/20',
                task.missed && 'border-muted',
                isPending && 'border-border'
            )}
        >
            {/* Status indicator */}
            <div
                className={cn(
                    'w-5 h-5 rounded-full border-2 flex-shrink-0 flex items-center justify-center',
                    task.completed && 'bg-success border-success',
                    task.missed && 'bg-muted border-muted',
                    isPending && 'border-border'
                )}
            >
                {task.completed && (
                    <svg
                        className="w-3 h-3 text-white"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={3}
                            d="M5 13l4 4L19 7"
                        />
                    </svg>
                )}
                {task.missed && (
                    <svg
                        className="w-3 h-3 text-muted-foreground/60"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={3}
                            d="M6 18L18 6M6 6l12 12"
                        />
                    </svg>
                )}
            </div>

            {/* Task title */}
            <div className="flex-1 min-w-0">
                <p
                    className={cn(
                        'text-sm font-medium truncate',
                        task.completed && 'text-success-foreground line-through',
                        task.missed && 'text-muted-foreground/60',
                        isPending && 'text-foreground'
                    )}
                >
                    {task.task.description}
                </p>
            </div>

            {/* Logic labels (optional) */}
            <div className="px-2 py-0.5 rounded text-[10px] font-bold tracking-wider uppercase bg-muted/40 text-muted-foreground/50">
                ARCHIVED
            </div>
        </div>
    );
}

export default HistoryDayView;
