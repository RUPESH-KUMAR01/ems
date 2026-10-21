const PREFIX = 'ems.event.schedule.'

export interface EventScheduleDraft {
  startTime?: string
  endTime?: string
}

export function loadEventSchedule(eventId: number | 'new'): EventScheduleDraft {
  try {
    const raw = localStorage.getItem(`${PREFIX}${eventId}`)
    if (!raw) return {}
    return JSON.parse(raw) as EventScheduleDraft
  } catch {
    return {}
  }
}

export function saveEventSchedule(eventId: number | 'new', draft: EventScheduleDraft) {
  localStorage.setItem(`${PREFIX}${eventId}`, JSON.stringify(draft))
}

export function clearEventSchedule(eventId: number | 'new') {
  localStorage.removeItem(`${PREFIX}${eventId}`)
}
