# incident-desk

A small internal tool to log and track production incidents. Teams use it to record what broke, on which server, how bad, what was done, and what the root cause was.

## Running it

```bash
# start Postgres and the API (with seed data)
docker compose up --build

# start the frontend dev server
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/applications | list applications |
| POST | /api/applications | create application |
| GET | /api/servers | list servers |
| POST | /api/servers | create server |
| GET | /api/incidents | list incidents (filters: status, severity, applicationId; paged) |
| POST | /api/incidents | open a new incident |
| GET | /api/incidents/{id} | get incident with notes |
| PATCH | /api/incidents/{id}/status | update status (resolving requires rootCause) |
| POST | /api/incidents/{id}/notes | add a note |
| GET | /api/incidents/stats | open count, count by severity, mean time to resolve |

Status transitions: OPEN → INVESTIGATING → RESOLVED. Resolved incidents cannot be reopened.

## Things I would add next

- Authentication (at minimum a shared token or SSO)
- Webhook notifications when a HIGH/CRITICAL incident is opened
- Incident timeline / audit log of status changes
- Full-text search across title and notes
