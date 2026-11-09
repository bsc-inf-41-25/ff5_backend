# Ticketmaster API endpoints

Base URL on the server's current Wi-Fi network: `http://10.236.249.177:8080`.
Use the server's current Wi-Fi IPv4 address; it can change when reconnecting.
Every endpoint below includes its actual `/api` prefix where the controller
defines one.

## Authentication and request conventions

- Register and login return JSON with `token` and `refreshToken`. Send the
  access token on protected calls as `Authorization: Bearer <access-token>`.
- Date/time values are JSON strings for Java `LocalDateTime`, normally formatted
  as `yyyy-MM-dd'T'HH:mm:ss` (for example `2026-11-07T18:30:00`), with no
  timezone suffix.
- The application exception handler returns
  `{"timestamp":"...","status":400,"error":"Bad Request","message":"..."}` for
  handled validation and application errors. Spring Security-generated 401/403
  responses may use its own response format.
- `Public` means no token is required. `Authenticated` means any valid token
  unless a role is listed. Endpoint-specific ownership/customer checks may also
  apply.

## Endpoint inventory

| Method | Full path | Token / role | Request body or parameters | Response fields |
|---|---|---|---|---|
| POST | `/api/auth/register` | Public | `email` string, `password` string (min 6), `role` enum (`CUSTOMER`, `ORGANIZER`, `VALIDATOR`) | `token`, `refreshToken` |
| POST | `/api/auth/login` | Public | `email` string, `password` string | `token`, `refreshToken` |
| POST | `/api/auth/google` | Public | `idToken` string | `token`, `refreshToken` |
| POST | `/api/auth/refresh` | Public | `refreshToken` string | `token`, `refreshToken` |
| GET | `/api/auth/qr/{text}` | Public | Path `text` string | PNG image bytes |
| GET | `/api/events` | Public | None | Array of events: `id`, `organizerId`, `title`, `venue`, `location`, `dateTime`, `startTime`, `endTime`, `status`, `organizerUsername` |
| GET | `/api/events/{id}` | Public | Path `id` long | Event fields listed above |
| POST | `/api/events` | Authenticated | Event entity: `title`, `venue`, `location`, `dateTime`, `startTime`, `endTime`, `status`, `organizerId`, `organizerUsername`; server sets `organizerUsername` and `status` on create | Event fields listed above; HTTP 201 |
| PUT | `/api/events/{id}` | Authenticated | Event entity; update uses `title`, `venue`, `location`, `dateTime`, `startTime`, `endTime` | Event fields listed above |
| PATCH | `/api/events/{id}/publish` | Authenticated | None | Event fields listed above |
| PATCH | `/api/events/{id}/cancel` | Authenticated | None | Event fields listed above |
| GET | `/api/organizer/events` | `ORGANIZER` | None | Array of event fields |
| GET | `/api/events/{eventId}/ticket-types` | Public | Path `eventId` long; optional query `name` string | Array of ticket types: `id`, `event`, `name`, `price`, `totalQuantity`, `quantitySold`, `remaining` |
| GET | `/api/ticket-types/{id}` | Public | Path `id` long | Ticket type fields listed above; HTTP 404 if missing |
| POST | `/api/events/{eventId}/ticket-types` | Authenticated | `name` string, `price` decimal, `totalQuantity` integer | Ticket type fields listed above; HTTP 201 |
| PUT | `/api/ticket-types/{id}` | Authenticated | `price` decimal, `totalQuantity` integer; `name` is accepted by the request class but not applied | Ticket type fields listed above |
| GET | `/api/interests` | Public | None | Array of interests: `id`, `name` |
| GET | `/api/customers/me` | Authenticated | None | `id`, `userId`, `fullName`, `phone`, `city`, `latitude`, `longitude` |
| PUT | `/api/customers/me` | Authenticated | `fullName` string, `phone` string, `city` string, `latitude` number, `longitude` number | Profile fields listed above |
| GET | `/api/customers/me/interests` | Authenticated; customer only | None | Array of customer interests: `id`, `customerId`, `interest` (`id`, `name`) |
| POST | `/api/customers/me/interests?interestId={id}` | Authenticated; customer only | Query `interestId` long | Customer-interest fields listed above |
| DELETE | `/api/customers/me/interests/{interestId}` | Authenticated; customer only | Path `interestId` long | Empty response |
| POST | `/api/orders` | `CUSTOMER` | `eventId` long, `items` array of `{ticketTypeId: long, quantity: integer}` | `id`, `eventId`, `totalAmount`, `status`, `createdAt`, `items` (`ticketTypeId`, `quantity`, `unitPrice`) |
| GET | `/api/orders/my` | `CUSTOMER` | None | Array of order response fields |
| GET | `/api/orders/{id}` | `CUSTOMER` | Path `id` long | Order response fields |
| POST | `/api/orders/{id}/cancel` | `CUSTOMER` | Path `id` long | Order response fields |
| POST | `/api/orders/{id}/status` | No token (internal payment integration) | `status` string (`OrderStatus` enum) | HTTP 200, empty body |
| POST | `/api/payments/checkout` | Authenticated | `amount` number, `orderId` long | `checkoutUrl` string |
| GET | `/api/payments/callback?tx_ref={tx_ref}` | Public callback | Query `tx_ref` string | Redirect to `myapp://payment-result?status=success&tx_ref=...` |
| GET | `/api/payments/return?tx_ref={tx_ref}&status={status}` | Public return | Optional query `tx_ref` string; optional `status` string (defaults to `failed`) | Redirect to `myapp://payment-result?status=...&tx_ref=...` |
| GET | `/api/tickets/my` | Authenticated; customer ticket ownership check | None | Array of ticket fields below |
| GET | `/api/tickets/{id}` | Authenticated; customer ticket ownership check | Path `id` long | `id`, `orderId`, `ticketCode`, `eventId`, `eventTitle`, `venue`, `location`, `eventDateTime`, `eventStartTime`, `eventEndTime`, `ticketTypeId`, `ticketTypeName`, `ticketPrice`, `status`, `issuedAt`, `usedAt` |
| POST | `/api/tickets/{id}/cancel` | Authenticated; customer ticket ownership check | Path `id` long | Empty response; HTTP 204 |
| POST | `/api/tickets/validate` | `VALIDATOR` | `ticketCode` string | `ticketId`, `ticketCode`, `eventTitle`, `status`, `usedAt`, `message` |
| GET | `/api/reports/events/{eventId}/sales` | Authenticated | Path `eventId` long | Array of `{ticketTypeId, ticketsSold, revenue}` |

No controller mapping with an empty path was found, and the auth routes use
`/api/auth/...` consistently (not `/auth/login`). Event and ticket-type
create/update services currently have organizer-ownership checks disabled for
the demo; valid authentication does not currently enforce ownership there.

## Examples

Register:

```json
{
  "email": "customer@example.com",
  "password": "example-password",
  "role": "CUSTOMER"
}
```

Login:

```json
{
  "email": "customer@example.com",
  "password": "example-password"
}
```

Successful auth response shape (token values omitted):

```json
{
  "token": "<access-token>",
  "refreshToken": "<refresh-token>"
}
```

Get events (`GET /api/events`) returns an array:

```json
[
  {
    "id": 12,
    "organizerId": 4,
    "title": "Live music",
    "venue": "Main Hall",
    "location": "Campus",
    "dateTime": "2026-11-07T18:00:00",
    "startTime": "2026-11-07T18:00:00",
    "endTime": "2026-11-07T21:00:00",
    "status": "PUBLISHED",
    "organizerUsername": "organizer@example.com"
  }
]
```

Get one event: `GET /api/events/12` returns the same event object.

Get ticket types (`GET /api/events/12/ticket-types`) returns an array:

```json
[
  {
    "id": 3,
    "event": {
      "id": 12,
      "organizerId": 4,
      "title": "Live music",
      "venue": "Main Hall",
      "location": "Campus",
      "dateTime": "2026-11-07T18:00:00",
      "startTime": "2026-11-07T18:00:00",
      "endTime": "2026-11-07T21:00:00",
      "status": "PUBLISHED",
      "organizerUsername": "organizer@example.com"
    },
    "name": "General admission",
    "price": 25.00,
    "totalQuantity": 300,
    "quantitySold": 42,
    "remaining": 258
  }
]
```

Place an order:

```json
{
  "eventId": 12,
  "items": [
    {
      "ticketTypeId": 3,
      "quantity": 2
    }
  ]
}
```

An order response has this shape:

```json
{
  "id": 20,
  "eventId": 12,
  "totalAmount": 50.00,
  "status": "PENDING",
  "createdAt": "2026-11-07T18:05:00",
  "items": [
    {
      "ticketTypeId": 3,
      "quantity": 2,
      "unitPrice": 25.00
    }
  ]
}
```

`GET /api/orders/my` returns an array of these order objects:

```json
[
  {
    "id": 20,
    "eventId": 12,
    "totalAmount": 50.00,
    "status": "PENDING",
    "createdAt": "2026-11-07T18:05:00",
    "items": [
      {
        "ticketTypeId": 3,
        "quantity": 2,
        "unitPrice": 25.00
      }
    ]
  }
]
```

Start payment checkout:

```json
{
  "amount": 50.00,
  "orderId": 20
}
```

Checkout responds with `{"checkoutUrl":"<provider-checkout-url>"}`. The
callback example is `GET /api/payments/callback?tx_ref=tx-123` and redirects to
`myapp://payment-result?status=success&tx_ref=tx-123`. The return example is
`GET /api/payments/return?tx_ref=tx-123&status=failed` and redirects to
`myapp://payment-result?status=failed&tx_ref=tx-123`.

## Browser and media notes

The backend permits development CORS origins on `localhost` and `127.0.0.1`
with arbitrary ports; native Flutter clients do not use browser CORS. No image
upload endpoint, multipart upload handling, or uploads resource handler exists
in the current project, so `/uploads/**` is only reserved as public in security
configuration; image upload/storage validation is not implemented.
