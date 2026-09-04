import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api'
import type { Analytics, PageResponse, Survey, SurveyQuestion } from '../types'
import '../styles/workspace-pages.css'
import { subscribeRealtime } from '../utils/realtime'

export default function AnalyticsPage() {
    const [params, setParams] = useSearchParams()
    const [surveys, setSurveys] = useState<Survey[]>([])
    const [selected, setSelected] = useState<number | undefined>(Number(params.get('survey')) || undefined)
    const [days, setDays] = useState(Number(params.get('days')) || 14)
    const [audienceField, setAudienceField] = useState('')
    const [audienceValue, setAudienceValue] = useState('')
    const [data, setData] = useState<Analytics | null>(null)
    const [loaded, setLoaded] = useState(false)
    const [error, setError] = useState('')

    useEffect(() => {
        void api<PageResponse<Survey>>('/api/surveys?size=100')
            .then(result => {
                setSurveys(result.content)
                if (!selected && result.content[0]) setSelected(result.content[0].id)
            })
            .catch(cause => setError(cause instanceof Error ? cause.message : 'Impossible de charger les enquêtes.'))
            .finally(() => setLoaded(true))
    }, [])

    useEffect(() => {
        if (!selected) return
        const query = new URLSearchParams({ survey: String(selected), days: String(days) })
        if (audienceField && audienceValue) {
            query.set('audienceField', audienceField)
            query.set('audienceValue', audienceValue)
        }
        setParams(query)
        setData(null)
        setError('')
        const load = () => void api<Analytics>(`/api/surveys/${selected}/analytics?${query}`).then(setData).catch(cause => setError(cause instanceof Error ? cause.message : 'Impossible de charger les résultats.'))
        load()
        return subscribeRealtime('/api/realtime/analytics','analytics',load)
    }, [selected, days, audienceField, audienceValue])

    const survey = surveys.find(item => item.id === selected)
    return <div className="page">
        <div className="page-head">
            <div><span className="eyebrow">MESURE</span><h1 className="title">Analytics</h1><p>Comprenez les réponses clients par question, segment et canal.</p></div>
            <div className="analytics-filters">
                <select className="select-inline" value={selected || ''} onChange={event => setSelected(Number(event.target.value) || undefined)}>{surveys.map(item => <option key={item.id} value={item.id}>{item.title}</option>)}</select>
                <select className="select-inline" value={days} onChange={event => setDays(Number(event.target.value))}><option value="7">7 derniers jours</option><option value="14">14 derniers jours</option><option value="30">30 derniers jours</option><option value="0">Toute la période</option></select>
                <select className="select-inline" value={audienceField} onChange={event => { setAudienceField(event.target.value); setAudienceValue('') }}><option value="">Toutes les audiences</option><option value="customerType">Type de client</option><option value="agency">Agence</option><option value="city">Ville</option><option value="relationshipStatus">Statut relation</option><option value="product">Produit</option></select>
                {audienceField && <input className="select-inline" placeholder="Valeur audience" value={audienceValue} onChange={event => setAudienceValue(event.target.value)} />}
            </div>
        </div>
        {error ? <div className="form-error" role="alert">{error}</div> : loaded && !survey ? <div className="empty"><b>Aucune enquête à analyser.</b><span>Créez ou publiez une enquête pour voir ses résultats.</span></div> : !data || !survey ? <div className="empty">Chargement des résultats…</div> : <>
            <div className="metric-grid">
                <Metric label="Démarrées" value={data.views} help="ouvertures de l’enquête" /><Metric label="Taux de réponse" value={`${data.responseRate}%`} help="réponses / ouvertures" accent />
                <Metric label="Complétées" value={data.responses} help={`${data.completionRate}% des démarrées`} accent />
                <Metric label="Abandonnées" value={data.abandoned} help="ouvertures sans réponse" />
                <Metric label="Questions" value={data.questions} help={`${data.logicRules} règles de logique`} />
            </div>
            <div className="analytics-dashboard-grid">
                <BreakdownCard title="Performance par segment" subtitle="Réponses clients identifiées." rows={data.segmentAnalytics || []} />
                <ChannelCard rows={data.channelAnalytics || []} />
            </div>
            <section className="content-card"><div className="card-head"><div><h2>Résultats par question</h2><p>Les réponses réelles sont regroupées selon le format de chaque question.</p></div></div><div className="data-table-wrap"><table className="data-table"><thead><tr><th>Question</th><th>Réponses</th><th>Tendance</th></tr></thead><tbody>{data.questionAnalytics.map(result => <tr key={result.index} className={result.answered > 0 ? 'selected-row' : ''}><td><QuestionResult result={result} question={survey.questions[result.index]} /></td><td>{result.answered}</td><td><span className="status-badge active">{result.average !== null ? `Moy. ${result.average}` : 'En cours'}</span></td></tr>)}</tbody></table></div></section>
            <section className="content-card"><div className="card-head"><div><h2>Réponses dans le temps</h2><p>{days === 0 ? 'Toute la période' : `${days} derniers jours`}</p></div></div><div className="chart"><div className="chart-bars">{data.series.map(point => <div key={point.date} className="chart-bar-wrap"><div className="chart-bar" style={{ height: `${Math.max(5, point.responses * 18 + 8)}px` }} /><small>{point.date.slice(5)}</small></div>)}</div></div></section>
        </>}
    </div>
}

function Metric({ label, value, help, accent = false }: { label: string; value: string | number; help: string; accent?: boolean }) { return <div className={`metric-card${accent ? ' accent' : ''}`}><span>{label}</span><strong>{value}</strong><small>{help}</small></div> }
function BreakdownCard({ title, subtitle, rows }: { title: string; subtitle: string; rows: { segment: string; responses: number; rate: number }[] }) { const max = Math.max(...rows.map(row => row.responses), 1); return <section className="content-card"><div className="card-head"><div><h2>{title}</h2><p>{subtitle}</p></div></div>{rows.length ? <div className="analytics-breakdown">{rows.map(row => <div className="breakdown-row" key={row.segment}><div><b>{row.segment}</b><small>{row.responses} réponse{row.responses > 1 ? 's' : ''} · {row.rate}%</small></div><div className="breakdown-track"><i style={{ width: `${row.responses / max * 100}%` }} /></div></div>)}</div> : <div className="empty">Aucun segment disponible.</div>}</section> }
function ChannelCard({ rows }: { rows: { channel: string; views: number; responses: number; rate: number }[] }) { return <section className="content-card"><div className="card-head"><div><h2>Performance par canal</h2><p>Comparaison des ouvertures et réponses.</p></div></div>{rows.length ? <div className="analytics-breakdown">{rows.map(row => <div className="breakdown-row" key={row.channel}><div><b>{row.channel}</b><small>{row.responses}/{row.views} réponses · {row.rate}%</small></div><div className="breakdown-track"><i style={{ width: `${Math.min(100, row.rate)}%` }} /></div></div>)}</div> : <div className="empty">Aucun canal mesuré.</div>}</section> }
function QuestionResult({ result, question }: { result: Analytics['questionAnalytics'][number]; question: SurveyQuestion | undefined }) { const entries = Object.entries(result.distribution); const total = result.answered; const type = question?.type; return <article className="analytics-question"><div className="analytics-question-head"><span className="question-number">{String(result.index + 1).padStart(2, '0')}</span><div><b>{result.question}</b><small>{labelForType(type)} · {result.answered} réponse{result.answered > 1 ? 's' : ''}{result.average !== null ? ` · moyenne ${result.average}` : ''}</small></div></div><div className="analytics-distribution">{entries.slice(0, 12).map(([key, value]) => <div className="distribution-row" key={key}><span>{key}</span><div><i style={{ width: `${total ? value / total * 100 : 0}%` }} /></div><b>{value} <small>({total ? Math.round(value / total * 100) : 0}%)</small></b></div>)}</div></article> }
function labelForType(type?: string) { return ({ NPS: 'NPS', SCALE: 'Échelle', STARS: 'Étoiles', SINGLE_CHOICE: 'Choix unique', MULTIPLE_CHOICE: 'Choix multiple', YES_NO: 'Oui / Non', SHORT_TEXT: 'Texte court', LONG_TEXT: 'Texte long' } as Record<string, string>)[type || ''] || 'Question' }
