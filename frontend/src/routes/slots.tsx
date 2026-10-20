import { createFileRoute } from '@tanstack/react-router'
import { useEffect, useState } from 'react'
import { Button } from '~/components/ui/button'
import { Card, CardContent } from '~/components/ui/card'
import { ErrorNotice } from '~/components/ui'
import { Loading } from '~/components/ui'
import { SlotGrid } from '~/components/ui'
import { Stat } from '~/components/ui'
import { TypeLegend } from '~/components/ui'
import { api, type SlotMap, type VehicleType } from '~/lib/api'

export const Route = createFileRoute('/slots')({
  component: SlotsPage,
})

function SlotsPage() {
  const [slots, setSlots] = useState<SlotMap | null>(null)
  const [types, setTypes] = useState<VehicleType[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  async function load() {
    setLoading(true)
    setError('')
    try {
      const [nextSlots, nextTypes] = await Promise.all([api.slots(), api.vehicleTypes()])
      setSlots(nextSlots)
      setTypes(nextTypes)
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not load the slot map.')
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
          <p className="text-xs font-medium font-medium text-muted-foreground">Live state</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-foreground">
            Slot map
          </h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            Every slot is read from the backend. Free slots show FREE; occupied
            slots show the plate and vehicle code.
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={() => void load()}>
          Refresh
        </Button>
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