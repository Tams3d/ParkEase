import {
  HeadContent,
  Link,
  Scripts,
  createRootRoute,
} from '@tanstack/react-router'
import type { ReactNode } from 'react'
import appCss from '~/styles/app.css?url'
import { apiBaseUrl } from '~/lib/api'
import { Sheet, SheetContent, SheetTrigger } from '~/components/ui/sheet'
import { Separator } from '~/components/ui/separator'
import { SquareParking } from 'lucide-react'

export const Route = createRootRoute({
  head: () => ({
    meta: [
      { charSet: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      { title: 'ParkEase - Parking control' },
      {
        name: 'description',
        content: 'Park, exit, inspect slots and review the parking summary.',
      },
    ],
    links: [
      { rel: 'stylesheet', href: appCss },
      { rel: 'icon', href: '/favicon.ico' },
      { rel: 'manifest', href: '/site.webmanifest' },
    ],
  }),
  shellComponent: RootDocument,
  errorComponent: RootError,
  notFoundComponent: NotFound,
})

const navLinks = [
  { to: '/', label: 'Dashboard' },
  { to: '/park', label: 'Park' },
  { to: '/exit', label: 'Exit' },
  { to: '/slots', label: 'Slots' },
  { to: '/summary', label: 'Summary' },
] as const

function RootDocument({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <head>
        <HeadContent />
      </head>
      <body>
        <div id="app-shell">
          <aside id="sidebar" className="hidden lg:flex">
            <div className="flex h-12 items-center justify-between px-4 border-b border-sidebar-border">
              <Link to="/" className="flex items-center gap-2">
                <SquareParking className="h-5 w-5 text-primary" aria-hidden="true" />
                <span className="font-semibold text-lg">ParkEase</span>
              </Link>
            </div>
            <nav className="flex-1 p-4 space-y-1">
              {navLinks.map((link) => (
                <Link
                  key={link.to}
                  to={link.to}
                  className="nav-link rounded-md"
                  activeOptions={{ exact: link.to === '/' }}
                  activeProps={{ className: 'nav-link nav-link-active rounded-md' }}
                >
                  {link.label}
                </Link>
              ))}
            </nav>
            <div className="border-t border-sidebar-border p-4 text-xs text-muted-foreground">
              Spring Boot backend at <span className="font-mono">{apiBaseUrl}</span>
            </div>
          </aside>

          <div id="top-bar" className="lg:hidden">
            <Sheet>
              <SheetTrigger asChild>
                <button className="p-2 rounded-md hover:bg-sidebar-accent hover:text-sidebar-accent-foreground" aria-label="Menu">
                  <SquareParking className="h-5 w-5" />
                </button>
              </SheetTrigger>
              <SheetContent side="left" className="w-64 p-4">
                <Link to="/" className="flex items-center gap-2 mb-6">
                  <SquareParking className="h-5 w-5 text-primary" aria-hidden="true" />
                  <span className="font-semibold text-lg">ParkEase</span>
                </Link>
                <nav className="space-y-1">
                  {navLinks.map((link) => (
                    <Link
                      key={link.to}
                      to={link.to}
                      className="nav-link rounded-md"
                      activeOptions={{ exact: link.to === '/' }}
                      activeProps={{ className: 'nav-link nav-link-active rounded-md' }}
                    >
                      {link.label}
                    </Link>
                  ))}
                </nav>
                <Separator className="my-4" />
                <p className="text-xs text-muted-foreground">
                  Spring Boot backend at <span className="font-mono">{apiBaseUrl}</span>
                </p>
              </SheetContent>
            </Sheet>
          </div>

          <main id="sheet">
            <div id="sheet-inner">
              {children}
            </div>
          </main>
        </div>
        <Scripts />
      </body>
    </html>
  )
}

function RootError({ error }: { error: unknown }) {
  const message =
    error instanceof Error ? error.message : 'An unexpected error occurred.'
  return (
    <div className="rounded-lg border border-destructive bg-destructive/10 p-4 text-destructive">
      <p className="text-sm font-medium">Page error</p>
      <h1 className="mt-2 text-lg font-semibold">Something went wrong</h1>
      <p className="mt-2 text-sm">{message}</p>
    </div>
  )
}

function NotFound() {
  return (
    <div className="rounded-lg border border-destructive bg-destructive/10 p-4 text-destructive">
      <p className="text-sm font-medium">Not found</p>
      <h1 className="mt-2 text-lg font-semibold">This page does not exist</h1>
      <p className="mt-2 text-sm">Use the navigation above to return to ParkEase.</p>
    </div>
  )
}