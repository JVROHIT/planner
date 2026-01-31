'use client';

import { useState } from 'react';
import { AppShell } from '@/components/layout/AppShell';
import { useGoalsDashboard } from '@/hooks';
import { GoalCard } from '@/components/goals/GoalCard';
import { AddGoalDialog } from '@/components/goals/AddGoalDialog';
import { ApiError } from '@/components/error/ApiError';
import { Skeleton } from '@/components/ui/Skeleton';

/**
 * Goals page - Direction Mode.
 * "Where you're heading and how you're doing."
 *
 * Source of truth: Goals + Snapshots
 */
export default function GoalsPage() {
  const { data: dashboard, isLoading, error, refetch } = useGoalsDashboard();
  const [showAddDialog, setShowAddDialog] = useState(false);
  const goals = Array.isArray(dashboard) ? dashboard : dashboard?.goals ?? [];

  return (
    <AppShell>
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-3xl font-bold mb-2">Goals</h1>
            <p className="text-muted-foreground">
              Direction mode - Where you&apos;re heading and how you&apos;re doing
            </p>
          </div>

          <button
            onClick={() => setShowAddDialog(true)}
            className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
            aria-haspopup="dialog"
          >
            + Add Goal
          </button>
        </div>

        {/* Loading state */}
        {isLoading && (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="border rounded-lg p-6 bg-card">
                <Skeleton className="h-6 w-1/3 mb-4" />
                <Skeleton className="h-3 w-full mb-2" />
                <Skeleton className="h-3 w-2/3" />
              </div>
            ))}
          </div>
        )}

        {/* Error state */}
        {error && (
          <ApiError error={error} reset={refetch} />
        )}

        {/* Goals list */}
        {dashboard && (
          <>
            {goals.length === 0 ? (
              <div className="border rounded-lg p-12 text-center bg-card">
                <p className="text-muted-foreground mb-4">No goals yet</p>
                <p className="text-sm text-muted-foreground">
                  Create your first goal to start tracking your progress
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {goals.map((goalDetail) => (
                  <GoalCard key={goalDetail.goal.id} goalDetail={goalDetail} />
                ))}
              </div>
            )}
          </>
        )}

        {/* Add Goal Dialog */}
        {showAddDialog && (
          <AddGoalDialog onClose={() => setShowAddDialog(false)} />
        )}
      </div>
    </AppShell>
  );
}
