import { createFileRoute, Link } from '@tanstack/react-router'
import type { FormEvent } from 'react'
import { useEffect, useState } from 'react'
import { Button } from '~/components/ui/button'
import { Card, CardContent } from '~/components/ui/card'
import { Label } from '~/components/ui/label'
import { Input } from '~/components/ui/input'
import { ErrorNotice } from '~/components/ui'
import { TicketCard } from '~/components/ui'
import { api, type Ticket, type VehicleType } from '~/lib/api'

export const Route = createFileRoute('/park')({
  component: ParkPage,
})

function ParkPage() {
  const [types, setTypes] = useState<VehicleType[]>([])
  const [vehicleType, setVehicleType] = useState('')
  const [plate, setPlate] = useState('')
  const [owner, setOwner] = useState('')
  const [ticket, setTicket] = useState<Ticket | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api
      .vehicleTypes()
      .then((nextTypes) => {
        setTypes(nextTypes)
        setVehicleType((current) => current || nextTypes[0]?.code || '')
      })
      .catch((failure) => {
        setError(failure instanceof Error ? failure.message : 'Could not load vehicle types.')
      })
      .finally(() => setLoading(false))
  }, [])

  const selected = types.find((type) => type.code === vehicleType)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setTicket(null)
    setLoading(true)
    try {
      const issued = await api.park({ vehicleType, plate, owner })
      setTicket(issued)
      setPlate('')
      setOwner('')
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not park the vehicle.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="text-xs font-medium font-medium text-muted-foreground">Entry</p>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight text-foreground">
          Park a vehicle
        </h1>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
          Choose a vehicle type, enter the number plate and owner. The backend
          allocates slots and issues the next ticket.
        </p>
      </div>

      {error && <ErrorNotice message={error} />}

      <div className="grid gap-6 lg:grid-cols-[1.1fr_1fr]">
        <Card>
          <CardContent className="p-6">
            <h2 className="text-lg font-semibold mb-4">Vehicle details</h2>
            <form onSubmit={submit} className="grid gap-4">
              <div>
                <Label htmlFor="vehicleType">Vehicle type</Label>
                <select
                  id="vehicleType"
                  className="w-full rounded-xl border border-border bg-card px-3 py-2 text-sm text-foreground outline-none transition placeholder:text-muted-foreground focus:border-ring focus:ring-2 focus:ring-ring/20"
                  value={vehicleType}
                  onChange={(event) => setVehicleType(event.target.value)}
                  disabled={loading || types.length === 0}
                  required
                >
                  {types.map((type) => (
                    <option key={type.code} value={type.code}>
                      {type.name} - Rs. {type.rate}/hour -{' '}
                      {type.slots === 1 ? '1 slot' : `${type.slots} consecutive slots`}
                    </option>
                  ))}
                </select>
                {selected && (
                  <p className="mt-2 text-xs text-muted-foreground">
                    A {selected.name.toLowerCase()} needs {selected.slots}{' '}
                    {selected.slots === 1 ? 'slot' : 'consecutive slots'}.
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="plate">Number plate</Label>
                <Input
                  id="plate"
                  value={plate}
                  onChange={(event) => setPlate(event.target.value)}
                  placeholder="TN37AB1234"
                  autoComplete="off"
                  required
                />
                <p className="mt-2 text-xs text-muted-foreground">
                  Indian plate format. Spaces and hyphens are ignored.
                </p>
              </div>

              <div>
                <Label htmlFor="owner">Owner name</Label>
                <Input
                  id="owner"
                  value={owner}
                  onChange={(event) => setOwner(event.target.value)}
                  placeholder="Owner name"
                  maxLength={60}
                  required
                />
              </div>

              <div className="flex flex-wrap gap-2 pt-1">
                <Button type="submit" disabled={loading}>
                  {loading ? 'Issuing ticket...' : 'Park vehicle'}
                </Button>
                <Link to="/slots" className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted">
                  View slots
                </Link>
              </div>
            </form>
          </CardContent>
        </Card>

        <div className="space-y-6">
          {ticket ? (
            <>
              <TicketCard ticket={ticket} />
              <Card>
                <CardContent className="p-6">
                  <h2 className="text-lg font-semibold mb-2">Next step</h2>
                  <p className="text-sm leading-6 text-muted-foreground">
                    Keep ticket <span className="font-mono">{ticket.number}</span>{' '}
                    for exit. The ticket and slot changes are already visible to the
                    CLI and the web slot map.
                  </p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    <Link to="/tickets/$ticketNo" params={{ ticketNo: String(ticket.ticketNo) }} className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted">
                      Ticket details
                    </Link>
                    <Link to="/slots" className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted">
                      Open slot map
                    </Link>
                  </div>
                </CardContent>
              </Card>
            </>
          ) : (
            <Card>
              <CardContent className="p-6">
                <h2 className="text-lg font-semibold mb-3">Parking rules</h2>
                <ul className="space-y-3 text-sm leading-6 text-muted-foreground">
                  <li>Trucks need two consecutive free slots.</li>
                  <li>A plate cannot have two active tickets.</li>
                  <li>Partial hours are rounded up on exit.</li>
                  <li>The daily billing cap is 8 hours.</li>
                  <li>Parking and slot updates share one transaction.</li>
                </ul>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  )
}