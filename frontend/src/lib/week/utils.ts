/**
 * Week utility functions.
 * 
 * Handles week calculations and navigation.
 */

/**
 * Get the start date (Monday) of the week containing the given date.
 * 
 * @param date ISO date string (YYYY-MM-DD) or Date object
 * @returns ISO date string (YYYY-MM-DD) of Monday
 */
export function getWeekStart(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Adjust when day is Sunday
  const monday = new Date(d.setDate(diff));
  return monday.toISOString().split('T')[0];
}

/**
 * Get the start date of the previous week.
 * 
 * @param weekStart ISO date string (YYYY-MM-DD)
 * @returns ISO date string (YYYY-MM-DD) of previous week's Monday
 */
export function getPreviousWeek(weekStart: string): string {
  const date = new Date(weekStart);
  date.setDate(date.getDate() - 7);
  return date.toISOString().split('T')[0];
}

/**
 * Get the start date of the next week.
 * 
 * @param weekStart ISO date string (YYYY-MM-DD)
 * @returns ISO date string (YYYY-MM-DD) of next week's Monday
 */
export function getNextWeek(weekStart: string): string {
  const date = new Date(weekStart);
  date.setDate(date.getDate() + 7);
  return date.toISOString().split('T')[0];
}

/**
 * Get today's date as ISO string.
 * 
 * @returns ISO date string (YYYY-MM-DD)
 */
export function getToday(): string {
  return new Date().toISOString().split('T')[0];
}

/**
 * Check if a date is in the past (before today).
 * 
 * @param date ISO date string (YYYY-MM-DD)
 * @returns true if date is before today
 */
export function isPastDate(date: string): boolean {
  const today = getToday();
  return date < today;
}

/**
 * Check if a date is today.
 * 
 * @param date ISO date string (YYYY-MM-DD)
 * @returns true if date is today
 */
export function isToday(date: string): boolean {
  return date === getToday();
}

/**
 * Check if a date is in the future (after today).
 * 
 * @param date ISO date string (YYYY-MM-DD)
 * @returns true if date is after today
 */
export function isFutureDate(date: string): boolean {
  const today = getToday();
  return date > today;
}

/**
 * Get DayOfWeek from a date string.
 * 
 * @param date ISO date string (YYYY-MM-DD)
 * @returns DayOfWeek enum value
 */
export function getDayOfWeekFromDate(date: string): import('@/types/domain').DayOfWeek {
  const dayIndex = new Date(date).getDay();
  const dayMap: import('@/types/domain').DayOfWeek[] = [
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
