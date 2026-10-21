import * as authApi from '@/api/auth'
import { getToken, setAuthHandlers, setToken } from '@/api/client'
import type { Role, UserResponse } from '@/api/types'
import { hasRole } from '@/lib/role'
import { useToast } from '@/components/ui/Toast'
import {
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { createContext, useCallback, useContext, useEffect, useMemo, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'

type AuthContextValue = {
  user: UserResponse | undefined
  isLoading: boolean
  isAuthenticated: boolean
  hasRole: (minimum: Role) => boolean
  logout: () => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { toast } = useToast()

  const token = getToken()

  const meQuery = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: authApi.getMe,
    enabled: Boolean(token),
    retry: false,
  })

  useEffect(() => {
    setAuthHandlers({
      onUnauthorized: () => {
        setToken(null)
        queryClient.removeQueries({ queryKey: ['auth', 'me'] })
        navigate(`/login?redirect=${encodeURIComponent(window.location.pathname)}`)
      },
      onForbidden: (message) => {
        void queryClient.invalidateQueries({ queryKey: ['auth', 'me'] })
        toast(message || 'Permissions changed—sign in again', 'error')
      },
    })
  }, [navigate, queryClient, toast])

  const logout = useCallback(() => {
    authApi.logout()
    queryClient.removeQueries({ queryKey: ['auth', 'me'] })
    navigate('/login')
  }, [navigate, queryClient])

  const refreshUser = useCallback(async () => {
    await queryClient.invalidateQueries({ queryKey: ['auth', 'me'] })
  }, [queryClient])

  const value = useMemo<AuthContextValue>(
    () => ({
      user: meQuery.data,
      isLoading: Boolean(token) && meQuery.isLoading,
      isAuthenticated: Boolean(token && meQuery.data),
      hasRole: (minimum) => hasRole(meQuery.data?.role, minimum),
      logout,
      refreshUser,
    }),
    [logout, meQuery.data, meQuery.isLoading, refreshUser, token],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth requires AuthProvider')
  return ctx
}
