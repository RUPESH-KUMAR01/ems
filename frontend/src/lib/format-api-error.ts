import { ApiError } from '@/api/client'

export function formatApiError(error: unknown): string {
  if (error instanceof ApiError) {
    const parts = [error.message, ...(error.details ?? [])].filter(Boolean)
    return parts.join(' · ')
  }
  if (error instanceof Error) return error.message
  return 'Request failed'
}
