import { createFileRoute } from '@tanstack/react-router'
import { useEffect, useState } from 'react'
import { RefreshCw } from 'lucide-react'
import { Card, CardContent } from '~/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '~/components/ui/table'
import { Progress } from '~/components/ui/progress'
import { Separator } from '~/components/ui/separator'
import { ErrorNotice } from '~/components/ui'
import { Loading } from '~/components/ui'
import { Stat } from '~/components/ui'
import { api, type Summary } from '~/lib/api'

export const Route = createFileRoute('/summary')({
  component: SummaryPage,
})

function SummaryPage() {
  const [summary, setSummary] = useState<Summary | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  async function load() {
    setRefreshing(true)
    setError('')
    try {
      const minWait = new Promise((resolve) => setTimeout(resolve, 400))
      const [nextSummary] = await Promise.all([
        api.summary(),
        minWait,
      ])
      setSummary(nextSummary)
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not load the summary.')
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs font-medium font-medium text-muted-foreground">Reporting</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-foreground">
            Parking summary
          </h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            Active slot state, vehicle counts and collection totals from the
            shared MySQL-backed backend.
          </p>
        </div>
        <button
          type="button"
          disabled={refreshing}
          className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted cursor-pointer disabled:opacity-60"
          onClick={() => void load()}
          title="Refresh summary data"
        >
          <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
          <span>{refreshing ? 'Refreshing...' : 'Refresh'}</span>
        </button>
      </div>

      {error && <ErrorNotice message={error} />}
      {loading && !summary && <Loading label="Loading the summary..." />}

      {summary && (
        <>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <Stat
              label="Slots"
              value={`${summary.occupied}/${summary.capacity}`}
              detail={`${summary.free} free`}
            />
            <Stat
              label="Vehicles parked"
              value={summary.parked}
              detail="Active tickets in memory and MySQL"
            />
            <Stat
              label="Tickets issued"
              value={summary.ticketCount}
              detail="All active and closed tickets"
            />
            <Stat
              label="Today"
              value={`Rs. ${summary.today}`}
              tone="green"
              detail="Closed-ticket collection"
            />
          </div>

          <div className="grid gap-6 lg:grid-cols-[1.1fr_1fr]">
            <Card>
              <CardContent className="p-6">
                <h2 className="text-lg font-semibold mb-4">By vehicle type</h2>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Type</TableHead>
                      <TableHead>Code</TableHead>
                      <TableHead className="text-right">Parked</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {summary.byType.map((type) => (
                      <TableRow key={type.code}>
                        <TableCell className="font-medium">{type.name}</TableCell>
                        <TableCell className="font-mono text-muted-foreground">{type.code}</TableCell>
                        <TableCell className="text-right text-lg font-semibold">
                          {type.count}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>

            <div className="space-y-6">
              <Card>
                <CardContent className="p-6">
                  <h2 className="text-lg font-semibold mb-4">Collections</h2>
                  <div className="space-y-4">
                    <div className="flex items-end justify-between gap-4">
                      <span className="text-sm text-muted-foreground">Today</span>
                      <span className="text-3xl font-semibold text-positive">
                        Rs. {summary.today}
                      </span>
                    </div>
                    <Separator />
                    <div className="flex items-end justify-between gap-4">
                      <span className="text-sm text-muted-foreground">All time</span>
                      <span className="text-3xl font-semibold text-foreground">
                        Rs. {summary.total}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <h2 className="text-lg font-semibold mb-4">Capacity</h2>
                  <Progress
                    value={summary.capacity ? (summary.occupied / summary.capacity) * 100 : 0}
                    className="mb-4"
                  />
                  <div className="space-y-3 text-sm">
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Total slots</span>
                      <span className="font-semibold">{summary.capacity}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Occupied</span>
                      <span className="font-semibold text-warning-foreground">
                        {summary.occupied}
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Free</span>
                      <span className="font-semibold text-positive">
                        {summary.free}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </>
      )}
    </div>
  )
}