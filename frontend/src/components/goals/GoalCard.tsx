'use client';

import { useState } from 'react';
import type { GoalDetail } from '@/types/domain';
import { GoalProgress } from './GoalProgress';
import { KeyResultList } from './KeyResultList';

/**
 * GoalCard Component
 * 
 * Displays a single goal with:
 * - Title
 * - Actual vs Expected progress
 * - Status (Ahead/On-track/Behind)
 * - Trend indicator (↑/→/↓)
 * - Expandable to show key results
 * 
 * Philosophy: Render backend-provided meaning, never compute progress.
 * All status, trend, and percentages come from API.
 */

interface GoalCardProps {
    goalDetail: GoalDetail;
    onExpand?: (goalId: string) => void;
    isExpanded?: boolean;
}

export function GoalCard({ goalDetail, onExpand, isExpanded = false }: GoalCardProps) {
    const [expanded, setExpanded] = useState(isExpanded);
    const { goal, status, trend, actualPercent, expectedPercent, keyResults } = goalDetail;

    const handleToggle = () => {
        const newExpanded = !expanded;
        setExpanded(newExpanded);
        if (onExpand && newExpanded) {
            onExpand(goal.id);
        }
    };

    // Status badge styling
    const statusStyles = {
        AHEAD: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300',
        ON_TRACK: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300',
        BEHIND: 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-300',
    };

    // Trend icon
    const trendIcons = {
        UP: '↑',
        FLAT: '→',
        DOWN: '↓',
    };

    const trendColors = {
        UP: 'text-green-600 dark:text-green-400',
        FLAT: 'text-gray-600 dark:text-gray-400',
        DOWN: 'text-orange-600 dark:text-orange-400',
    };

    return (
        <div className="border rounded-lg p-6 bg-card hover:shadow-md transition-shadow">
            {/* Header */}
            <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                        <h3 className="text-xl font-semibold">{goal.title}</h3>

                        {/* Trend indicator */}
                        <span className={`text-2xl ${trendColors[trend]}`} title={`Trend: ${trend}`}>
                            {trendIcons[trend]}
                        </span>
                    </div>

                </div>

                {/* Status badge */}
                <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusStyles[status]}`}>
                    {status.replace('_', ' ')}
                </span>
            </div>

            {/* Progress */}
            <GoalProgress
                actualPercent={actualPercent}
                expectedPercent={expectedPercent}
                status={status}
            />

            {/* Key Results count and expand button */}
            <div className="mt-4 flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                    {keyResults.length} {keyResults.length === 1 ? 'Key Result' : 'Key Results'}
                </span>

                <button
                    onClick={handleToggle}
                    className="text-sm text-primary hover:underline focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 rounded px-2 py-1"
                    aria-expanded={expanded}
                >
                    {expanded ? 'Hide Key Results' : 'Show Key Results'}
                </button>
            </div>

            {/* Expanded Key Results section */}
            {expanded && (
                <div className="mt-4 pt-4 border-t">
                    <KeyResultList
                        goalId={goal.id}
                        keyResults={keyResults}
                        goalStartDate={goal.startDate}
                        goalEndDate={goal.endDate}
                    />
                </div>
            )}
        </div>
    );
}

export default GoalCard;
