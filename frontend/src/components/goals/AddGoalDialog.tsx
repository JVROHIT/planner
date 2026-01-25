'use client';

import { useState } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { useCreateGoal } from '@/hooks';

/**
 * AddGoalDialog Component
 * 
 * Dialog for creating a new goal.
 * Uses Radix UI for accessibility.
 */

interface AddGoalDialogProps {
    onClose: () => void;
}

export function AddGoalDialog({ onClose }: AddGoalDialogProps) {
    const { mutate: createGoal, isPending } = useCreateGoal();
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!title.trim()) return;

        createGoal(
            {
                title: title.trim(),
                description: description.trim() || undefined,
            },
            {
                onSuccess: () => {
                    setTitle('');
                    setDescription('');
                    onClose();
                },
            }
        );
    };

    return (
        <Dialog.Root open onOpenChange={(open) => !open && onClose()}>
            <Dialog.Portal>
                <Dialog.Overlay className="fixed inset-0 bg-black/50 z-50 animate-in fade-in" />
                <Dialog.Content
                    className="fixed left-[50%] top-[50%] translate-x-[-50%] translate-y-[-50%] bg-card border rounded-lg p-6 max-w-md w-full mx-4 shadow-xl z-50 animate-in zoom-in-95"
                    aria-describedby="add-goal-description"
                >
                    <Dialog.Title className="text-xl font-semibold mb-4">Create New Goal</Dialog.Title>
                    <div id="add-goal-description" className="sr-only">Form to create a new high-level goal</div>

                    <form onSubmit={handleSubmit}>
                        <div className="space-y-4">
                            {/* Title */}
                            <div>
                                <label htmlFor="goal-title" className="block text-sm font-medium mb-2">
                                    Title <span className="text-destructive">*</span>
                                </label>
                                <input
                                    id="goal-title"
                                    type="text"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
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
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                    placeholder="Add more context about this goal..."
                                    rows={3}
                                    className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-background resize-none"
                                />
                            </div>

                            {/* Actions */}
                            <div className="flex gap-3 justify-end pt-2">
                                <Dialog.Close asChild>
                                    <button
                                        type="button"
                                        className="px-4 py-2 border rounded-lg hover:bg-muted transition-colors"
                                        disabled={isPending}
                                    >
                                        Cancel
                                    </button>
                                </Dialog.Close>
                                <button
                                    type="submit"
                                    className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                    disabled={isPending || !title.trim()}
                                >
                                    {isPending ? 'Creating...' : 'Create Goal'}
                                </button>
                            </div>
                        </div>
                    </form>
                </Dialog.Content>
            </Dialog.Portal>
        </Dialog.Root>
    );
}

export default AddGoalDialog;
