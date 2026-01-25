'use client';

import { usePathname } from 'next/navigation';
import { clearToken } from '@/lib/api';
import { cn } from '@/lib/utils';

interface HeaderProps {
  onMenuClick?: () => void;
}

const modeInfo: Record<string, { title: string; subtitle: string; color: string }> = {
  '/today': {
    title: 'Today',
    subtitle: 'Execution Mode',
    color: 'text-today-active',
  },
  '/week': {
    title: 'Week',
    subtitle: 'Intent Mode',
    color: 'text-week-intent',
  },
  '/goals': {
    title: 'Goals',
    subtitle: 'Direction Mode',
    color: 'text-goal-direction',
  },
  '/history': {
    title: 'History',
    subtitle: 'Truth Mode',
    color: 'text-history-truth',
  },
};

/**
 * Application header with mode indicator and user actions.
 */
export function Header({ onMenuClick }: HeaderProps) {
  const pathname = usePathname();

  const getModeInfo = () => {
    if (pathname.startsWith('/history')) {
      return modeInfo['/history'];
    }
    return modeInfo[pathname] ?? { title: 'FocusFlow', subtitle: '', color: '' };
  };

  const { title, subtitle, color } = getModeInfo();

  const handleLogout = () => {
    clearToken();
    window.location.href = '/login';
  };

  return (
    <header className="h-14 border-b border-border bg-background flex items-center justify-between px-4">
      {/* Mobile menu button */}
      <button
        onClick={onMenuClick}
        className="lg:hidden p-2 -ml-2 rounded-md hover:bg-muted"
        aria-label="Open menu"
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
            d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      {/* Mode indicator */}
      <div className="flex items-center gap-2">
        <h1 className={cn('font-semibold text-lg', color)}>{title}</h1>
        {subtitle && (
          <span className="text-sm text-muted-foreground hidden sm:inline">
            — {subtitle}
          </span>
        )}
      </div>

      {/* User actions */}
      <div className="flex items-center gap-2">
        <button
          onClick={handleLogout}
          className="px-3 py-1.5 text-sm rounded-md hover:bg-muted text-muted-foreground hover:text-foreground transition-colors"
        >
          Logout
        </button>
      </div>
    </header>
  );
}

export default Header;
