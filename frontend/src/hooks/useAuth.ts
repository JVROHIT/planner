/* eslint-disable react-hooks/set-state-in-effect */
'use client';

/**
 * Re-exports useAuth from the centralized AuthProvider.
 * This maintains backward compatibility while ensuring all components
 * see the same shared auth state.
 */
import { useAuth } from '@/providers/AuthProvider';
export { useAuth };
export default useAuth;
