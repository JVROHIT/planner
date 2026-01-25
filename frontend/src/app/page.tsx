import { redirect } from 'next/navigation';

/**
 * Root page redirects to /today (the main execution view).
 */
export default function Home() {
  redirect('/today');
}
