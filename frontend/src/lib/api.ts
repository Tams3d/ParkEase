const STORAGE_KEY = 'parkease-web-v1'
const CAPACITY = 20
const DAILY_CAP_HOURS = 8

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
  ticketNo: number | null
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

type StoredTicket = {
  ticketNo: number
  plate: string
  owner: string
  vehicleType: string
  slots: number[]
  entryMillis: number
  hours: number
  charge: number
  active: boolean
}

type Stored = {
  nextTicket: number
  tickets: StoredTicket[]
}

const VEHICLE_TYPES: VehicleType[] = [
  { code: 'BIKE', name: 'Bike', rate: 10, slots: 1 },
  { code: 'CAR', name: 'Car', rate: 30, slots: 1 },
  { code: 'TRUCK', name: 'Truck', rate: 60, slots: 2 },
]

const empty: Stored = { nextTicket: 1001, tickets: [] }
const listeners = new Set<() => void>()

function read(): Stored {
  if (typeof window === 'undefined') {
    return { nextTicket: empty.nextTicket, tickets: [] }
  }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return { nextTicket: empty.nextTicket, tickets: [] }
    }
    const parsed = JSON.parse(raw) as Stored
    if (!parsed || !Array.isArray(parsed.tickets)) {
      return { nextTicket: empty.nextTicket, tickets: [] }
    }
    return parsed
  } catch {
    return { nextTicket: empty.nextTicket, tickets: [] }
  }
}

function write(state: Stored) {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  listeners.forEach((listener) => listener())
}

if (typeof window !== 'undefined') {
  window.addEventListener('storage', (event) => {
    if (event.key === STORAGE_KEY) {
      listeners.forEach((listener) => listener())
    }
  })
}

export function subscribe(listener: () => void): () => void {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}

function typeOf(code: string): VehicleType {
  const found = VEHICLE_TYPES.find((type) => type.code === code)
  if (!found) {
    throw new Error('Unknown vehicle type: ' + code)
  }
  return found
}

function toTicket(stored: StoredTicket): Ticket {
  const type = typeOf(stored.vehicleType)
  return {
    ticketNo: stored.ticketNo,
    number: 'T-' + stored.ticketNo,
    plate: stored.plate,
    owner: stored.owner,
    vehicleType: stored.vehicleType,
    vehicleTypeName: type.name,
    slots: stored.slots,
    entryMillis: stored.entryMillis,
    hours: stored.hours,
    charge: stored.charge,
    active: stored.active,
  }
}

function slotMap(state: Stored): SlotMap {
  const active = state.tickets.filter((ticket) => ticket.active)
  const slots: Slot[] = []

  for (let number = 1; number <= CAPACITY; number = number + 1) {
    const ticket = active.find((item) => item.slots.includes(number))
    if (!ticket) {
      slots.push({ number, free: true, display: 'FREE', ticketNo: null })
    } else {
      const type = typeOf(ticket.vehicleType)
      slots.push({
        number,
        free: false,
        display: ticket.plate + ' ' + type.code.charAt(0),
        ticketNo: ticket.ticketNo,
      })
    }
  }

  const free = slots.filter((slot) => slot.free).length
  return { capacity: CAPACITY, free, occupied: CAPACITY - free, slots }
}

function summaryOf(state: Stored): Summary {
  const active = state.tickets.filter((ticket) => ticket.active)
  const byType = VEHICLE_TYPES.map((type) => ({
    code: type.code,
    name: type.name,
    count: active.filter((ticket) => ticket.vehicleType === type.code).length,
  }))
  const closed = state.tickets.filter((ticket) => !ticket.active)
  const today = new Date().toDateString()
  const todayTotal = closed
    .filter((ticket) => new Date(ticket.entryMillis).toDateString() === today)
    .reduce((sum, ticket) => sum + ticket.charge, 0)
  const total = closed.reduce((sum, ticket) => sum + ticket.charge, 0)

  return {
    capacity: CAPACITY,
    free: CAPACITY - active.length,
    occupied: active.length,
    parked: active.length,
    byType,
    ticketCount: state.tickets.length,
    total,
    today: todayTotal,
  }
}

function findFreeSlots(state: Stored, needed: number): number[] {
  const used = new Set<number>()
  state.tickets
    .filter((ticket) => ticket.active)
    .forEach((ticket) => ticket.slots.forEach((slot) => used.add(slot)))

  for (let start = 1; start <= CAPACITY - needed + 1; start = start + 1) {
    const numbers: number[] = []
    for (let offset = 0; offset < needed; offset = offset + 1) {
      numbers.push(start + offset)
    }
    if (numbers.every((number) => !used.has(number))) {
      return numbers
    }
  }
  return []
}

export const api = {
  vehicleTypes: async (): Promise<VehicleType[]> => VEHICLE_TYPES,

  slots: async (): Promise<SlotMap> => slotMap(read()),

  summary: async (): Promise<Summary> => summaryOf(read()),

  ticket: async (ticketNo: number): Promise<Ticket> => {
    const state = read()
    const found = state.tickets.find((ticket) => ticket.ticketNo === ticketNo)
    if (!found) {
      throw new Error('No ticket T-' + ticketNo + '.')
    }
    return toTicket(found)
  },

  park: async (input: ParkInput): Promise<Ticket> => {
    const state = read()
    const plate = input.plate.trim().toUpperCase()
    const owner = input.owner.trim()

    if (!plate) {
      throw new Error('Number plate is required.')
    }
    if (!owner) {
      throw new Error('Owner name is required.')
    }
    if (state.tickets.some((ticket) => ticket.active && ticket.plate === plate)) {
      const existing = state.tickets.find(
        (ticket) => ticket.active && ticket.plate === plate,
      )
      throw new Error(
        'Vehicle ' + plate + ' is already parked (T-' + existing?.ticketNo + ').',
      )
    }

    const type = typeOf(input.vehicleType)
    const slots = findFreeSlots(state, type.slots)
    if (slots.length === 0) {
      throw new Error(
        'Parking full: no ' +
          (type.slots === 2 ? 'two consecutive slots' : 'free slot') +
          ' available for a ' +
          type.name.toLowerCase() +
          '.',
      )
    }

    const ticket: StoredTicket = {
      ticketNo: state.nextTicket,
      plate,
      owner,
      vehicleType: type.code,
      slots,
      entryMillis: Date.now(),
      hours: 0,
      charge: 0,
      active: true,
    }

    state.tickets.push(ticket)
    state.nextTicket = state.nextTicket + 1
    write(state)
    return toTicket(ticket)
  },

  exit: async (ticketNo: number, hours: number): Promise<Ticket> => {
    const state = read()
    const ticket = state.tickets.find(
      (item) => item.ticketNo === ticketNo && item.active,
    )
    if (!ticket) {
      throw new Error('No active ticket T-' + ticketNo + '.')
    }
    if (hours <= 0) {
      throw new Error('Hours parked must be greater than 0.')
    }

    const bill = Math.ceil(hours)
    const paidHours = Math.min(bill, DAILY_CAP_HOURS)
    const type = typeOf(ticket.vehicleType)

    ticket.hours = bill
    ticket.charge = paidHours * type.rate
    ticket.active = false

    write(state)
    return toTicket(ticket)
  },
}

export function formatDateTime(millis: number): string {
  return new Intl.DateTimeFormat('en-IN', {
    timeZone: 'Asia/Kolkata',
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(millis))
}

export function formatMoney(value: number): string {
  return 'Rs. ' + value
}

export function ticketNumber(value: string): number {
  const normalized = value.trim().toUpperCase().replace(/^T-?/, '')
  const parsed = Number(normalized)
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new Error('Enter a ticket number such as T-1001 or 1001.')
  }
  return parsed
}
