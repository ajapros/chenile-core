# Chenile Admin UI

This is a standalone React frontend for inspecting a running Chenile system.

## What it does

- prompts for a Chenile base URL such as `localhost:8000`
- calls `GET /info` to list deployed services
- calls `GET /service-info/{service}` to show the full `ChenileServiceDefinition`
- calls `GET /health-check/{service}` when the service has a health checker
- when a workflow-oriented service also exposes its generated companion `*StateEntityInfoService`, renders workflow PNG diagrams and testcase diagrams from the corresponding `/{service}/info/...` endpoints
- when the target monolith hosts `serviceregistryService`, calls `GET /serviceregistry` to show the ecosystem-wide service catalog aggregated from all delegates
- probes common Swagger/OpenAPI endpoints and shows a direct docs link when available

The UI is intentionally thin. It reads Chenile info endpoints and, when available, the central service registry endpoint.
When running through Vite in local development, it uses an internal proxy endpoint to avoid browser CORS failures against a Chenile server on another origin such as `http://localhost:8000`.

## Run locally

```bash
npm install
npm run dev
```

By default Vite runs on `http://localhost:4173`.

## Build

```bash
npm run build
```

## Notes

- The app expects the Chenile HTTP endpoints to be accessible from the browser.
- In local development, the built-in Vite proxy handles cross-origin calls.
- In production without that proxy, the backend must allow CORS or the UI must be served from the same origin.
- The frontend accepts both raw JSON payloads and Chenile `GenericResponse`-wrapped payloads.
- Workflow tools only appear when the target deployment actually exposes the companion workflow-info service endpoints. If those endpoints are absent, the UI hides the feature entirely.
- Ecosystem mode appears only when the target monolith is hosting the central service registry.
