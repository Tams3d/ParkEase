import {
  HeadContent,
  Link,
  Scripts,
  createRootRoute,
} from '@tanstack/react-router'
import { useState, type ReactNode } from 'react'
import appCss from '~/styles/app.css?url'
import { apiBaseUrl } from '~/lib/api'
import { Sheet, SheetContent, SheetTrigger } from '~/components/ui/sheet'
import { Separator } from '~/components/ui/separator'
import { SquareParking } from 'lucide-react'
import { ThemeToggle } from '~/components/theme-toggle'

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
    scripts: [
      {
        children: `(function(){try{var t=localStorage.getItem('parkease-theme');var d=window.matchMedia('(prefers-color-scheme: dark)').matches;if(t==='dark'||(!t&&d)){document.documentElement.classList.add('dark')}else{document.documentElement.classList.remove('dark')}}catch(e){}})()`,
      },
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
  const [sidebarOpen, setSidebarOpen] = useState(true)

  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <HeadContent />
      </head>
      <body>
        <div id="app-shell">
          {/* Left Sidebar */}
          <aside
            id="sidebar"
            className="hidden lg:flex"
            data-collapsed={!sidebarOpen ? '' : undefined}
          >
            <div className="flex h-12 items-center px-4 border-b border-sidebar-border">
              <Link to="/" className="flex items-center gap-2">
                <SquareParking className="h-5 w-5 text-primary" aria-hidden="true" />
                <span className="font-semibold text-lg tracking-tight">ParkEase</span>
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
            <div className="border-t border-sidebar-border p-4 text-xs text-muted-foreground flex items-center justify-between">
              <span>Spring Boot backend at <span className="font-mono">{apiBaseUrl}</span></span>
            </div>
          </aside>

          {/* Right Main Area */}
          <div className="flex-1 flex flex-col min-w-0">
            <header id="top-bar">
              <div className="flex items-center gap-2">
                {/* On mobile, P logo opens the navigation sheet */}
                <div className="lg:hidden">
                  <Sheet>
                    <SheetTrigger asChild>
                      <button
                        className="p-1.5 rounded-md hover:bg-sidebar-accent hover:text-sidebar-accent-foreground text-foreground cursor-pointer"
                        aria-label="Menu"
                        title="Menu"
                      >
                        <SquareParking className="h-5 w-5 text-primary" />
                      </button>
                    </SheetTrigger>
                    <SheetContent side="left" className="w-64 p-4 flex flex-col justify-between">
                      <div>
                        <div className="flex items-center justify-between mb-6">
                          <Link to="/" className="flex items-center gap-2">
                            <SquareParking className="h-5 w-5 text-primary" aria-hidden="true" />
                            <span className="font-semibold text-lg tracking-tight">ParkEase</span>
                          </Link>
                        </div>
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
                      </div>
                      <div>
                        <Separator className="my-4" />
                        <p className="text-xs text-muted-foreground">
                          Spring Boot backend at <span className="font-mono">{apiBaseUrl}</span>
                        </p>
                      </div>
                    </SheetContent>
                  </Sheet>
                </div>

                {/* On desktop, P logo toggles the sidebar */}
                <div className="hidden lg:block">
                  <button
                    onClick={() => setSidebarOpen((prev) => !prev)}
                    className="flex items-center justify-center p-1.5 rounded-md hover:bg-muted text-primary transition-colors cursor-pointer"
                    aria-label={sidebarOpen ? 'Close sidebar' : 'Open sidebar'}
                    title={sidebarOpen ? 'Close sidebar' : 'Open sidebar'}
                  >
                    <SquareParking className="h-5 w-5" />
                  </button>
                </div>

                {/* Dark mode button right after the P logo */}
                <ThemeToggle />
              </div>
            </header>

            <main id="sheet">
              <div id="sheet-inner">
                {children}
              </div>
            </main>
          </div>
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