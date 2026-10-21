import type { EventRegistrationResponse, RegistrationStatus } from '@/api/types'
import { compareIsoDesc, toIsoStringOrEpoch } from '@/lib/iso-datetime'

const LEGACY_KEY = 'ems.entries'

function storageKey(userId: number): string {
  return `ems.entries.${userId}`
}

export interface StoredEntry {
  userId: number
  registrationId: number
  eventId: number
  registeredAt: string
  registrationStatus?: RegistrationStatus
}

function normalizeEntry(raw: Partial<StoredEntry>, userId: number): StoredEntry | null {
  const registrationId = Number(raw.registrationId)
  const eventId = Number(raw.eventId)
  if (!Number.isFinite(registrationId) || !Number.isFinite(eventId)) return null
  const status = raw.registrationStatus
  const registrationStatus =
    status === 'PENDING' || status === 'REGISTERED' || status === 'CANCELLED'
      ? status
      : 'PENDING'
  return {
    userId,
    registrationId,
    eventId,
    registeredAt: toIsoStringOrEpoch(raw.registeredAt),
    registrationStatus,
  }
}

function readForUser(userId: number): StoredEntry[] {
  try {
    const raw = localStorage.getItem(storageKey(userId))
    if (!raw) return []
    const parsed = JSON.parse(raw) as Partial<StoredEntry>[] | Partial<StoredEntry>
    const list = Array.isArray(parsed) ? parsed : [parsed]
    const normalized = list
      .map((e) => normalizeEntry(e, userId))
      .filter((e): e is StoredEntry => e !== null)
    if (normalized.length !== list.length) {
      writeForUser(userId, normalized)
    }
    return normalized
  } catch {
    return []
  }
}

function writeForUser(userId: number, entries: StoredEntry[]) {
  localStorage.setItem(storageKey(userId), JSON.stringify(entries))
}

/** Drop pre-per-user cache so another account on the same browser is not affected. */
export function clearLegacyEntriesCache() {
  localStorage.removeItem(LEGACY_KEY)
}

export function listEntries(userId: number): StoredEntry[] {
  clearLegacyEntriesCache()
  return readForUser(userId).sort((a, b) => compareIsoDesc(a.registeredAt, b.registeredAt))
}

export function syncFromRegistrations(userId: number, registrations: EventRegistrationResponse[]) {
  const entries = registrations.map((r) =>
    normalizeEntry(
      {
        registrationId: r.id,
        eventId: r.eventId,
        registeredAt: r.registeredAt,
        registrationStatus: r.status,
      },
      userId,
    ),
  ).filter((e): e is StoredEntry => e !== null)
  writeForUser(userId, entries)
}

export function addEntry(userId: number, entry: Omit<StoredEntry, 'userId'>) {
  const normalized = normalizeEntry({ ...entry, userId }, userId)
  if (!normalized) return
  const entries = readForUser(userId).filter((e) => e.registrationId !== normalized.registrationId)
  entries.push(normalized)
  writeForUser(userId, entries)
}

export function removeEntry(userId: number, registrationId: number) {
  writeForUser(userId, readForUser(userId).filter((e) => e.registrationId !== registrationId))
}

export function getEntryForEvent(userId: number, eventId: number): StoredEntry | undefined {
  return readForUser(userId).find((e) => e.eventId === eventId)
}

export function updateEntryStatus(
  userId: number,
  registrationId: number,
  registrationStatus: RegistrationStatus,
) {
  const entries = readForUser(userId).map((e) =>
    e.registrationId === registrationId ? { ...e, registrationStatus } : e,
  )
  writeForUser(userId, entries)
}
