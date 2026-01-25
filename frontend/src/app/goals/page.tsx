'use client';

import { useState } from 'react';
import { AppShell } from '@/components/layout';
import { useGoalsDashboard, useCreateGoal } from '@/hooks';
import { GoalCard } from '@/components/goals/GoalCard';

/**
 * Goals page - Direction Mode.
 * "Where you're heading and how you're doing."
 *
 * Source of truth: Goals + Snapshots
 * 
 * Philosophy:
 * - Single authoritative query: useGoalsDashboard()
 * - Never compute progress, trends, or status client-side
 * - Render only backend-provided meaning
 * - Direction ≠ Intent ≠ Execution
 */
export default function GoalsPage() {
  const { data: dashboard, isLoading, error } = useGoalsDashboard();
  const { mutate: createGoal, isPending: isCreating } = useCreateGoal();
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [newGoalTitle, setNewGoalTitle] = useState('');
  const [newGoalDescription, setNewGoalDescription] = useState('');

  const handleCreateGoal = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newGoalTitle.trim()) return;

    createGoal(
      {
        title: newGoalTitle.trim(),
        description: newGoalDescription.trim() || undefined,
      },
      {
        onSuccess: () => {
          setNewGoalTitle('');
          setNewGoalDescription('');
          setShowAddDialog(false);
        },
      }
    );
  };

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
          >
            + Add Goal
          </button>
        </div>

        {/* Loading state */}
        {isLoading && (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="border rounded-lg p-6 bg-card animate-pulse">
                <div className="h-6 bg-muted rounded w-1/3 mb-4"></div>
                <div className="h-3 bg-muted rounded w-full mb-2"></div>
                <div className="h-3 bg-muted rounded w-2/3"></div>
              </div>
            ))}
          </div>
        )}

        {/* Error state */}
        {error && (
          <div className="border border-destructive/50 rounded-lg p-6 bg-destructive/10">
            <p className="text-destructive font-medium">Failed to load goals</p>
            <p className="text-sm text-muted-foreground mt-1">
              {error instanceof Error ? error.message : 'An error occurred'}
            </p>
          </div>
        )}

        {/* Goals list */}
        {dashboard && (
          <>
            {dashboard.goals.length === 0 ? (
              <div className="border rounded-lg p-12 text-center bg-card">
                <p className="text-muted-foreground mb-4">No goals yet</p>
                <p className="text-sm text-muted-foreground">
                  Create your first goal to start tracking your progress
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {dashboard.goals.map((goalDetail) => (
                  <GoalCard key={goalDetail.goal.id} goalDetail={goalDetail} />
                ))}
              </div>
            )}
          </>
        )}

        {/* Add Goal Dialog */}
        {showAddDialog && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-card border rounded-lg p-6 max-w-md w-full mx-4">
              <h2 className="text-xl font-semibold mb-4">Create New Goal</h2>

              <form onSubmit={handleCreateGoal}>
                <div className="space-y-4">
                  {/* Title */}
                  <div>
                    <label htmlFor="goal-title" className="block text-sm font-medium mb-2">
                      Title <span className="text-destructive">*</span>
                    </label>
                    <input
                      id="goal-title"
                      type="text"
                      value={newGoalTitle}
                      onChange={(e) => setNewGoalTitle(e.target.value)}
                      placeholder="e.g., Read 12 books this year"
                      className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-background"
                      autoFocus
                      required
                    />
                  </div>

                  {/* Description */}
                  <div>
                    <label htmlFor="goal-description" className="block text-sm font-medium mb-2">
                      Description (optional)
                    </label>
                    <textarea
                      id="goal-description"
                      value={newGoalDescription}
                      onChange={(e) => setNewGoalDescription(e.target.value)}
                      placeholder="Add more context about this goal..."
                      rows={3}
                      className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-background resize-none"
                    />
                  </div>

                  {/* Actions */}
                  <div className="flex gap-3 justify-end">
                    <button
                      type="button"
                      onClick={() => {
                        setShowAddDialog(false);
                        setNewGoalTitle('');
                        setNewGoalDescription('');
                      }}
                      className="px-4 py-2 border rounded-lg hover:bg-muted transition-colors"
                      disabled={isCreating}
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                      disabled={isCreating || !newGoalTitle.trim()}
                    >
                      {isCreating ? 'Creating...' : 'Create Goal'}
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </AppShell>
  );
}

