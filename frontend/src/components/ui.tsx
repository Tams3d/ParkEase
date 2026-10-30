import type { ReactNode } from 'react'
import type { Slot, Ticket } from '~/lib/api'
import { formatDateTime, formatMoney } from '~/lib/api'
import { Card, CardContent } from '~/components/ui/card'
import { Badge } from '~/components/ui/badge'
import { Alert, AlertDescription, AlertTitle } from '~/components/ui/alert'
import { Skeleton } from '~/components/ui/skeleton'
import { Separator } from '~/components/ui/separator'

type PanelProps = {
  title?: string
  description?: string
  actions?: ReactNode
  children: ReactNode
  className?: string
}

export function Panel({ title, description, actions, children, className = '' }: PanelProps) {
  return (
    <Card className={className}>
      {(title || description || actions) && (
        <div className="flex flex-wrap items-start justify-between gap-3 px-6 pt-6">
          <div>
            {title && <h2 className="text-lg font-semibold">{title}</h2>}
            {description && <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">{description}</p>}
          </div>
          {actions}
        </div>
      )}
      <CardContent className={title || description || actions ? 'pt-4' : ''}>{children}</CardContent>
    </Card>
  )
}

type StatProps = {
  label: string
  value: ReactNode
  detail?: ReactNode
  tone?: 'default' | 'green' | 'amber'
}

export function Stat({ label, value, detail, tone = 'default' }: StatProps) {
  return (
    <Card>
      <CardContent className="p-5">
        <p className="text-xs font-medium font-medium text-muted-foreground">{label}</p>
        <p className={`mt-2 text-3xl font-semibold tracking-tight ${tone === 'green' ? 'text-positive' : tone === 'amber' ? 'text-warning-foreground' : 'text-foreground'}`}>{value}</p>
        {detail && <p className="mt-2 text-xs leading-5 text-muted-foreground">{detail}</p>}
      </CardContent>
    </Card>
  )
}

export function ErrorNotice({ message }: { message: string }) {
  return (
    <Alert variant="destructive">
      <AlertTitle>Attention</AlertTitle>
      <AlertDescription>{message}</AlertDescription>
    </Alert>
  )
}

export function Loading({ label = 'Loading...' }: { label?: string }) {
  return (
    <div className="rounded-xl border border-border bg-card p-6 text-sm text-muted-foreground">
      <Skeleton className="h-4 w-1/3 mb-3" />
      <Skeleton className="h-4 w-2/3" />
      <p className="mt-3">{label}</p>
    </div>
  )
}

export function SlotGrid({ slots }: { slots: Slot[] }) {
  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-5">
      {slots.map((slot) => (
        <Card key={slot.number} className={`border ${slot.free ? 'border-positive-soft bg-positive-soft' : 'border-warning-soft bg-warning-soft'}`}>
          <CardContent className="p-3">
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-medium text-muted-foreground">Slot {slot.number}</span>
              <span className={`h-2 w-2 rounded-full ${slot.free ? 'bg-positive' : 'bg-warning'}`} />
            </div>
            <p className={`mt-3 break-words text-sm font-medium ${slot.free ? 'text-positive-foreground' : 'text-warning-foreground'}`}>
              {slot.display}
            </p>
            {slot.ticketNo !== null && (
              <p className="mt-1 text-xs font-medium text-warning-foreground">
                T-{slot.ticketNo}
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}

export function TypeLegend({ types }: { types: Array<{ code: string; name: string }> }) {
  return (
    <div className="flex flex-wrap gap-2">
      {types.map((type) => (
        <Badge key={type.code} variant="secondary" className="rounded-full px-3 py-1 text-xs font-medium">
          <span className="font-bold">{type.code.charAt(0)}</span> {type.name}
        </Badge>
      ))}
      <Badge className="rounded-full bg-positive-soft text-positive-foreground hover:bg-positive-soft">FREE available</Badge>
    </div>
  )
}

export function TicketCard({ ticket }: { ticket: Ticket }) {
  return (
    <Card>
      <CardContent className="p-6">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="text-xs font-medium font-medium text-muted-foreground">Ticket</p>
            <h2 className="mt-1 text-2xl font-semibold">{ticket.number}</h2>
            <p className="mt-1 text-sm text-muted-foreground">{ticket.plate} - {ticket.vehicleTypeName}</p>
          </div>
          <Badge variant={ticket.active ? 'default' : 'outline'} className={ticket.active ? 'bg-positive text-white hover:bg-positive' : ''}>
            {ticket.active ? 'Active' : 'Closed'}
          </Badge>
        </div>
        <dl className="mt-5 grid gap-4 text-sm sm:grid-cols-2">
          <div>
            <dt className="text-muted-foreground">Owner</dt>
            <dd className="mt-1 font-medium">{ticket.owner}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground">Slots</dt>
            <dd className="mt-1 font-medium">{ticket.slots.join(' & ')}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground">Entry</dt>
            <dd className="mt-1 font-medium">{formatDateTime(ticket.entryMillis)} IST</dd>
          </div>
          <div>
            <dt className="text-muted-foreground">{ticket.active ? 'Rate' : 'Billable hours'}</dt>
            <dd className="mt-1 font-medium">{ticket.active ? 'Charged on exit' : `${ticket.hours} hour${ticket.hours === 1 ? '' : 's'}`}</dd>
          </div>
        </dl>
        {!ticket.active && (
          <div className="mt-5 flex items-center justify-between rounded-xl border border-positive-soft bg-positive-soft px-4 py-3">
            <span className="text-sm text-positive-foreground">Charge</span>
            <span className="text-xl font-semibold text-positive-foreground">{formatMoney(ticket.charge)}</span>
          </div>
        )}
        <Separator className="mt-6" />
      </CardContent>
    </Card>
  )
}