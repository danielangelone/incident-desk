import { useEffect, useState } from 'react'
import { api } from '../api'
import type { IncidentSummary, Severity, Status, Stats, Application } from '../api'
import { navigate } from '../App'

const SEVERITIES: Severity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const STATUSES: Status[] = ['OPEN', 'INVESTIGATING', 'RESOLVED']

export default function IncidentList() {
  const [incidents, setIncidents] = useState<IncidentSummary[]>([])
  const [stats, setStats] = useState<Stats | null>(null)
  const [applications, setApplications] = useState<Application[]>([])
  const [filters, setFilters] = useState<{ status?: Status; severity?: Severity; applicationId?: number }>({})
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setPage(0)
  }, [filters])

  useEffect(() => {
    api.getIncidents({ ...filters, page }).then(p => {
      setIncidents(p.content)
      setTotalPages(p.totalPages)
    }).catch(e => setError(e.message))
  }, [filters, page])

  useEffect(() => {
    api.getStats().then(setStats).catch(() => {})
    api.getApplications().then(setApplications).catch(() => {})
  }, [])

  return (
    <div>
      {stats && (
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-value">{stats.openCount}</div>
            <div className="stat-label">open</div>
          </div>
          {SEVERITIES.map(s => (
            <div key={s} className={`stat-card sev-${s.toLowerCase()}`}>
              <div className="stat-value">{stats.bySeverity[s] ?? 0}</div>
              <div className="stat-label">{s.toLowerCase()}</div>
            </div>
          ))}
          <div className="stat-card">
            <div className="stat-value">
              {stats.meanTimeToResolveHours != null ? stats.meanTimeToResolveHours.toFixed(1) + 'h' : '—'}
            </div>
            <div className="stat-label">mttr (90d)</div>
          </div>
        </div>
      )}

      <div className="toolbar">
        <div className="filters">
          <select value={filters.status ?? ''} onChange={e => setFilters(f => ({ ...f, status: (e.target.value as Status) || undefined }))}>
            <option value="">all statuses</option>
            {STATUSES.map(s => <option key={s} value={s}>{s.toLowerCase()}</option>)}
          </select>
          <select value={filters.severity ?? ''} onChange={e => setFilters(f => ({ ...f, severity: (e.target.value as Severity) || undefined }))}>
            <option value="">all severities</option>
            {SEVERITIES.map(s => <option key={s} value={s}>{s.toLowerCase()}</option>)}
          </select>
          <select value={filters.applicationId ?? ''} onChange={e => setFilters(f => ({ ...f, applicationId: e.target.value ? Number(e.target.value) : undefined }))}>
            <option value="">all apps</option>
            {applications.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
          </select>
        </div>
        <button className="btn-primary" onClick={() => navigate('/incidents/new')}>new incident</button>
      </div>

      {error && <p className="error">{error}</p>}

      <table className="incident-table">
        <thead>
          <tr>
            <th>title</th>
            <th>app</th>
            <th>severity</th>
            <th>status</th>
            <th>started</th>
          </tr>
        </thead>
        <tbody>
          {incidents.map(i => (
            <tr key={i.id} className="clickable" onClick={() => navigate(`/incidents/${i.id}`)}>
              <td>{i.title}</td>
              <td>{i.applicationName}</td>
              <td><span className={`badge sev-${i.severity.toLowerCase()}`}>{i.severity}</span></td>
              <td><span className={`badge status-${i.status.toLowerCase()}`}>{i.status}</span></td>
              <td>{new Date(i.startedAt).toLocaleString()}</td>
            </tr>
          ))}
          {incidents.length === 0 && (
            <tr><td colSpan={5} className="empty">no incidents</td></tr>
          )}
        </tbody>
      </table>

      {totalPages > 1 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>prev</button>
          <span>{page + 1} / {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>next</button>
        </div>
      )}
    </div>
  )
}
