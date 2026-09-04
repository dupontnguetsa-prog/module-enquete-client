import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import type { PageResponse, Survey, SurveyStatus } from '../types'
import { Icon } from '../components/Icons'
import '../styles/workspace-pages.css'

const filters: [string, SurveyStatus | 'Toutes'][] = [
    ['Toutes', 'Toutes'],
    ['Actives', 'Active'],
    ['Brouillons', 'Brouillon'],
    ['Programmées', 'Programmée'],
    ['En pause', 'En pause'],
    ['Terminées', 'Terminée'],
    ['Archivées', 'Archivée'],
]

const dateFormatter = new Intl.DateTimeFormat('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
const timeFormatter = new Intl.DateTimeFormat('fr-FR', { hour: '2-digit', minute: '2-digit' })
const formatDate = (value: string) => dateFormatter.format(new Date(value))
const formatTime = (value: string) => timeFormatter.format(new Date(value))
const createEmbedCode = (survey: Survey) => {
    const surveyUrl = `${window.location.origin}/survey/${survey.publicKey}`
    const title = JSON.stringify(survey.title)
    const url = JSON.stringify(surveyUrl)
    return `<script>
(function () {
  var delay = 3000 + Math.floor(Math.random() * 7001);
  window.setTimeout(function () {
    var overlay = document.createElement('div');
    overlay.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:2147483647;display:flex;align-items:flex-end;justify-content:flex-end;padding:20px;box-sizing:border-box';
    var panel = document.createElement('div');
    panel.style.cssText = 'position:relative;width:min(430px,100%);height:min(700px,calc(100vh - 40px));background:#fff;border-radius:16px;box-shadow:0 12px 40px rgba(0,0,0,.25);overflow:hidden';
    var close = document.createElement('button');
    close.type = 'button';
    close.textContent = '×';
    close.setAttribute('aria-label', 'Fermer le questionnaire');
    close.style.cssText = 'position:absolute;right:8px;top:6px;z-index:1;border:0;background:#fff;border-radius:50%;font-size:26px;line-height:32px;width:34px;height:34px;cursor:pointer';
    var frame = document.createElement('iframe');
    frame.src = ${url};
    frame.title = ${title};
    frame.style.cssText = 'width:100%;height:100%;border:0';
    frame.setAttribute('allow', 'clipboard-write');
    close.onclick = function () { overlay.remove(); };
    overlay.onclick = function (event) { if (event.target === overlay) overlay.remove(); };
    panel.appendChild(close);
    panel.appendChild(frame);
    overlay.appendChild(panel);
    document.body.appendChild(overlay);
  }, delay);
}());
</script>`
}

export default function SurveysPage() {
    const nav = useNavigate()
    const [params] = useSearchParams()
    const [surveys, setSurveys] = useState<Survey[]>([])
    const initialStatus = params.get('status') as SurveyStatus | null
    const [tab, setTab] = useState<SurveyStatus | 'Toutes'>(initialStatus && filters.some(([, value]) => value === initialStatus) ? initialStatus : 'Toutes')
    const [query, setQuery] = useState('')
    const [busy, setBusy] = useState(true)
    const [deletingId, setDeletingId] = useState<number | null>(null)
    const [error, setError] = useState('')
    const [copiedId, setCopiedId] = useState<number | null>(null)
    const [emailSurveyId, setEmailSurveyId] = useState<number | null>(null)
    const [emailRecipient, setEmailRecipient] = useState('')
    const [emailBusy, setEmailBusy] = useState(false)
    const [emailSentId, setEmailSentId] = useState<number | null>(null)
    const [embedId, setEmbedId] = useState<number | null>(null)
    const [page, setPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)

    async function load(selectedTab: SurveyStatus | 'Toutes' = tab) {
        setBusy(true)
        setError('')
        try {
            const params = new URLSearchParams({ page: String(page), size: '20' })
            if (selectedTab !== 'Toutes') params.set('status', selectedTab)
            const loaded = await api<PageResponse<Survey>>(`/api/surveys?${params}`)
            setSurveys(loaded.content)
            setTotalPages(loaded.totalPages)
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Impossible de charger les enquêtes.')
        } finally {
            setBusy(false)
        }
    }

    useEffect(() => { setPage(0) }, [tab])
    useEffect(() => { void load(tab)     }, [tab, page])

    const filtered = useMemo(() => surveys.filter((survey) =>
        (tab === 'Toutes' || survey.status === tab) &&
        survey.title.toLowerCase().includes(query.toLowerCase()),
    ), [surveys, tab, query])

    async function action(path: string) {
        setError('')
        try {
            await api(path, { method: 'POST' })
            await load(tab)
        } catch (err) {
            setError(err instanceof Error ? err.message : 'L’opération n’a pas pu être effectuée.')
        }
    }

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

    async function copyPublicLink(survey: Survey) {
            const link = `${window.location.origin}/survey/${survey.publicKey}`
            try {
                await navigator.clipboard.writeText(link)
                setError('')
                setCopiedId(survey.id)
                window.setTimeout(() => setCopiedId(current => current === survey.id ? null : current), 2200)
            } catch {
                setError(`Lien public : ${link}`)
            }
    }

    async function sharePublicLink(survey: Survey) {
            const link = `${window.location.origin}/survey/${survey.publicKey}`
            if (navigator.share) {
                await navigator.share({ title: survey.title, text: `Répondez à l’enquête « ${survey.title} »`, url: link })
                return
            }
            await copyPublicLink(survey)
    }

    async function sendByEmail(survey: Survey) {
            if (!emailRecipient.trim()) {
                setError('Saisissez au moins une adresse email.')
                return
            }

            const recipients = emailRecipient.split(/[,;\s]+/).filter(Boolean)
            if (recipients.some(email => !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))) {
                setError('Une adresse email est invalide.')
                return
            }
            setEmailBusy(true)
            setError('')
            try {
                await api<void>(`/api/surveys/${survey.id}/send`, { method: 'POST', body: JSON.stringify({ email: emailRecipient.trim() }) })
                setEmailSentId(survey.id)
                setEmailSurveyId(null)
                setEmailRecipient('')
                window.setTimeout(() => setEmailSentId(current => current === survey.id ? null : current), 2500)
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Impossible d’envoyer l’enquête par email.')
            } finally {
                setEmailBusy(false)
            }
    }

    async function copyEmbedCode(survey: Survey) {
        const code = createEmbedCode(survey)
        try {
            await navigator.clipboard.writeText(code)
            setError('')
            setCopiedId(survey.id)
            window.setTimeout(() => setCopiedId(current => current === survey.id ? null : current), 2200)
        } catch {
            setError('La copie est bloquée par le navigateur. Sélectionnez le code pour le copier.')
        }
    }

    return <div className="page">
        <div className="page-head">
            <div><span className="eyebrow">ENQUÊTES</span><h1 className="title">Mes enquêtes</h1><p>Créez, programmez, diffusez et archivez vos campagnes.</p></div>
            <button className="btn btn-primary" onClick={() => nav('/bureau/enquetes/nouvelle')}>＋ Créer une enquête</button>
        </div>
        <div className="toolbar">
            <div className="search-field">⌕<input placeholder="Rechercher…" value={query} onChange={event => setQuery(event.target.value)} /></div>
            <div className="tabs">{filters.map(([label, value]) =>
                <button key={value} className={tab === value ? 'tab active' : 'tab'} onClick={() => setTab(value)}>{label}</button>,
            )}</div>
        </div>
        {error && <div className="form-error" role="alert">{error}</div>}
        {busy ? <div className="empty">Chargement…</div> : filtered.length === 0 ? <div className="empty"><b>Aucune enquête correspondante.</b><span>Modifiez la recherche ou créez une nouvelle enquête.</span></div> :
            <div className="data-table-wrap">
                <table className="data-table survey-data-table">
                    <thead>
                        <tr>
                            <th>Campagne</th>
                            <th>Statut</th>
                            <th>Questions</th>
                            <th>Canaux</th>
                            <th>Modifié</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map(survey => (
                            <tr key={survey.id}>
                                <td>
                                    <button className="campaign-row" onClick={() => nav(`/bureau/enquetes/${survey.id}`)}>
                                        <span className="survey-avatar big">{survey.title.slice(0, 1).toUpperCase()}</span>
                                        <span className="campaign-info">
                                            <b>{survey.title}</b>
                                            <small>{survey.description || 'Aucune description.'}</small>
                                        </span>
                                    </button>
                                </td>
                                <td><span className={`status-badge ${survey.status.toLowerCase().replace(' ', '-')}`}>{survey.status}</span></td>
                                <td>{survey.questions.length}</td>
                                <td>{survey.channels.length}</td>
                                <td>
                                    <div className="muted-stack">
                                        <strong>{formatDate(survey.updatedAt)}</strong>
                                        <small>{formatTime(survey.updatedAt)}</small>
                                    </div>
                                </td>
                                <td className="actions-cell">
                                    <div className="survey-card-actions">
                                        <button className="btn btn-secondary" onClick={() => nav(`/bureau/enquetes/${survey.id}`)}>Ouvrir</button>
                                        {survey.status === 'Active' && <div className="survey-share-actions"><button className="icon-btn share-action" title="Ouvrir le questionnaire public" onClick={() => window.open(`/survey/${survey.publicKey}`, '_blank', 'noopener,noreferrer')}><Icon name="arrow" /></button><button className="icon-btn share-action" title="Copier le lien public" onClick={() => void copyPublicLink(survey)}><Icon name={copiedId === survey.id ? 'check' : 'copy'} /></button><button className="icon-btn share-action share-label" title="Partager le lien public" onClick={() => void sharePublicLink(survey)}><Icon name="share" /><span>Partager</span></button><button className="icon-btn share-action share-label" title="Envoyer par email" onClick={() => { setEmailSurveyId(emailSurveyId === survey.id ? null : survey.id); setError('') }}><Icon name="mail" /><span>Email</span></button><button className="icon-btn share-action share-label" title="Intégrer dans un site" onClick={() => { setEmbedId(embedId === survey.id ? null : survey.id); setError('') }}><Icon name="code" /><span>Intégrer</span></button></div>}
                                        <button className="icon-btn" title="Dupliquer" onClick={() => void action(`/api/surveys/${survey.id}/duplicate`)}>⧉</button>
                                        <button type="button" className="btn btn-danger" disabled={deletingId !== null} aria-busy={deletingId === survey.id} onClick={event => { event.stopPropagation(); void removeSurvey(survey.id) }}>{deletingId === survey.id ? 'Suppression…' : 'Supprimer'}</button>
                                        {(survey.status === 'Active' || survey.status === 'Programmée') && <button className="icon-btn" title="Mettre en pause" onClick={() => void action(`/api/surveys/${survey.id}/pause`)}><Icon name="pause" /></button>}
                                        {survey.status === 'En pause' && <button className="icon-btn" title="Reprendre" onClick={() => void action(`/api/surveys/${survey.id}/resume`)}><Icon name="play" /></button>}
                                        {survey.status !== 'Archivée' && <button className="icon-btn" title="Archiver" onClick={() => void action(`/api/surveys/${survey.id}/archive`)}><Icon name="archive" /></button>}
                                    </div>
                                    {emailSurveyId === survey.id && <div className="email-share-form"><input type="text" value={emailRecipient} onChange={event => setEmailRecipient(event.target.value)} onKeyDown={event => { if (event.key === 'Enter') void sendByEmail(survey) }} placeholder="email1@exemple.com, email2@exemple.com" autoFocus /><button className="btn btn-primary" disabled={emailBusy} onClick={() => void sendByEmail(survey)}>{emailBusy ? 'Envoi…' : 'Envoyer'}</button></div>}
                                    {emailSentId === survey.id && <span className="email-sent"><Icon name="check" /> Email envoyé</span>}
                                    {embedId === survey.id && <div className="embed-share-form"><label>Code à coller dans votre site (apparition automatique après 3 à 10 secondes)</label><textarea readOnly value={createEmbedCode(survey)} onFocus={event => event.currentTarget.select()} /><button className="btn btn-primary" onClick={() => void copyEmbedCode(survey)}><Icon name="copy" /> Copier le code</button></div>}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>}
        {!busy && totalPages > 1 && <div className="pagination"><button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage(value => value - 1)}>Précédente</button><span>Page {page + 1} sur {totalPages}</span><button className="btn btn-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(value => value + 1)}>Suivante</button></div>}
    </div>
}
