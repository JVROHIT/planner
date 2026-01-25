'use client';

import { useState } from 'react';
import type { KeyResult } from '@/types/domain';
import { KeyResultItem } from './KeyResultItem';
import { AddKeyResultDialog } from './AddKeyResultDialog';

/**
 * KeyResultList Component
 * 
 * Displays a list of key results for a goal.
 * Includes add key result functionality.
 * 
 * Philosophy: Render data from backend, no computation.
 */

interface KeyResultListProps {
    goalId: string;
    keyResults: KeyResult[];
}

export function KeyResultList({ goalId, keyResults }: KeyResultListProps) {
    const [showAddDialog, setShowAddDialog] = useState(false);

    return (
        <div className="space-y-3">
            {/* Header */}
            <div className="flex items-center justify-between">
                <h3 className="text-sm font-medium text-muted-foreground">
                    Key Results ({keyResults.length})
                </h3>
                <button
                    onClick={() => setShowAddDialog(true)}
                    className="text-sm text-primary hover:underline focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 rounded px-2 py-1"
                >
                    + Add Key Result
                </button>
            </div>

            {/* Key Results */}
            {keyResults.length === 0 ? (
                <div className="text-center py-8 text-sm text-muted-foreground border rounded-lg bg-muted/20">
                    No key results yet. Add one to track progress.
                </div>
            ) : (
                <div className="space-y-2">
                    {keyResults.map((kr) => (
                        <KeyResultItem key={kr.id} keyResult={kr} goalId={goalId} />
                    ))}
                </div>
            )}

            {/* Add Dialog */}
            {showAddDialog && (
                <AddKeyResultDialog
                    goalId={goalId}
                    onClose={() => setShowAddDialog(false)}
                />
            )}
        </div>
    );
}

export default KeyResultList;
