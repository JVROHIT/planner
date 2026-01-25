import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AppShell } from '../AppShell';

// Mock usePathname
vi.mock('next/navigation', () => ({
  usePathname: () => '/today',
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
}));

describe('AppShell', () => {
  it('renders navigation links', () => {
    render(
      <AppShell>
        <div>Test Content</div>
      </AppShell>
    );

    // Check for navigation items by their link role
    const links = screen.getAllByRole('link');
    const linkTexts = links.map(link => link.textContent);

    // Should have nav links including FocusFlow logo and the main nav items
    expect(linkTexts.some(text => text?.includes('Today'))).toBe(true);
    expect(linkTexts.some(text => text?.includes('Week'))).toBe(true);
    expect(linkTexts.some(text => text?.includes('Goals'))).toBe(true);
    expect(linkTexts.some(text => text?.includes('History'))).toBe(true);
  });

  it('renders children content', () => {
    render(
      <AppShell>
        <div data-testid="test-content">Test Content</div>
      </AppShell>
    );

    expect(screen.getByTestId('test-content')).toBeInTheDocument();
    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });

  it('renders FocusFlow logo', () => {
    render(
      <AppShell>
        <div>Content</div>
      </AppShell>
    );

    expect(screen.getByText('FocusFlow')).toBeInTheDocument();
    expect(screen.getByText('FF')).toBeInTheDocument();
  });

  it('renders mode descriptions', () => {
    render(
      <AppShell>
        <div>Content</div>
      </AppShell>
    );

    // Each nav item has a description
    expect(screen.getByText("What you're doing")).toBeInTheDocument();
    expect(screen.getByText('What you plan')).toBeInTheDocument();
    expect(screen.getByText("Where you're heading")).toBeInTheDocument();
    expect(screen.getByText('What happened')).toBeInTheDocument();
  });

  it('renders logout button', () => {
    render(
      <AppShell>
        <div>Content</div>
      </AppShell>
    );

    expect(screen.getByText('Logout')).toBeInTheDocument();
  });

  it('renders philosophy tagline', () => {
    render(
      <AppShell>
        <div>Content</div>
      </AppShell>
    );

    expect(screen.getByText('Intent ≠ Execution ≠ Meaning')).toBeInTheDocument();
  });
});
