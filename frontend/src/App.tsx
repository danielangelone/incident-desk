import { useEffect, useState } from 'react'
import IncidentList from './screens/IncidentList'
import IncidentDetail from './screens/IncidentDetail'
import NewIncident from './screens/NewIncident'

type Route =
  | { screen: 'list' }
  | { screen: 'detail'; id: number }
  | { screen: 'new' }

function parseRoute(): Route {
  const hash = window.location.hash.slice(1) // strip #
  if (hash.startsWith('/incidents/new')) return { screen: 'new' }
  const m = hash.match(/^\/incidents\/(\d+)$/)
  if (m) return { screen: 'detail', id: Number(m[1]) }
  return { screen: 'list' }
}

export function navigate(path: string) {
  window.location.hash = path
}

export default function App() {
  const [route, setRoute] = useState<Route>(parseRoute)

  useEffect(() => {
    const handler = () => setRoute(parseRoute())
    window.addEventListener('hashchange', handler)
    return () => window.removeEventListener('hashchange', handler)
  }, [])

  return (
    <div className="app">
      <header className="topbar">
        <a className="brand" onClick={() => navigate('/')} href="#">incident desk</a>
      </header>
      <main className="content">
        {route.screen === 'list' && <IncidentList />}
        {route.screen === 'detail' && <IncidentDetail id={route.id} />}
        {route.screen === 'new' && <NewIncident />}
      </main>
    </div>
  )
}
