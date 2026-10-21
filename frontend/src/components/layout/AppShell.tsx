import { useAuth } from '@/hooks/useAuth'
import { useTheme } from '@/hooks/useTheme'
import { cn } from '@/lib/cn'
import { Link, NavLink, Outlet } from 'react-router-dom'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  cn('nav-link', isActive && 'nav-link-active')

export function AppShell() {
  const { isAuthenticated, user, logout, hasRole } = useAuth()
  const { dark, toggle } = useTheme()

  return (
    <div className="app-frame min-h-screen flex flex-col">
      <header className="site-header no-print">
        <div className="header-inner">
          <Link to="/" className="brand" aria-label="EMS home"><span className="brand-symbol">e</span><span className="brand-word">ems<span className="brand-reg">®</span></span></Link>
          <nav className="desktop-nav" aria-label="Main navigation">
            <NavLink to="/events" className={linkClass}>Discover</NavLink>
            {isAuthenticated ? (
              <>
                <NavLink to="/entries" className={linkClass}>My passes</NavLink>
                <NavLink to="/teams" className={linkClass}>Teams</NavLink>
                {hasRole('MODERATOR') ? <NavLink to="/manage/events" className={linkClass}>Manage</NavLink> : null}
                {user?.role === 'ADMIN' ? <NavLink to="/admin" className={linkClass}>Admin</NavLink> : null}
              </>
            ) : null}
          </nav>
          <div className="header-actions">
            <button type="button" className="theme-toggle" onClick={toggle} aria-label="Toggle color theme"><span>{dark ? '☼' : '☾'}</span></button>
            {isAuthenticated ? (
              <>
                <Link to="/profile" className="header-profile"><span className="profile-dot">{(user?.name ?? 'U').slice(0,1).toUpperCase()}</span><span className="profile-name">{user?.name?.split(' ')[0] ?? 'Profile'}</span></Link>
                <button type="button" className="header-signout" onClick={logout}>Sign out</button>
              </>
            ) : (
              <>
                <Link to="/login" className="header-login">Log in</Link>
                <Link to="/signup" className="header-join">Get your pass <span>↗</span></Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="app-main w-full flex-1">
        <Outlet />
      </main>

      <nav className="mobile-nav no-print" aria-label="Mobile navigation">
        <NavLink to="/events" className={linkClass}><span>◈</span>Discover</NavLink>
        <NavLink to="/entries" className={linkClass}><span>▤</span>Passes</NavLink>
        <NavLink to="/teams" className={linkClass}><span>◎</span>Teams</NavLink>
        <NavLink to="/profile" className={linkClass}><span>◉</span>Profile</NavLink>
      </nav>
    </div>
  )
}
