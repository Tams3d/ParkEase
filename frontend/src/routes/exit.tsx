import { createFileRoute, Link } from '@tanstack/react-router'
import type { FormEvent } from 'react'
import { useEffect, useState } from 'react'
import { Button } from '~/components/ui/button'
import { Card, CardContent } from '~/components/ui/card'
import { Label } from '~/components/ui/label'
import { Input } from '~/components/ui/input'
import { ErrorNotice } from '~/components/ui'
import { TicketCard } from '~/components/ui'
import { api, ticketNumber, type Ticket } from '~/lib/api'

export const Route = createFileRoute('/exit')({
  component: ExitPage,
})

function ExitPage() {
  const [number, setNumber] = useState('')
  const [hours, setHours] = useState('1')
  const [ticket, setTicket] = useState<Ticket | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const searchParams = new URLSearchParams(window.location.search)
      const ticketParam = searchParams.get('ticket') || searchParams.get('ticketNo')
      if (ticketParam) {
        setNumber(ticketParam.toUpperCase().startsWith('T-') ? ticketParam.toUpperCase() : `T-${ticketParam}`)
      }
    }
  }, [])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setTicket(null)
    setLoading(true)
    try {
      const parsedHours = Number(hours)
      if (!Number.isFinite(parsedHours) || parsedHours <= 0) {
        throw new Error('Enter hours greater than 0, for example 2.5.')
      }
      const closed = await api.exit(ticketNumber(number), parsedHours)
      setTicket(closed)
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not exit the vehicle.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="text-xs font-medium font-medium text-muted-foreground">Exit</p>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight text-foreground">
          Exit a vehicle
        </h1>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
          Enter the ticket number and parked hours. The backend rounds partial
          hours up, applies the daily cap and frees the ticket slots.
        </p>
      </div>

      {error && <ErrorNotice message={error} />}

      <div className="grid gap-6 lg:grid-cols-[1fr_1fr]">
        <Card>
          <CardContent className="p-6">
            <h2 className="text-lg font-semibold mb-4">Ticket and hours</h2>
            <form onSubmit={submit} className="grid gap-4">
              <div>
                <Label htmlFor="ticket">Ticket number</Label>
                <Input
                  id="ticket"
                  value={number}
                  onChange={(event) => setNumber(event.target.value)}
                  placeholder="T-1001 or 1001"
                  autoComplete="off"
                  required
                />
              </div>

              <div>
                <Label htmlFor="hours">Hours parked</Label>
                <Input
                  id="hours"
                  type="number"
                  min="0.1"
                  step="0.1"
                  value={hours}
                  onChange={(event) => setHours(event.target.value)}
                  required
                />
                <p className="mt-2 text-xs text-muted-foreground">
                  Partial hours are rounded up. Billing is capped at 8 hours.
                </p>
              </div>

              <div className="flex flex-wrap gap-2 pt-1">
                <Button type="submit" disabled={loading}>
                  {loading ? 'Closing ticket...' : 'Exit and bill'}
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
                  <h2 className="text-lg font-semibold mb-2">Receipt ready</h2>
                  <p className="text-sm leading-6 text-muted-foreground">
                    Slots {ticket.slots.join(' & ')} are free again. The charge is
                    recorded in MySQL and both clients now see the updated summary.
                  </p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    <Link to="/summary" className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted">
                      Open summary
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
                <h2 className="text-lg font-semibold mb-3">Billing rules</h2>
                <ul className="space-y-3 text-sm leading-6 text-muted-foreground">
                  <li>Bike: Rs. 10/hour, 1 slot.</li>
                  <li>Car: Rs. 30/hour, 1 slot.</li>
                  <li>Truck: Rs. 60/hour, 2 consecutive slots.</li>
                  <li>Partial hours round up with Math.ceil.</li>
                  <li>Billing is capped at the first 8 hours.</li>
                </ul>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  )
}