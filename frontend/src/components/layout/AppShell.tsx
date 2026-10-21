import { useAuth } from '@/hooks/useAuth'
import { useTheme } from '@/hooks/useTheme'
import { cn } from '@/lib/cn'
import { Link, NavLink, Outlet } from 'react-router-dom'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  cn('text-sm', isActive ? 'text-ink font-medium' : 'text-ink-muted hover:text-ink')

export function AppShell() {
  const { isAuthenticated, user, logout, hasRole } = useAuth()
  const { dark, toggle } = useTheme()

  return (
    <div className="min-h-screen flex flex-col">
      <header className="no-print sticky top-0 z-30 border-b border-border bg-paper/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <Link to="/" className="font-display text-lg font-bold tracking-tight">
            EMS Pass
          </Link>
          <nav className="hidden items-center gap-4 md:flex">
            <NavLink to="/events" className={linkClass}>Events</NavLink>
            {isAuthenticated ? (
              <>
                <NavLink to="/entries" className={linkClass}>My entries</NavLink>
                <NavLink to="/teams" className={linkClass}>Teams</NavLink>
                {hasRole('MODERATOR') ? (
                  <NavLink to="/manage/events" className={linkClass}>Manage</NavLink>
                ) : null}
                {user?.role === 'ADMIN' ? (
                  <NavLink to="/admin" className={linkClass}>Admin</NavLink>
                ) : null}
                <NavLink to="/profile" className={linkClass}>Profile</NavLink>
              </>
            ) : (
              <>
                <NavLink to="/login" className={linkClass}>Log in</NavLink>
                <NavLink to="/signup" className={linkClass}>Sign up</NavLink>
              </>
            )}
          </nav>
          <div className="flex items-center gap-2">
            <button type="button" className="text-sm" onClick={toggle} aria-label="Toggle theme">
              {dark ? 'Light' : 'Dark'}
            </button>
            {isAuthenticated ? (
              <button type="button" className="text-sm text-ink-muted" onClick={logout}>
                Sign out
              </button>
            ) : null}
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 pb-20 md:pb-6">
        <Outlet />
      </main>

      <nav
        className="no-print fixed bottom-0 left-0 right-0 flex border-t border-border bg-surface md:hidden"
        aria-label="Mobile"
      >
        <NavLink to="/events" className="flex-1 py-3 text-center text-xs">Events</NavLink>
        <NavLink to="/entries" className="flex-1 py-3 text-center text-xs">Entries</NavLink>
        <NavLink to="/teams" className="flex-1 py-3 text-center text-xs">Teams</NavLink>
        <NavLink to="/profile" className="flex-1 py-3 text-center text-xs">Profile</NavLink>
      </nav>
    </div>
  )
}
