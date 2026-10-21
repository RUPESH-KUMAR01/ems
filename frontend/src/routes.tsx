import { AppShell } from '@/components/layout/AppShell'
import { RequireRole } from '@/components/RequireRole'
import { AuthProvider } from '@/hooks/useAuth'
import { Outlet } from 'react-router-dom'
import { AdminDashboardPage } from '@/features/admin/AdminDashboardPage'
import {
  AdminEventsPage,
  AdminPaymentsPage,
  AdminRegistrationsPage,
  AdminTeamsPage,
} from '@/features/admin/AdminListPages'
import { AdminUsersPage } from '@/features/admin/AdminUsersPage'
import { LoginPage } from '@/features/auth/LoginPage'
import { SignupPage } from '@/features/auth/SignupPage'
import { VerifyPage } from '@/features/auth/VerifyPage'
import { EntriesPage } from '@/features/entries/EntriesPage'
import { EntryDetailPage } from '@/features/entries/EntryDetailPage'
import { EventDetailPage } from '@/features/events/EventDetailPage'
import { EventsListPage } from '@/features/events/EventsListPage'
import { LandingPage } from '@/features/events/LandingPage'
import { ManageEventsPage } from '@/features/manage/ManageEventsPage'
import { ManageEventEditPage, ManageEventNewPage } from '@/features/manage/ManageEventEditPage'
import { ProfilePage } from '@/features/profile/ProfilePage'
import { RegistrationWizard } from '@/features/register/RegistrationWizard'
import { TeamHubPage } from '@/features/teams/TeamHubPage'
import { TeamsPage } from '@/features/teams/TeamsPage'
import { ForbiddenPage } from '@/pages/ForbiddenPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { RouteErrorPage } from '@/pages/RouteErrorPage'
import { createBrowserRouter } from 'react-router-dom'

function AuthLayout() {
  return (
    <AuthProvider>
      <Outlet />
    </AuthProvider>
  )
}

export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    errorElement: <RouteErrorPage />,
    children: [
      {
        path: '/',
        element: <AppShell />,
        errorElement: <RouteErrorPage />,
        children: [
      { index: true, element: <LandingPage /> },
      { path: 'events', element: <EventsListPage /> },
      { path: 'events/:eventId', element: <EventDetailPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      {
        path: 'verify',
        element: (
          <RequireRole minimum="USER">
            <VerifyPage />
          </RequireRole>
        ),
      },
      {
        path: 'register/:eventId',
        element: (
          <RequireRole minimum="USER">
            <RegistrationWizard />
          </RequireRole>
        ),
      },
      {
        path: 'entries',
        element: (
          <RequireRole minimum="USER">
            <EntriesPage />
          </RequireRole>
        ),
      },
      {
        path: 'entries/:registrationId',
        element: (
          <RequireRole minimum="USER">
            <EntryDetailPage />
          </RequireRole>
        ),
      },
      {
        path: 'teams',
        element: (
          <RequireRole minimum="USER">
            <TeamsPage />
          </RequireRole>
        ),
      },
      {
        path: 'events/:eventId/teams/:teamId',
        element: (
          <RequireRole minimum="USER">
            <TeamHubPage />
          </RequireRole>
        ),
      },
      {
        path: 'profile',
        element: (
          <RequireRole minimum="USER">
            <ProfilePage />
          </RequireRole>
        ),
      },
      {
        path: 'manage/events',
        element: (
          <RequireRole minimum="MODERATOR">
            <ManageEventsPage />
          </RequireRole>
        ),
      },
      {
        path: 'manage/events/new',
        element: (
          <RequireRole minimum="MODERATOR">
            <ManageEventNewPage />
          </RequireRole>
        ),
      },
      {
        path: 'manage/events/:eventId/edit',
        element: (
          <RequireRole minimum="MODERATOR">
            <ManageEventEditPage />
          </RequireRole>
        ),
      },
      {
        path: 'admin',
        element: (
          <RequireRole adminOnly>
            <AdminDashboardPage />
          </RequireRole>
        ),
      },
      {
        path: 'admin/users',
        element: (
          <RequireRole adminOnly>
            <AdminUsersPage />
          </RequireRole>
        ),
      },
      {
        path: 'admin/events',
        element: (
          <RequireRole adminOnly>
            <AdminEventsPage />
          </RequireRole>
        ),
      },
      {
        path: 'admin/teams',
        element: (
          <RequireRole adminOnly>
            <AdminTeamsPage />
          </RequireRole>
        ),
      },
      {
        path: 'admin/registrations',
        element: (
          <RequireRole adminOnly>
            <AdminRegistrationsPage />
          </RequireRole>
        ),
      },
      {
        path: 'admin/payments',
        element: (
          <RequireRole adminOnly>
            <AdminPaymentsPage />
          </RequireRole>
        ),
      },
      { path: '403', element: <ForbiddenPage /> },
      { path: '*', element: <NotFoundPage /> },
        ],
      },
    ],
  },
])
