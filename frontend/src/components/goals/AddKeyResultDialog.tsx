'use client';

import { useState } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import type { KeyResultType } from '@/types/domain';
import { useCreateKeyResult } from '@/hooks';

/**
 * AddKeyResultDialog Component
 * 
 * Dialog for creating a new key result.
 * Uses Radix UI for accessibility.
 */

interface AddKeyResultDialogProps {
    goalId: string;
    onClose: () => void;
}

export function AddKeyResultDialog({ goalId, onClose }: AddKeyResultDialogProps) {
    const { mutate: createKeyResult, isPending } = useCreateKeyResult();
    const [title, setTitle] = useState('');
    const [type, setType] = useState<KeyResultType>('ACCUMULATIVE');
    const [targetValue, setTargetValue] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        const target = parseInt(targetValue, 10);
        if (!title.trim() || isNaN(target) || target <= 0) return;

        createKeyResult(
            {
                goalId,
                request: {
                    title: title.trim(),
                    type,
                    targetValue: target,
                },
            },
            {
                onSuccess: () => {
                    setTitle('');
                    setType('ACCUMULATIVE');
                    setTargetValue('');
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
                    aria-describedby="add-kr-description"
                >
                    <Dialog.Title className="text-xl font-semibold mb-4">Add Key Result</Dialog.Title>
                    <div id="add-kr-description" className="sr-only">Form to add a new key result to the goal</div>

                    <form onSubmit={handleSubmit}>
                        <div className="space-y-4">
                            {/* Title */}
                            <div>
                                <label htmlFor="kr-title" className="block text-sm font-medium mb-2">
                                    Title <span className="text-destructive">*</span>
                                </label>
                                <input
                                    id="kr-title"
                                    type="text"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                    placeholder="e.g., Read 12 books"
                                    className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-background"
                                    autoFocus
                                    required
                                />
                            </div>

                            {/* Type */}
                            <div>
                                <label htmlFor="kr-type" className="block text-sm font-medium mb-2">
                                    Type <span className="text-destructive">*</span>
                                </label>
                                <select
                                    id="kr-type"
                                    value={type}
                                    onChange={(e) => setType(e.target.value as KeyResultType)}
                                    className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-background"
                                >
                                    <option value="ACCUMULATIVE">Accumulative (e.g., Read 12 books)</option>
                                    <option value="HABIT">Habit (e.g., Exercise 3x per week)</option>
                                    <option value="MILESTONE">Milestone (e.g., Launch MVP)</option>
                                </select>
                                <p className="text-xs text-muted-foreground mt-1">
                                    {type === 'ACCUMULATIVE' && 'Progress increases over time'}
                                    {type === 'HABIT' && 'Regular practice tracking'}
                                    {type === 'MILESTONE' && 'Binary completion'}
                                </p>
                            </div>

                            {/* Target Value */}
                            <div>
                                <label htmlFor="kr-target" className="block text-sm font-medium mb-2">
                                    Target Value <span className="text-destructive">*</span>
                                </label>
                                <input
                                    id="kr-target"
                                    type="number"
                                    min="1"
                                    value={targetValue}
                                    onChange={(e) => setTargetValue(e.target.value)}
                                    placeholder={type === 'MILESTONE' ? '1' : '12'}
                                    className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-background"
                                    required
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
                                    disabled={isPending || !title.trim() || !targetValue}
                                >
                                    {isPending ? 'Adding...' : 'Add Key Result'}
                                </button>
                            </div>
                        </div>
                    </form>
                </Dialog.Content>
            </Dialog.Portal>
        </Dialog.Root>
    );
}

export default AddKeyResultDialog;
