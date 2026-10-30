import { createFileRoute } from '@tanstack/react-router'
import { useEffect, useState } from 'react'
import { RefreshCw } from 'lucide-react'
import { Card, CardContent } from '~/components/ui/card'
import { ErrorNotice } from '~/components/ui'
import { Loading } from '~/components/ui'
import { SlotGrid } from '~/components/ui'
import { Stat } from '~/components/ui'
import { TypeLegend } from '~/components/ui'
import { api, subscribe, type SlotMap, type VehicleType } from '~/lib/api'

export const Route = createFileRoute('/slots')({
  component: SlotsPage,
})

function SlotsPage() {
  const [slots, setSlots] = useState<SlotMap | null>(null)
  const [types, setTypes] = useState<VehicleType[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  async function load(showSpinner = true) {
    if (showSpinner) {
      setRefreshing(true)
    }
    setError('')
    try {
      const minWait = new Promise((resolve) => setTimeout(resolve, 400))
      const [nextSlots, nextTypes] = await Promise.all([
        api.slots(),
        api.vehicleTypes(),
        minWait,
      ])
      setSlots(nextSlots)
      setTypes(nextTypes)
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not load the slot map.')
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => {
    void load()
    return subscribe(() => {
      void load(false)
    })
  }, [])

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs font-medium font-medium text-muted-foreground">Live state</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-foreground">
            Slot map
          </h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            Free slots show FREE; occupied slots show the plate, vehicle code
            and ticket number.
          </p>
        </div>
        <button
          type="button"
          disabled={refreshing}
          className="inline-flex items-center justify-center gap-2 rounded-md border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition hover:bg-muted cursor-pointer disabled:opacity-60"
          onClick={() => void load()}
          title="Refresh slot map"
        >
          <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
          <span>{refreshing ? 'Refreshing...' : 'Refresh'}</span>
        </button>
      </div>

      {error && <ErrorNotice message={error} />}
      {loading && !slots && <Loading label="Loading the slot map..." />}

      {slots && (
        <>
          <div className="grid gap-3 sm:grid-cols-3">
            <Stat label="Capacity" value={slots.capacity} />
            <Stat label="Free" value={slots.free} tone="green" />
            <Stat label="Occupied" value={slots.occupied} tone="amber" />
          </div>

          <Card>
            <CardContent className="p-6">
              <div className="mb-4">
                <h2 className="text-lg font-semibold">Twenty slots</h2>
                <p className="mt-1 text-sm text-muted-foreground">A truck occupies two consecutive slots with the same ticket.</p>
              </div>
              <SlotGrid slots={slots.slots} />
              <div className="mt-5">
                <TypeLegend types={types} />
              </div>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  )
}