import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../context/AuthContext'
import type { PageResponse, Survey } from '../types'
import '../styles/workspace-pages.css'

export default function DashboardPage() {
    const nav = useNavigate()
    const { user } = useAuth()
    const [surveys, setSurveys] = useState<Survey[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [deletingId, setDeletingId] = useState<number | null>(null)

    useEffect(() => {
        void api<PageResponse<Survey>>('/api/surveys?size=100')
            .then(result => setSurveys(result.content))
            .catch(err => setError(err instanceof Error ? err.message : 'Impossible de charger les enquêtes.'))
            .finally(() => setLoading(false))
    }, [])

    async function removeSurvey(id: number) {
        setError('')
        setDeletingId(id)
        try {
            await api<void>(`/api/surveys/${id}`, { method: 'DELETE' })
            setSurveys(current => current.filter(survey => survey.id !== id))
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Impossible de supprimer cette enquête.')
        } finally {
            setDeletingId(null)
        }
    }

    const active = surveys.filter(s => s.status === 'Active').length
    const drafts = surveys.filter(s => s.status === 'Brouillon').length

    return <div className="page">
        <div className="page-head"><div><span className="eyebrow">TABLEAU DE BORD</span><h1 className="title">Bonjour {user?.identifiant || 'Utilisateur'} 👋</h1><p>Un aperçu clair de vos campagnes et de leur activité.</p></div><button className="btn btn-primary" onClick={() => nav('/bureau/enquetes/nouvelle')}>＋ Nouvelle enquête</button></div>
        <div className="metric-grid"><div className="metric-card"><span>Enquêtes</span><strong>{surveys.length}</strong><small>dans votre espace</small></div><div className="metric-card"><span>Actives</span><strong>{active}</strong><small>en cours de diffusion</small></div><div className="metric-card"><span>Programmées</span><strong>{surveys.filter(s => s.status === 'Programmée').length}</strong><small>prêtes à démarrer</small></div><div className="metric-card accent"><span>Brouillons</span><strong>{drafts}</strong><small>à finaliser</small></div></div>
        <section className="content-card"><div className="card-head"><div><h2>Vos dernières enquêtes</h2><p>Accédez directement à vos campagnes.</p></div><button className="btn btn-secondary" onClick={() => nav('/bureau/enquetes')}>Voir toutes</button></div>
            {error && <div className="form-error" role="alert">{error}</div>}
            {loading ? <div className="empty">Chargement…</div> : surveys.length === 0 ? <div className="empty"><b>Aucune enquête pour le moment.</b><span>Créez votre première campagne pour commencer à recueillir des retours.</span><button className="btn btn-primary" onClick={() => nav('/bureau/enquetes/nouvelle')}>Créer une enquête</button></div> :
                <div className="survey-table">
                    <table>
                        <thead>
                            <tr>
                                <th>Campagne</th>
                                <th>Statut</th>
                                <th>Questions</th>
                                <th>Canaux</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {surveys.slice(0, 6).map(s => (
                                <tr key={s.id}>
                                    <td>
                                        <button className="survey-row-main" onClick={() => nav(`/bureau/enquetes/${s.id}`)}>
                                            <span className="survey-avatar">{s.title.slice(0, 1).toUpperCase()}</span>
                                            <span className="survey-info"><b>{s.title}</b><small>{s.questions.length} question{s.questions.length > 1 ? 's' : ''}</small></span>
                                        </button>
                                    </td>
                                    <td><span className={`status-badge ${s.status.toLowerCase().replace(' ', '-')}`}>{s.status}</span></td>
                                    <td>{s.questions.length}</td>
                                    <td>{s.channels.length}</td>
                                    <td><button type="button" className="btn btn-danger row-delete" disabled={deletingId !== null} aria-busy={deletingId === s.id} onClick={() => void removeSurvey(s.id)}>{deletingId === s.id ? 'Suppression…' : 'Supprimer'}</button></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>}
        </section>
    </div>
}
