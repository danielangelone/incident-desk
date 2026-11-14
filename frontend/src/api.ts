const BASE = 'http://localhost:8080/api'

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type Status = 'OPEN' | 'INVESTIGATING' | 'RESOLVED'
export type Environment = 'PROD' | 'STAGING'

export interface Application {
  id: number
  name: string
  ownerTeam: string
}

export interface Server {
  id: number
  hostname: string
  environment: Environment
}

export interface IncidentSummary {
  id: number
  title: string
  severity: Severity
  status: Status
  applicationId: number
  applicationName: string
  serverId: number | null
  serverHostname: string | null
  startedAt: string
  resolvedAt: string | null
}

export interface IncidentNote {
  id: number
  body: string
  createdAt: string
}

export interface IncidentDetail extends IncidentSummary {
  rootCause: string | null
  notes: IncidentNote[]
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Stats {
  openCount: number
  bySeverity: Record<Severity, number>
  meanTimeToResolveHours: number | null
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: res.statusText }))
    throw new Error(body.error ?? res.statusText)
  }
  return res.json()
}

export const api = {
  getApplications: () => request<Application[]>('/applications'),
  createApplication: (name: string, ownerTeam: string) =>
    request<Application>('/applications', { method: 'POST', body: JSON.stringify({ name, ownerTeam }) }),

  getServers: () => request<Server[]>('/servers'),
  createServer: (hostname: string, environment: Environment) =>
    request<Server>('/servers', { method: 'POST', body: JSON.stringify({ hostname, environment }) }),

  getIncidents: (params: { status?: Status; severity?: Severity; applicationId?: number; page?: number }) => {
    const qs = new URLSearchParams()
    if (params.status) qs.set('status', params.status)
    if (params.severity) qs.set('severity', params.severity)
    if (params.applicationId) qs.set('applicationId', String(params.applicationId))
    if (params.page != null) qs.set('page', String(params.page))
    return request<Page<IncidentSummary>>(`/incidents?${qs}`)
  },

  createIncident: (title: string, severity: Severity, applicationId: number, serverId?: number) =>
    request<IncidentDetail>('/incidents', {
      method: 'POST',
      body: JSON.stringify({ title, severity, applicationId, serverId: serverId ?? null }),
    }),

  getIncident: (id: number) => request<IncidentDetail>(`/incidents/${id}`),

  updateStatus: (id: number, status: Status, rootCause?: string) =>
    request<IncidentDetail>(`/incidents/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status, rootCause: rootCause ?? null }),
    }),

  addNote: (id: number, body: string) =>
    request<IncidentNote>(`/incidents/${id}/notes`, { method: 'POST', body: JSON.stringify({ body }) }),

  getStats: () => request<Stats>('/incidents/stats'),
}
