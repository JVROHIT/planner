'use client';

import { useState, useEffect } from 'react';
import { cn } from '@/lib/utils';

/**
 * Indicator that shows when the app is offline.
 * Displays a banner at the top of the screen.
 */
export function OfflineIndicator() {
  const [isOnline, setIsOnline] = useState(true);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    // Set initial state
    setIsOnline(navigator.onLine);
    setIsVisible(!navigator.onLine);

    const handleOnline = () => {
      setIsOnline(true);
      // Show "back online" briefly, then hide
      setTimeout(() => setIsVisible(false), 2000);
    };

    const handleOffline = () => {
      setIsOnline(false);
      setIsVisible(true);
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  if (!isVisible) return null;

  return (
    <div
      className={cn(
        'fixed top-0 left-0 right-0 z-50 py-2 px-4 text-center text-sm font-medium transition-colors',
        isOnline
          ? 'bg-today-active text-white'
          : 'bg-destructive text-destructive-foreground'
      )}
      role="status"
      aria-live="polite"
    >
      {isOnline ? (
        'Back online'
      ) : (
        <>
          <span className="inline-block mr-2">●</span>
          You&apos;re offline — showing cached data
        </>
      )}
    </div>
  );
}

export default OfflineIndicator;
