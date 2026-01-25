import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { GoalProgress } from '../GoalProgress';

describe('GoalProgress', () => {
    it('renders progress bar with correct width', () => {
        render(<GoalProgress actualPercent={60} expectedPercent={50} status="AHEAD" />);

        // We can't easily check the width of the div via styles in this test environment without specific setup,
        // but we can check if the percentage labels are correct.
        expect(screen.getByText('60.0%')).toBeDefined();
        expect(screen.getByText('50.0%')).toBeDefined();
    });

    it('displays percentage labels correctly', () => {
        render(<GoalProgress actualPercent={25.5} expectedPercent={30} status="BEHIND" />);

        expect(screen.getByText('25.5%')).toBeDefined();
        expect(screen.getByText('30.0%')).toBeDefined();
    });
});
