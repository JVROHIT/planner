/**
 * Domain-aware API error mapping.
 * Translates technical error codes and HTTP status into human-readable messages.
 * 
 * Philosophy: Errors are part of the conversation.
 */

export interface ApiErrorInfo {
    title: string;
    message: string;
}

export function getErrorMessage(status: number, errorCode: string | null, message: string | null): ApiErrorInfo {
    // Priority 1: Handle specific error codes from backend
    if (errorCode) {
        switch (errorCode) {
            case 'PLAN_CLOSED':
                return {
                    title: 'Day Locked',
                    message: 'This day is closed and cannot be modified. The past is immutable.',
                };
            case 'DUPLICATE_EMAIL':
                return {
                    title: 'Account Exists',
                    message: 'An account with this email already exists. Try logging in.',
                };
            case 'INVALID_CREDENTIALS':
                return {
                    title: 'Login Failed',
                    message: 'Invalid email or password. Please try again.',
                };
            case 'GOAL_NOT_FOUND':
                return {
                    title: 'Goal Not Found',
                    message: 'The requested goal could not be found.',
                };
            case 'UNAUTHORIZED':
                return {
                    title: 'Session Expired',
                    message: 'Your session has expired. Please log in again.',
                };
            case 'FORBIDDEN':
                return {
                    title: 'Access Denied',
                    message: "You don't have permission to perform this action.",
                };
            case 'CONFLICT':
                return {
                    title: 'Conflict',
                    message: message || 'A data conflict occurred.',
                };
        }
    }

    // Priority 2: Handle HTTP status codes
    switch (status) {
        case 401:
            return {
                title: 'Authentication Required',
                message: 'Please log in to continue.',
            };
        case 403:
            return {
                title: 'Forbidden',
                message: "You don't have access to this resource.",
            };
        case 404:
            return {
                title: 'Not Found',
                message: 'The resource you requested could not be found.',
            };
        case 409:
            return {
                title: 'Conflict',
                message: message || 'The operation could not be completed due to a conflict.',
            };
        case 500:
        case 502:
        case 503:
        case 504:
            return {
                title: 'Server Error',
                message: 'Something went wrong on our end. Please try again later.',
            };
        default:
            return {
                title: 'Error',
                message: message || 'An unexpected error occurred.',
            };
    }
}
