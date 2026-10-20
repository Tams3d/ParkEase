import { Moon, Sun } from 'lucide-react'
import { Button } from '~/components/ui/button'
import { useTheme } from '~/lib/theme'

interface ThemeToggleProps {
  className?: string
  showLabel?: boolean
}

export function ThemeToggle({ className, showLabel = false }: ThemeToggleProps) {
  const { isDark, toggleTheme, mounted } = useTheme()

  const label = mounted ? (isDark ? 'Switch to light mode' : 'Switch to dark mode') : 'Toggle theme'

  return (
    <Button
      variant="ghost"
      size={showLabel ? 'sm' : 'icon'}
      onClick={toggleTheme}
      className={`relative inline-flex items-center justify-center rounded-lg text-muted-foreground transition-colors hover:bg-muted hover:text-foreground cursor-pointer ${className ?? ''}`}
      aria-label={label}
      title={label}
    >
      <div className="relative flex items-center justify-center size-4">
        <Sun
          className={`size-4 transition-all duration-300 ${
            mounted && isDark
              ? 'rotate-0 scale-100 opacity-100 text-amber-400'
              : '-rotate-90 scale-0 opacity-0 text-foreground'
          }`}
          aria-hidden="true"
        />
        <Moon
          className={`absolute size-4 transition-all duration-300 ${
            mounted && isDark
              ? 'rotate-90 scale-0 opacity-0 text-foreground'
              : 'rotate-0 scale-100 opacity-100 text-foreground'
          }`}
          aria-hidden="true"
        />
      </div>
      {showLabel && (
        <span className="ml-2 text-xs font-medium">
          {mounted ? (isDark ? 'Light' : 'Dark') : 'Theme'}
        </span>
      )}
      <span className="sr-only">{label}</span>
    </Button>
  )
}
