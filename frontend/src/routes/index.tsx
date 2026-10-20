import { createFileRoute, Link } from '@tanstack/react-router'
import { useEffect, useState } from 'react'
import { Button } from '~/components/ui/button'
import { Card, CardContent } from '~/components/ui/card'
import { ErrorNotice } from '~/components/ui'
import { Loading } from '~/components/ui'
import { SlotGrid } from '~/components/ui'
import { Stat } from '~/components/ui'
import { TypeLegend } from '~/components/ui'
import { api, type SlotMap, type Summary } from '~/lib/api'

export const Route = createFileRoute('/')({
  component: Dashboard,
})

function Dashboard() {
  const [summary, setSummary] = useState<Summary | null>(null)
  const [slots, setSlots] = useState<SlotMap | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  async function load() {
    setLoading(true)
    setError('')
    try {
      const [nextSummary, nextSlots] = await Promise.all([
        api.summary(),
        api.slots(),
      ])
      setSummary(nextSummary)
      setSlots(nextSlots)
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not load the lot.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs font-medium font-medium text-muted-foreground">Parking control</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-foreground sm:text-4xl">
            Live lot status
          </h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            The web app and CLI both read and write through the same Spring Boot
            backend. Changes appear on both clients immediately.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link to="/park" className="inline-flex items-center justify-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition hover:bg-primary/90">
            Park a vehicle
          </Link>
          <Link to="/exit" className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted">
            Exit a vehicle
          </Link>
          <Button variant="secondary" size="sm" onClick={() => void load()}>
            Refresh
          </Button>
        </div>
      </div>

      {error && <ErrorNotice message={error} />}
      {loading && !summary && <Loading label="Loading the parking lot..." />}

      {summary && (
        <>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <Stat
              label="Capacity"
              value={summary.capacity}
              detail="Total parking slots"
            />
            <Stat
              label="Free"
              value={summary.free}
              tone="green"
              detail="Ready for the next vehicle"
            />
            <Stat
              label="Occupied"
              value={summary.occupied}
              tone="amber"
              detail="Slots currently in use"
            />
            <Stat
              label="Tickets"
              value={summary.ticketCount}
              detail={`${summary.parked} active vehicle${summary.parked === 1 ? '' : 's'}`}
            />
          </div>

          <div className="grid gap-6 lg:grid-cols-[1.5fr_1fr]">
            <Card>
              <CardContent className="pt-6">
                <div className="mb-4 flex items-start justify-between gap-3">
                  <div>
                    <h2 className="text-lg font-semibold">Slot map</h2>
                    <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
                      Occupied slots show the plate and vehicle code.
                    </p>
                  </div>
                  <Link to="/slots" className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted">
                    Open slot map
                  </Link>
                </div>
                {slots ? (
                  <SlotGrid slots={slots.slots} />
                ) : (
                  <Loading />
                )}
                <div className="mt-4">
                  <TypeLegend types={summary.byType} />
                </div>
              </CardContent>
            </Card>

            <div className="space-y-6">
              <Card>
                <CardContent className="p-6">
                  <div className="space-y-3">
                    {summary.byType.map((type) => (
                      <div
                        key={type.code}
                        className="flex items-center justify-between rounded-xl border border-border bg-card p-4"
                      >
                        <div>
                          <p className="text-sm font-medium text-foreground">
                            {type.name}
                          </p>
                          <p className="text-xs text-muted-foreground">{type.code}</p>
                        </div>
                        <span className="text-2xl font-semibold text-foreground">
                          {type.count}
                        </span>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="space-y-3"></div>
                  <Link to="/summary" className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted w-full justify-center">
                    Full summary
                  </Link>
                </CardContent>
              </Card>
            </div>
          </div>
        </>
      )}
    </div>
  )
}