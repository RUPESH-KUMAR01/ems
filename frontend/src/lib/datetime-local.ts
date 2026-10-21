/** Parse `datetime-local` value (no timezone) as local wall time → UTC ISO string. */
export function fromLocalDateTimeInput(value: string): string | undefined {
  if (!value) return undefined
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(value)
  if (!match) return undefined
  const year = Number(match[1])
  const month = Number(match[2])
  const day = Number(match[3])
  const hours = Number(match[4])
  const minutes = Number(match[5])
  const d = new Date(year, month - 1, day, hours, minutes, 0, 0)
  if (Number.isNaN(d.getTime())) return undefined
  return d.toISOString()
}

/** Format UTC ISO instant for `datetime-local` input in the user's local timezone. */
export function toLocalDateTimeInput(iso?: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}
