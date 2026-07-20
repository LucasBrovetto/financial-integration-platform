# POS Terminal

React application that simulates the point-of-sale entry point for the Financial Integration Platform.

## Development

```bash
pnpm install
pnpm dev
```

The development server is available at `http://localhost:5173` by default.
Requests under `/api` are proxied to the transaction service at `http://localhost:8080`.

## Quality Checks

```bash
pnpm lint
pnpm build
```

Start the transaction service before testing transaction creation or lookup. Recent activity is stored locally in the browser; the backend currently exposes creation and lookup by transaction ID, not a collection endpoint.
