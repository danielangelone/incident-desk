import { useEffect, useState } from 'react'
import { api } from '../api'
import type { IncidentDetail as Detail, Status } from '../api'
import { navigate } from '../App'

interface Props { id: number }

export default function IncidentDetail({ id }: Props) {
  const [incident, setIncident] = useState<Detail | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [noteBody, setNoteBody] = useState('')
  const [rootCause, setRootCause] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    api.getIncident(id).then(setIncident).catch(e => setError(e.message))
  }, [id])

  async function changeStatus(next: Status) {
    if (!incident) return
    setError(null)
    try {
      const updated = await api.updateStatus(id, next, next === 'RESOLVED' ? rootCause : undefined)
      setIncident(updated)
    } catch (e: unknown) {
      setError((e as Error).message)
    }
  }

  async function submitNote(e: React.FormEvent) {
    e.preventDefault()
    if (!noteBody.trim()) return
    setSubmitting(true)
    try {
      const note = await api.addNote(id, noteBody.trim())
      setIncident(prev => prev ? { ...prev, notes: [...prev.notes, note] } : prev)
      setNoteBody('')
    } catch (e: unknown) {
      setError((e as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  if (error && !incident) return <p className="error">{error}</p>
  if (!incident) return <p className="loading">loading...</p>

  const canResolve = incident.status !== 'RESOLVED'
  const canInvestigate = incident.status === 'OPEN'

  return (
    <div className="detail">
      <div className="detail-header">
        <button className="btn-link" onClick={() => navigate('/')}>← back</button>
        <h1>{incident.title}</h1>
      </div>

      <div className="detail-meta">
        <span className={`badge sev-${incident.severity.toLowerCase()}`}>{incident.severity}</span>
        <span className={`badge status-${incident.status.toLowerCase()}`}>{incident.status}</span>
        <span>{incident.applicationName}</span>
        {incident.serverHostname && <span>{incident.serverHostname}</span>}
        <span>started {new Date(incident.startedAt).toLocaleString()}</span>
        {incident.resolvedAt && <span>resolved {new Date(incident.resolvedAt).toLocaleString()}</span>}
      </div>

      {incident.rootCause && (
        <div className="root-cause">
          <strong>root cause:</strong> {incident.rootCause}
        </div>
      )}

      {error && <p className="error">{error}</p>}

      {incident.status !== 'RESOLVED' && (
        <div className="actions">
          {canInvestigate && (
            <button className="btn-secondary" onClick={() => changeStatus('INVESTIGATING')}>
              mark investigating
            </button>
          )}
          {canResolve && (
            <div className="resolve-form">
              <input
                className="resolve-input"
                placeholder="root cause (required to resolve)"
                value={rootCause}
                onChange={e => setRootCause(e.target.value)}
              />
              <button
                className="btn-resolve"
                disabled={!rootCause.trim()}
                onClick={() => changeStatus('RESOLVED')}
              >
                resolve
              </button>
            </div>
          )}
        </div>
      )}

      <section className="notes-section">
        <h2>notes</h2>
        {incident.notes.length === 0 && <p className="empty">no notes yet</p>}
        {incident.notes.map(n => (
          <div key={n.id} className="note">
            <p>{n.body}</p>
            <small>{new Date(n.createdAt).toLocaleString()}</small>
          </div>
        ))}
        <form className="add-note" onSubmit={submitNote}>
          <textarea
            rows={3}
            placeholder="add a note..."
            value={noteBody}
            onChange={e => setNoteBody(e.target.value)}
          />
          <button type="submit" className="btn-primary" disabled={submitting || !noteBody.trim()}>
            add note
          </button>
        </form>
      </section>
    </div>
  )
}
