# ParkEase Web

TanStack Start web client for the ParkEase Spring Boot backend. It uses React, Vite,
file-based TanStack Router routes and Tailwind CSS.

## Requirements

- Bun 1.3 or newer
- ParkEase backend running on `http://localhost:8080` by default

## Run

```bash
bun install
bun run dev
```

Open <http://localhost:5173>.

The backend URL comes from `VITE_API_URL`. Copy `.env.example` to `.env` to override the
default:

```text
VITE_API_URL=http://localhost:8080
```

## Routes

| Route | Purpose |
|-------|---------|
| `/` | Dashboard with summary cards and the slot map |
| `/park` | Park a vehicle and issue a ticket |
| `/exit` | Exit a vehicle and show the bill |
| `/slots` | Full slot map |
| `/summary` | Slot, vehicle and collection summary |
| `/tickets/$ticketNo` | Active ticket details |

## Build

```bash
bun run build
```

The command runs `vite build` and `tsc --noEmit`. Production output is written to
`.output`. Use `bun run start` to serve it with Nitro.
