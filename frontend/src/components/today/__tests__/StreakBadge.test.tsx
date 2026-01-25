import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StreakBadge } from '../StreakBadge';

describe('StreakBadge', () => {
  it('displays streak count', () => {
    render(<StreakBadge streak={5} />);

    expect(screen.getByText('5 days')).toBeInTheDocument();
  });

  it('displays singular form for 1 day', () => {
    render(<StreakBadge streak={1} />);

    expect(screen.getByText('1 day')).toBeInTheDocument();
  });

  it('shows fire icon for active streak', () => {
    const { container } = render(<StreakBadge streak={5} />);

    // Check for SVG icon (fire icon has fill="currentColor" on the SVG element)
    const icon = container.querySelector('svg[aria-hidden="true"]');
    expect(icon).toBeTruthy();
    // Verify it's the fire icon (SVG has fill="currentColor")
    expect(icon?.getAttribute('fill')).toBe('currentColor');
    // Verify it has a path element
    const path = icon?.querySelector('path');
    expect(path).toBeTruthy();
  });

  it('shows clock icon for zero streak', () => {
    render(<StreakBadge streak={0} />);

    const icon = document.querySelector('svg[aria-hidden="true"]');
    expect(icon).toBeInTheDocument();
  });
});
