# EMS frontend

Vite + React + TypeScript client for the Event Management System Dropwizard API.

## Setup

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

| Variable | Description |
|----------|-------------|
| `VITE_API_URL` | Backend origin for the dev proxy (default `http://localhost:8080`). Browser calls `/api/*`; Vite proxies to this target. |
| `VITE_MOCK` | Set `true` to use in-memory API mocks (no backend). |

Production builds use same-origin `/api` unless you configure a reverse proxy.

## Scripts

- `npm run dev` — Vite dev server with `/api` proxy
- `npm run build` — `tsc -b` + production bundle
- `npm run lint` — oxlint
- `npm run format` — Prettier (if installed via `npx prettier`)

## Razorpay test mode

1. Register for a paid event → `POST /api/payments/orders` returns `razorpayKeyId`, `razorpayOrderId`, `amount` (INR). With backend `razorpay.mockPayments: true`, checkout completes instantly (no Razorpay modal).
2. Checkout.js opens with `amount` in **paise** (`amount * 100`).
3. On success, the client calls `POST /api/payments/verify` with Razorpay IDs + signature.
4. If the user closes the modal, call `POST /api/payments/fail/{razorpayOrderId}`.
5. Use [Razorpay test cards](https://razorpay.com/docs/payments/payments/test-card-details/) — webhooks are not required for the client verify path.

## API reference

See [docs/api-map.md](./docs/api-map.md) and typed clients in `src/api/`.

## Backend gaps & workarounds

| Gap | Frontend workaround |
|-----|---------------------|
| No CORS on Dropwizard | Dev: Vite proxy only. Prod: reverse proxy or add CORS on server. |
| `EventResponse` omits `startTime`/`endTime` | Moderator form saves schedule in `localStorage` (`event-schedule-store`); still sent on create/update body. |
| No venue/category | Filters omit category; pass subtitle from description. |
| `GET /api/registrations/mine` | My entries loads from API; `localStorage` is scoped per user for resume/register UX only. |
| No live seat counts | Show `maxParticipants` only, no “seats left”. |
| Moderator cannot list registrations | Manage UI links to teams; full tables use admin API. |
| Stats without time series | Admin charts use status/role maps from `SystemStatsResponse` only. |
| `TeamResponse` without `memberCount` | Fetch members; use `length` for min-size checks. |
| `TeamMembershipResponse.type` is request status | UI treats as status; use `/team-requests/me` for invite vs join context. |
| Role change mid-session | 403 invalidates `/me` + toast to re-authenticate. |
| Event team list requires USER auth | Backend class-level `@RolesAllowed("USER")` on `EventTeamResource`. |

## Admin: manual verification bypass

Admins can mark users verified without OTP at **Admin → Users**:

- **Mark email verified** / **Mark phone verified** — single channel
- **Bypass verification (both)** — confirmation dialog; calls `PUT /api/admin/users/{id}/verification`

Users should refresh the app or revisit **Profile** so `/api/auth/me` reflects the change.

## Ticket confirmation

A pass counts as a **confirmed ticket** only when:

1. **Profile complete** — email and phone verified (`GET /api/auth/me`).
2. **Registration** — status `REGISTERED` (free events) or payment `COMPLETED` (paid events).

The UI blocks registration and payment until verification is done, and pass stamps stay **Pending** until both requirements are met.

## Design

[docs/design-tokens.md](./docs/design-tokens.md)
