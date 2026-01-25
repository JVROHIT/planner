'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { DayProgress, DayOfWeek } from '@/types/domain';

/**
 * Backend response shape for DayProgress.
 */
interface BackendDayProgress {
  date: string;
  totalTasks: number;
  completedTasks: number;
  closed: boolean;
}

/**
 * Map day index to DayOfWeek enum.
 */
function getDayOfWeek(date: string): DayOfWeek {
  const dayIndex = new Date(date).getDay();
  const dayMap: DayOfWeek[] = [
    'SUNDAY',
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
  ];
  return dayMap[dayIndex];
}

/**
 * Transform backend response to frontend shape.
 */
function transformDayProgress(backend: BackendDayProgress): DayProgress {
  return {
    date: backend.date,
    dayOfWeek: getDayOfWeek(backend.date),
    completed: backend.completedTasks,
    total: backend.totalTasks,
    closed: backend.closed,
  };
}

/**
 * Hook for fetching week dashboard data.
 * 
 * Returns completion statistics for each day in the week.
 * Transforms backend response to match frontend type.
 * 
 * @param weekStart ISO date string (YYYY-MM-DD) - start date of the week
 * @returns DayProgress[] with completion stats for each day
 * 
 * @example
 * const { data, isLoading, error } = useWeekDashboard('2026-01-20');
 */
export function useWeekDashboard(weekStart: string) {
  return useQuery({
    queryKey: ['week-dashboard', weekStart],
    queryFn: async (): Promise<DayProgress[]> => {
      const backendData = await api.get<BackendDayProgress[]>(
        `/api/dashboard/week?weekStart=${weekStart}`
      );
      return backendData.map(transformDayProgress);
    },
    refetchOnWindowFocus: true,
    staleTime: 30000, // 30 seconds
  });
}

export default useWeekDashboard;
