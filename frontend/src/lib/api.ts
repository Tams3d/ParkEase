const configuredUrl = import.meta.env.VITE_API_URL?.trim()

export const apiBaseUrl = (configuredUrl && configuredUrl.length > 0
  ? configuredUrl
  : 'http://localhost:8080'
).replace(/\/+$/, '')

export type VehicleType = {
  code: string
  name: string
  rate: number
  slots: number
}

export type Ticket = {
  ticketNo: number
  number: string
  plate: string
  owner: string
  vehicleType: string
  vehicleTypeName: string
  slots: number[]
  entryMillis: number
  hours: number
  charge: number
  active: boolean
}

export type Slot = {
  number: number
  free: boolean
  display: string
}

export type SlotMap = {
  capacity: number
  free: number
  occupied: number
  slots: Slot[]
}

export type TypeCount = {
  code: string
  name: string
  count: number
}

export type Summary = {
  capacity: number
  free: number
  occupied: number
  parked: number
  byType: TypeCount[]
  ticketCount: number
  total: number
  today: number
}

type ParkInput = {
  vehicleType: string
  plate: string
  owner: string
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers)
  headers.set('Accept', 'application/json')
  if (init?.body) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers,
  })
  const text = await response.text()
  let body: unknown = null
  if (text) {
    try {
      body = JSON.parse(text)
    } catch {
      body = text
    }
  }

  if (!response.ok) {
    const message =
      body &&
      typeof body === 'object' &&
      'message' in body &&
      typeof body.message === 'string'
        ? body.message
        : `Request failed with HTTP ${response.status}.`
    throw new Error(message)
  }

  return body as T
}

export const api = {
  vehicleTypes: () => request<VehicleType[]>('/api/vehicle-types'),
  slots: () => request<SlotMap>('/api/slots'),
  summary: () => request<Summary>('/api/summary'),
  ticket: (ticketNo: number) => request<Ticket>(`/api/tickets/${ticketNo}`),
  park: (input: ParkInput) =>
    request<Ticket>('/api/tickets', {
      method: 'POST',
      body: JSON.stringify(input),
    }),
  exit: (ticketNo: number, hours: number) =>
    request<Ticket>(`/api/tickets/${ticketNo}/exit`, {
      method: 'POST',
      body: JSON.stringify({ hours }),
    }),
}

export function formatDateTime(millis: number): string {
  return new Intl.DateTimeFormat('en-IN', {
    timeZone: 'Asia/Kolkata',
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(millis))
}

export function formatMoney(value: number): string {
  return `Rs. ${value}`
}

export function ticketNumber(value: string): number {
  const normalized = value.trim().toUpperCase().replace(/^T-?/, '')
  const parsed = Number(normalized)
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new Error('Enter a ticket number such as T-1001 or 1001.')
  }
  return parsed
}
