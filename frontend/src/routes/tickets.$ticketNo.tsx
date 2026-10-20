import { createFileRoute, Link } from '@tanstack/react-router'
import { useEffect, useState } from 'react'
import { Button } from '~/components/ui/button'
import { Card, CardContent } from '~/components/ui/card'
import { ErrorNotice } from '~/components/ui'
import { Loading } from '~/components/ui'
import { TicketCard } from '~/components/ui'
import { api, type Ticket } from '~/lib/api'

export const Route = createFileRoute('/tickets/$ticketNo')({
  component: TicketPage,
})

function TicketPage() {
  const { ticketNo } = Route.useParams()
  const [ticket, setTicket] = useState<Ticket | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    setError('')
    const parsed = Number(ticketNo)
    api
      .ticket(parsed)
      .then(setTicket)
      .catch((failure) => {
        setError(failure instanceof Error ? failure.message : 'Could not load the ticket.')
      })
      .finally(() => setLoading(false))
  }, [ticketNo])

  return (
    <div className="space-y-6">
      <div>
        <p className="text-xs font-medium font-medium text-muted-foreground">Active ticket</p>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight text-foreground">
          Ticket T-{ticketNo}
        </h1>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
          Ticket details are loaded from the backend. Closed tickets are not
          returned by the active-ticket endpoint.
        </p>
      </div>

      {error && <ErrorNotice message={error} />}
      {loading && <Loading label="Loading ticket..." />}

      {ticket && (
        <>
          <Card>
            <CardContent className="p-6">
              <TicketCard ticket={ticket} />
            </CardContent>
          </Card>
          <Button className="w-full rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition hover:bg-primary/90 mt-4">
            Exit this vehicle
          </Button>
          <Link to="/slots" className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted mt-2">
            View slot map
          </Link>
        </>
      )}
    </div>
  )
}