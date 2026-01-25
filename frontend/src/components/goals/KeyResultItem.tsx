'use client';

import type { KeyResult } from '@/types/domain';
import { useDeleteKeyResult, useCompleteMilestone } from '@/hooks';

/**
 * KeyResultItem Component
 * 
 * Displays a single key result with:
 * - Title
 * - Progress (currentValue / targetValue)
 * - Type indicator (Accumulative, Habit, Milestone)
 * - Complete button for milestones
 * - Delete action
 * 
 * Philosophy: Render progress from API only, never compute.
 */

interface KeyResultItemProps {
    keyResult: KeyResult;
    goalId: string;
}

export function KeyResultItem({ keyResult, goalId }: KeyResultItemProps) {
    const { mutate: deleteKeyResult, isPending: isDeleting } = useDeleteKeyResult();
    const { mutate: completeMilestone, isPending: isCompleting } = useCompleteMilestone();

    const handleDelete = () => {
        if (confirm(`Delete key result "${keyResult.title}"?`)) {
            deleteKeyResult({ goalId, keyResultId: keyResult.id });
        }
    };

    const handleComplete = () => {
        completeMilestone({ goalId, keyResultId: keyResult.id });
    };

    // Type badge styling
    const typeBadges = {
        ACCUMULATIVE: { label: 'Accumulative', color: 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300' },
        HABIT: { label: 'Habit', color: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300' },
        MILESTONE: { label: 'Milestone', color: 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300' },
    };

    const typeBadge = typeBadges[keyResult.type];
    const progressPercent = keyResult.targetValue > 0
        ? (keyResult.currentValue / keyResult.targetValue) * 100
        : 0;

    return (
        <div className="flex items-center justify-between p-4 border rounded-lg bg-card/50 hover:bg-card transition-colors">
            <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                    <h4 className="font-medium">{keyResult.title}</h4>
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${typeBadge.color}`}>
                        {typeBadge.label}
                    </span>
                    {keyResult.completed && (
                        <span className="px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300">
                            ✓ Completed
                        </span>
                    )}
                </div>

                {/* Progress bar */}
                <div className="flex items-center gap-3">
                    <div className="flex-1 h-2 bg-muted rounded-full overflow-hidden">
                        <div
                            className={`h-full transition-all duration-300 ${keyResult.completed ? 'bg-green-500' : 'bg-primary'
                                }`}
                            style={{ width: `${Math.min(progressPercent, 100)}%` }}
                        />
                    </div>
                    <span className="text-sm text-muted-foreground min-w-[80px] text-right">
                        {keyResult.currentValue} / {keyResult.targetValue}
                    </span>
                </div>
            </div>

            {/* Actions */}
            <div className="flex items-center gap-2 ml-4">
                {keyResult.type === 'MILESTONE' && !keyResult.completed && (
                    <button
                        onClick={handleComplete}
                        disabled={isCompleting}
                        className="px-3 py-1 text-sm bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300 rounded hover:bg-green-200 dark:hover:bg-green-900/50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isCompleting ? 'Completing...' : 'Complete'}
                    </button>
                )}

                <button
                    onClick={handleDelete}
                    disabled={isDeleting}
                    className="px-3 py-1 text-sm text-destructive hover:bg-destructive/10 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    aria-label="Delete key result"
                >
                    {isDeleting ? '...' : 'Delete'}
                </button>
            </div>
        </div>
    );
}

export default KeyResultItem;
