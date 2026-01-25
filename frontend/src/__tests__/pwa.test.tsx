import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import fs from 'fs';
import path from 'path';
import { InstallPrompt } from '@/components/pwa/InstallPrompt';
import { OfflineIndicator } from '@/components/pwa/OfflineIndicator';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true,
});

describe('PWA Configuration', () => {
  describe('manifest.json', () => {
    it('manifest.json exists and is valid JSON', () => {
      const manifestPath = path.join(process.cwd(), 'public', 'manifest.json');
      expect(fs.existsSync(manifestPath)).toBe(true);

      const manifestContent = fs.readFileSync(manifestPath, 'utf-8');
      const manifest = JSON.parse(manifestContent);
      expect(manifest).toBeDefined();
    });

    it('manifest has required fields (name, short_name, icons, start_url)', () => {
      const manifestPath = path.join(process.cwd(), 'public', 'manifest.json');
      const manifestContent = fs.readFileSync(manifestPath, 'utf-8');
      const manifest = JSON.parse(manifestContent);

      expect(manifest.name).toBe('FocusFlow');
      expect(manifest.short_name).toBe('FocusFlow');
      expect(manifest.start_url).toBe('/today');
      expect(manifest.icons).toBeDefined();
      expect(Array.isArray(manifest.icons)).toBe(true);
      expect(manifest.icons.length).toBeGreaterThan(0);
    });

    it('theme_color matches app theme', () => {
      const manifestPath = path.join(process.cwd(), 'public', 'manifest.json');
      const manifestContent = fs.readFileSync(manifestPath, 'utf-8');
      const manifest = JSON.parse(manifestContent);

      // FocusFlow primary color is #3b82f6 (blue-500)
      expect(manifest.theme_color).toBe('#3b82f6');
    });

    it('display is standalone for app-like experience', () => {
      const manifestPath = path.join(process.cwd(), 'public', 'manifest.json');
      const manifestContent = fs.readFileSync(manifestPath, 'utf-8');
      const manifest = JSON.parse(manifestContent);

      expect(manifest.display).toBe('standalone');
    });
  });

  describe('PWA Icons', () => {
    it('icons exist at specified paths', () => {
      const iconPaths = [
        'public/icons/icon-192.png',
        'public/icons/icon-512.png',
        'public/icons/icon-maskable-512.png',
        'public/icons/apple-touch-icon.png',
      ];

      iconPaths.forEach((iconPath) => {
        const fullPath = path.join(process.cwd(), iconPath);
        expect(fs.existsSync(fullPath)).toBe(true);
      });
    });
  });
});

describe('InstallPrompt', () => {
  const mockPrompt = vi.fn();
  const mockUserChoice = Promise.resolve({ outcome: 'accepted' as const });

  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.clear();

    // Mock matchMedia for standalone check
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation((query: string) => ({
        matches: false, // Not in standalone mode
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders install button when beforeinstallprompt fired', async () => {
    render(<InstallPrompt />);

    // Simulate beforeinstallprompt event
    const event = new Event('beforeinstallprompt') as Event & {
      prompt: () => Promise<void>;
      userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
    };
    event.prompt = mockPrompt;
    event.userChoice = mockUserChoice;
    window.dispatchEvent(event);

    await waitFor(() => {
      expect(screen.getByText('Install FocusFlow')).toBeInTheDocument();
      expect(screen.getByText('Install')).toBeInTheDocument();
    });
  });

  it('hides when app is already installed', () => {
    // Mock as already in standalone mode
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation((query: string) => ({
        matches: query === '(display-mode: standalone)', // In standalone mode
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });

    render(<InstallPrompt />);

    // Should not render anything when already installed
    expect(screen.queryByText('Install FocusFlow')).not.toBeInTheDocument();
  });

  it('has dismiss button', async () => {
    render(<InstallPrompt />);

    // Simulate beforeinstallprompt event
    const event = new Event('beforeinstallprompt') as Event & {
      prompt: () => Promise<void>;
      userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
    };
    event.prompt = mockPrompt;
    event.userChoice = mockUserChoice;
    window.dispatchEvent(event);

    await waitFor(() => {
      expect(screen.getByText('Not now')).toBeInTheDocument();
    });

    // Click dismiss
    fireEvent.click(screen.getByText('Not now'));

    // Should remember dismissal
    expect(localStorage.getItem('focusflow_pwa_dismissed')).toBe('true');
  });
});

describe('OfflineIndicator', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    // Default to online
    Object.defineProperty(navigator, 'onLine', {
      writable: true,
      value: true,
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('hides when navigator.onLine is true', () => {
    Object.defineProperty(navigator, 'onLine', { writable: true, value: true });

    render(<OfflineIndicator />);

    // Should not show offline message when online
    expect(screen.queryByText(/offline/i)).not.toBeInTheDocument();
  });

  it('shows when navigator.onLine is false', () => {
    Object.defineProperty(navigator, 'onLine', { writable: true, value: false });

    render(<OfflineIndicator />);

    expect(screen.getByText(/offline.*cached data/i)).toBeInTheDocument();
  });

  it('updates on online/offline events', async () => {
    Object.defineProperty(navigator, 'onLine', { writable: true, value: true });

    render(<OfflineIndicator />);

    // Should not be visible when online
    expect(screen.queryByText(/offline/i)).not.toBeInTheDocument();

    // Go offline
    Object.defineProperty(navigator, 'onLine', { writable: true, value: false });
    window.dispatchEvent(new Event('offline'));

    await waitFor(() => {
      expect(screen.getByText(/offline.*cached data/i)).toBeInTheDocument();
    });

    // Go back online
    Object.defineProperty(navigator, 'onLine', { writable: true, value: true });
    window.dispatchEvent(new Event('online'));

    await waitFor(() => {
      expect(screen.getByText('Back online')).toBeInTheDocument();
    });
  });
});
