import { useEffect, useState } from 'react'
import { api } from '../api'
import type { Application, Server, Severity } from '../api'
import { navigate } from '../App'

const SEVERITIES: Severity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export default function NewIncident() {
  const [title, setTitle] = useState('')
  const [severity, setSeverity] = useState<Severity>('MEDIUM')
  const [applicationId, setApplicationId] = useState<number | ''>('')
  const [serverId, setServerId] = useState<number | ''>('')
  const [applications, setApplications] = useState<Application[]>([])
  const [servers, setServers] = useState<Server[]>([])
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    api.getApplications().then(setApplications).catch(() => {})
    api.getServers().then(setServers).catch(() => {})
  }, [])

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    if (!applicationId) return setError('application is required')
    setError(null)
    setSubmitting(true)
    try {
      const incident = await api.createIncident(
        title.trim(),
        severity,
        Number(applicationId),
        serverId ? Number(serverId) : undefined,
      )
      navigate(`/incidents/${incident.id}`)
    } catch (e: unknown) {
      setError((e as Error).message)
      setSubmitting(false)
    }
  }

  return (
    <div className="form-page">
      <div className="form-header">
        <button className="btn-link" onClick={() => navigate('/')}>← back</button>
        <h1>new incident</h1>
      </div>

      {error && <p className="error">{error}</p>}

      <form className="incident-form" onSubmit={submit}>
        <label>
          title
          <input
            required
            value={title}
            onChange={e => setTitle(e.target.value)}
            placeholder="brief description of what broke"
          />
        </label>

        <label>
          severity
          <select value={severity} onChange={e => setSeverity(e.target.value as Severity)}>
            {SEVERITIES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>

        <label>
          application
          <select required value={applicationId} onChange={e => setApplicationId(Number(e.target.value) || '')}>
            <option value="">select application</option>
            {applications.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
          </select>
        </label>

        <label>
          server (optional)
          <select value={serverId} onChange={e => setServerId(Number(e.target.value) || '')}>
            <option value="">none</option>
            {servers.map(s => (
              <option key={s.id} value={s.id}>{s.hostname} ({s.environment})</option>
            ))}
          </select>
        </label>

        <button type="submit" className="btn-primary" disabled={submitting || !title.trim()}>
          open incident
        </button>
      </form>
    </div>
  )
}
