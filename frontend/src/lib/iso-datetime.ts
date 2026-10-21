/** Coerce API / localStorage values to an ISO-8601 string, or null if invalid. */
export function toIsoString(value: unknown): string | null {
  if (value == null || value === '') return null

  if (typeof value === 'string') {
    const ms = Date.parse(value)
    return Number.isFinite(ms) ? new Date(ms).toISOString() : null
  }

  if (typeof value === 'number' && Number.isFinite(value)) {
    // Jackson often serializes instants as epoch seconds; JS Date expects milliseconds.
    const ms = value < 1e12 ? value * 1000 : value
    const d = new Date(ms)
    return Number.isNaN(d.getTime()) ? null : d.toISOString()
  }

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value.toISOString()
  }

  // Legacy Jackson-style [year, month, day, hour?, minute?, second?]
  if (Array.isArray(value) && value.length >= 3) {
    const y = Number(value[0])
    const m = Number(value[1])
    const d = Number(value[2])
    const h = Number(value[3] ?? 0)
    const mi = Number(value[4] ?? 0)
    const s = Number(value[5] ?? 0)
    if (![y, m, d].every(Number.isFinite)) return null
    const date = new Date(y, m - 1, d, h, mi, s)
    return Number.isNaN(date.getTime()) ? null : date.toISOString()
  }

  if (typeof value === 'object' && value !== null && 'toString' in value) {
    const s = String(value)
    const ms = Date.parse(s)
    if (Number.isFinite(ms)) return new Date(ms).toISOString()
  }

  return null
}

/** Like `toIsoString` but always returns a string (invalid → epoch) for stable sorting. */
export function toIsoStringOrEpoch(value: unknown): string {
  return toIsoString(value) ?? new Date(0).toISOString()
}

/** Sort key for descending chronological order (newest first). */
export function compareIsoDesc(a: unknown, b: unknown): number {
  const ia = toIsoString(a)
  const ib = toIsoString(b)
  if (!ia && !ib) return 0
  if (!ia) return 1
  if (!ib) return -1
  return ib.localeCompare(ia)
}

export function toEpochMs(value: unknown): number | null {
  const iso = toIsoString(value)
  if (!iso) return null
  const ms = Date.parse(iso)
  return Number.isFinite(ms) ? ms : null
}
