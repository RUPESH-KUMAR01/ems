import type { EventResponse, EventType } from '@/api/types'
import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { toEpochMs } from '@/lib/iso-datetime'
import { useMemo, useState } from 'react'

export type EventFilters = {
  search: string
  type: EventType | 'ALL'
  feeMin: string
  feeMax: string
  deadlineWithinDays: string
}

const defaultFilters: EventFilters = {
  search: '',
  type: 'ALL',
  feeMin: '',
  feeMax: '',
  deadlineWithinDays: '',
}

export function useEventFilters(events: EventResponse[]) {
  const [filters, setFilters] = useState<EventFilters>(defaultFilters)

  const filtered = useMemo(() => {
    return events.filter((e) => {
      if (filters.search) {
        const q = filters.search.toLowerCase()
        if (!e.name.toLowerCase().includes(q) && !e.description.toLowerCase().includes(q)) {
          return false
        }
      }
      if (filters.type !== 'ALL' && e.type !== filters.type) return false
      const fee = Number(e.registrationFee)
      if (filters.feeMin && fee < Number(filters.feeMin)) return false
      if (filters.feeMax && fee > Number(filters.feeMax)) return false
      if (filters.deadlineWithinDays) {
        const days = Number(filters.deadlineWithinDays)
        if (!Number.isFinite(days)) return true
        const deadline = toEpochMs(e.registrationDeadline)
        if (deadline == null) return false
        const limit = Date.now() + days * 86400000
        if (deadline > limit) return false
      }
      return true
    })
  }, [events, filters])

  return { filters, setFilters, filtered }
}

function FilterFields({
  filters,
  onChange,
}: {
  filters: EventFilters
  onChange: (f: EventFilters) => void
}) {
  return (
    <>
      <Input
        placeholder="Search events"
        value={filters.search}
        onChange={(e) => onChange({ ...filters, search: e.target.value })}
        aria-label="Search events"
      />
      <Select
        value={filters.type}
        onValueChange={(v) => onChange({ ...filters, type: v as EventFilters['type'] })}
        options={[
          { value: 'ALL', label: 'All types' },
          { value: 'SOLO', label: 'Solo' },
          { value: 'TEAM', label: 'Team' },
        ]}
      />
      <Input
        type="number"
        placeholder="Min fee"
        value={filters.feeMin}
        onChange={(e) => onChange({ ...filters, feeMin: e.target.value })}
        aria-label="Minimum fee"
      />
      <Input
        type="number"
        placeholder="Max fee"
        value={filters.feeMax}
        onChange={(e) => onChange({ ...filters, feeMax: e.target.value })}
        aria-label="Maximum fee"
      />
      <Input
        type="number"
        placeholder="Deadline within days"
        value={filters.deadlineWithinDays}
        onChange={(e) => onChange({ ...filters, deadlineWithinDays: e.target.value })}
        aria-label="Registration deadline within days"
      />
    </>
  )
}

export function FilterBar({
  filters,
  onChange,
}: {
  filters: EventFilters
  onChange: (f: EventFilters) => void
}) {
  return (
    <div className="hidden gap-3 rounded-lg border border-border bg-surface p-4 md:grid md:grid-cols-5">
      <FilterFields filters={filters} onChange={onChange} />
    </div>
  )
}

export function FilterSheet({
  filters,
  onChange,
}: {
  filters: EventFilters
  onChange: (f: EventFilters) => void
}) {
  const [open, setOpen] = useState(false)
  return (
    <>
      <div className="sticky bottom-20 z-10 flex justify-center md:hidden">
        <Button type="button" variant="secondary" onClick={() => setOpen(true)}>
          Filters
        </Button>
      </div>
      <Dialog open={open} onOpenChange={setOpen} title="Filter events">
        <div className="grid gap-3">
          <FilterFields filters={filters} onChange={onChange} />
          <Button type="button" className="w-full" onClick={() => setOpen(false)}>
            Apply filters
          </Button>
        </div>
      </Dialog>
    </>
  )
}
