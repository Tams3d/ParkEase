import { createFileRoute, Link } from '@tanstack/react-router'
import { useEffect, useState } from 'react'
import { RefreshCw } from 'lucide-react'
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
  const [refreshing, setRefreshing] = useState(false)

  async function loadTicket() {
    setRefreshing(true)
    setError('')
    try {
      const minWait = new Promise((resolve) => setTimeout(resolve, 400))
      const parsed = Number(ticketNo)
      const [fetched] = await Promise.all([
        api.ticket(parsed),
        minWait,
      ])
      setTicket(fetched)
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not load the ticket.')
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => {
    void loadTicket()
  }, [ticketNo])

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
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
        <button
          type="button"
          disabled={refreshing}
          className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted cursor-pointer disabled:opacity-60"
          onClick={() => void loadTicket()}
          title="Refresh ticket details"
        >
          <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
          <span>{refreshing ? 'Refreshing...' : 'Refresh'}</span>
        </button>
      </div>

      {error && <ErrorNotice message={error} />}
      {loading && !ticket && <Loading label="Loading ticket..." />}

      {ticket && (
        <>
          <Card>
            <CardContent className="p-6">
              <TicketCard ticket={ticket} />
            </CardContent>
          </Card>
          <div className="flex flex-wrap gap-2 pt-2">
            <Link
              to="/exit"
              search={{ ticket: `T-${ticketNo}` }}
              className="inline-flex items-center justify-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition hover:bg-primary/90"
            >
              Exit this vehicle
            </Link>
            <Link
              to="/slots"
              className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted"
            >
              View slot map
            </Link>
          </div>
        </>
      )}
    </div>
  )
}