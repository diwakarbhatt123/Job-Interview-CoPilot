# Gateway (Nginx)

This gateway fronts all backend services and enforces authentication for protected routes via
`auth_request`.

## Auth flow

- Public allowlist:
  - `POST /account/auth/register`
  - `POST /account/auth/login`
  - `GET /healthz`
- All other service routes require JWT.
- `auth_request` calls the internal `/userAuth/` location, which proxies to Account Service
  `GET /auth/authenticate`.
- On success, Account Service returns `X-User-Id`; the gateway forwards it downstream as
  `X-User-ID`.

## Files

- `gateway/nginx.conf` — main Nginx config.
- `gateway/conf.d/default.conf` — routes + auth_request wiring.

## Local usage

- Start: `make nginx-up`
- Stop: `make nginx-down`

## Notes

- `Authorization` header is forwarded only to the internal auth subrequest.
- 401 responses can redirect to the login route as configured in Nginx.
