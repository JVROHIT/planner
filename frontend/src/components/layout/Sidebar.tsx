'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';

interface NavItem {
  href: string;
  label: string;
  icon: React.ReactNode;
  mode: 'execution' | 'intent' | 'direction' | 'truth';
  description: string;
}

const navItems: NavItem[] = [
  {
    href: '/today',
    label: 'Today',
    mode: 'execution',
    description: 'What you\'re doing',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    href: '/week',
    label: 'Week',
    mode: 'intent',
    description: 'What you plan',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
          d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
  },
  {
    href: '/goals',
    label: 'Goals',
    mode: 'direction',
    description: 'Where you\'re heading',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
          d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
      </svg>
    ),
  },
  {
    href: '/history',
    label: 'History',
    mode: 'truth',
    description: 'What happened',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
          d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
];

const modeColors: Record<string, string> = {
  execution: 'text-today-active',
  intent: 'text-week-intent',
  direction: 'text-goal-direction',
  truth: 'text-history-truth',
};

interface SidebarProps {
  onNavigate?: () => void;
}

/**
 * Navigation sidebar for FocusFlow.
 * Displays mode-aware navigation with visual indication of current mode.
 */
export function Sidebar({ onNavigate }: SidebarProps) {
  const pathname = usePathname();

  const isActive = (href: string) => {
    if (href === '/history') {
      return pathname.startsWith('/history');
    }
    return pathname === href;
  };

  return (
    <nav className="flex flex-col h-full bg-muted/30">
      {/* Logo */}
      <div className="p-4 border-b border-border">
        <Link href="/today" className="flex items-center gap-2" onClick={onNavigate}>
          <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
            <span className="text-primary-foreground font-bold text-sm">FF</span>
          </div>
          <span className="font-semibold text-lg">FocusFlow</span>
        </Link>
      </div>

      {/* Navigation items */}
      <div className="flex-1 p-2 space-y-1">
        {navItems.map((item) => {
          const active = isActive(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              onClick={onNavigate}
              aria-current={active ? 'page' : undefined}
              aria-label={`${item.label}: ${item.description}`}
              className={cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary',
                'hover:bg-muted',
                active && 'bg-muted',
                active && modeColors[item.mode]
              )}
            >
              <span className={cn(!active && 'text-muted-foreground')}>
                {item.icon}
              </span>
              <div className="flex flex-col">
                <span className={cn('font-medium', !active && 'text-foreground')}>
                  {item.label}
                </span>
                <span className="text-xs text-muted-foreground">
                  {item.description}
                </span>
              </div>
            </Link>
          );
        })}
      </div>

      {/* Footer */}
      <div className="p-4 border-t border-border">
        <p className="text-xs text-muted-foreground text-center">
          Intent ≠ Execution ≠ Meaning
        </p>
      </div>
    </nav>
  );
}

export default Sidebar;
