import { describe, it, expect } from 'vitest';
import { getErrorMessage } from '../errors';

describe('getErrorMessage', () => {
    it('maps PLAN_CLOSED to specific message', () => {
        const error = getErrorMessage(409, 'PLAN_CLOSED', null);
        expect(error.title).toBe('Day Locked');
        expect(error.message).toContain('closed');
    });

    it('maps 401 status to authentication required', () => {
        const error = getErrorMessage(401, null, null);
        expect(error.title).toBe('Authentication Required');
    });

    it('maps 404 status to not found', () => {
        const error = getErrorMessage(404, null, null);
        expect(error.title).toBe('Not Found');
    });

    it('maps 500 status to server error', () => {
        const error = getErrorMessage(500, null, null);
        expect(error.title).toBe('Server Error');
    });

    it('uses provided message for conflicts if no specialized error code', () => {
        const customMsg = 'Username already taken';
        const error = getErrorMessage(409, null, customMsg);
        expect(error.message).toBe(customMsg);
    });
});
