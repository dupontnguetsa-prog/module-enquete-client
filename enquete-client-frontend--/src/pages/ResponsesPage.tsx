import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import type { PageResponse, Survey } from '../types'
import '../styles/workspace-pages.css'
import { subscribeRealtime } from '../utils/realtime'

type ResponseView = {
    id: number
    anonymous: boolean
    customerName: string | null
    answers: string
    completedAt: string
    triggeredAction: string | null
}

type ResponsePage = {
    content: ResponseView[]
    number: number
    totalPages: number
    totalElements: number
}

export default function ResponsesPage() {
    const nav = useNavigate()
    const [params] = useSearchParams()
    const [surveys, setSurveys] = useState<Survey[]>([])
    const [selected, setSelected] = useState<number | undefined>(
        Number(params.get('survey')) || undefined,
    )
    const [responses, setResponses] = useState<ResponseView[]>([])
    const [page, setPage] = useState(0)
    const [pageInfo, setPageInfo] = useState<ResponsePage | null>(null)
    const [error, setError] = useState('')

    useEffect(() => {
        void api<PageResponse<Survey>>('/api/surveys?size=100')
            .then((result) => {
                setSurveys(result.content)
                if (!selected && result.content[0]) setSelected(result.content[0].id)
            })
            .catch((cause) =>
                setError(
                    cause instanceof Error
                        ? cause.message
                        : 'Impossible de charger les enquêtes.',
                ),
            )
    }, [selected])

    useEffect(() => {
        setPage(0)
    }, [selected])

    useEffect(() => {
        if (!selected) return

        const load = () => {
            void api<ResponsePage>(
                `/api/surveys/${selected}/responses?page=${page}&size=25`,
            )
                .then((result) => {
                    setResponses(result.content)
                    setPageInfo(result)
                })
                .catch((cause) =>
                    setError(
                        cause instanceof Error
                            ? cause.message
                            : 'Impossible de charger les réponses.',
                    ),
                )
        }

        load()
        return subscribeRealtime('/api/realtime/analytics','analytics',load)
    }, [selected, page])

    async function exportCsv() {
        if (!selected) return
        try {
            const response = await fetch(
                `/api/surveys/${selected}/responses/export`,
                { credentials: 'include' },
            )
            if (!response.ok) throw new Error('Export impossible.')
            const blob = await response.blob()
            const url = URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = url
            link.download = `enquete-${selected}-reponses.csv`
            link.click()
            URL.revokeObjectURL(url)
        } catch (cause) {
            setError(
                cause instanceof Error ? cause.message : 'Export impossible.',
            )
        }
    }

    const survey = surveys.find((item) => item.id === selected)
    const total = pageInfo?.totalElements ?? 0

    return (
        <div className="page">
            <div className="page-head">
                <div>
                    <span className="eyebrow">COLLECTE</span>
                    <h1 className="title">Réponses</h1>
                    <p>Consultez les réponses reçues par enquête.</p>
                </div>
                <div>
                    <button
                        className="btn btn-secondary"
                        onClick={() => nav('/bureau/enquetes')}
                    >
                        Mes enquêtes
                    </button>
                    {selected && (
                        <button
                            className="btn btn-primary"
                            onClick={() => void exportCsv()}
                        >
                            Exporter CSV
                        </button>
                    )}
                </div>
            </div>

            {error && (
                <div className="form-error" role="alert">
                    {error}
                </div>
            )}

            <div className="split-layout">
                <aside className="content-card">
                    <div className="card-head">
                        <div>
                            <h2>Campagnes</h2>
                            <p>
                                {surveys.length} enquête
                                {surveys.length > 1 ? 's' : ''}
                            </p>
                        </div>
                    </div>
                    <div className="data-table-wrap">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Campagne</th>
                                    <th>Statut</th>
                                </tr>
                            </thead>
                            <tbody>
                                {surveys.map((item) => (
                                    <tr
                                        key={item.id}
                                        className={
                                            selected === item.id
                                                ? 'selected-row'
                                                : ''
                                        }
                                    >
                                        <td>
                                            <button
                                                className="campaign-row"
                                                onClick={() =>
                                                    setSelected(item.id)
                                                }
                                            >
                                                <span className="survey-avatar">
                                                    {item.title
                                                        .slice(0, 1)
                                                        .toUpperCase()}
                                                </span>
                                                <span className="campaign-info">
                                                    <b>{item.title}</b>
                                                    <small>
                                                        {item.questions.length}{' '}
                                                        questions
                                                    </small>
                                                </span>
                                            </button>
                                        </td>
                                        <td>
                                            <span
                                                className={`status-badge ${item.status
                                                    .toLowerCase()
                                                    .replace(' ', '-')}`}
                                            >
                                                {item.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </aside>

                <section className="content-card">
                    <div className="card-head">
                        <div>
                            <h2>
                                {survey?.title ||
                                    'Sélectionnez une enquête'}
                            </h2>
                            <p>
                                {total} réponse{total > 1 ? 's' : ''}
                            </p>
                        </div>
                    </div>

                    {responses.length === 0 ? (
                        <div className="empty">
                            <b>Aucune réponse reçue.</b>
                            <span>
                                Les réponses apparaîtront ici dès qu’un client
                                répondra à l’enquête.
                            </span>
                        </div>
                    ) : (
                        <>
                            <div className="response-list">
                                {responses.map((response) => (
                                    <article
                                        key={response.id}
                                        className="response-card"
                                    >
                                        <div className="response-head">
                                            <div>
                                                <b>
                                                    {response.anonymous
                                                        ? 'Réponse anonyme'
                                                        : response.customerName ||
                                                          'Client'}
                                                </b>
                                                <small>
                                                    {new Date(
                                                        response.completedAt,
                                                    ).toLocaleString('fr-FR')}
                                                </small>
                                            </div>
                                            <span>
                                                # {response.id}
                                            </span>
                                        </div>
                                        <pre>
                                            {formatAnswers(
                                                response.answers,
                                                survey,
                                            )}
                                        </pre>
                                    </article>
                                ))}
                            </div>
                            <div className="page-actions">
                                <button
                                    className="btn btn-secondary"
                                    disabled={page === 0}
                                    onClick={() =>
                                        setPage((value) => value - 1)
                                    }
                                >
                                    ← Précédentes
                                </button>
                                <span>
                                    {page + 1} / {pageInfo?.totalPages || 1}
                                </span>
                                <button
                                    className="btn btn-secondary"
                                    disabled={
                                        !pageInfo ||
                                        page + 1 >= pageInfo.totalPages
                                    }
                                    onClick={() =>
                                        setPage((value) => value + 1)
                                    }
                                >
                                    Suivantes →
                                </button>
                            </div>
                        </>
                    )}
                </section>
            </div>
        </div>
    )
}

function formatAnswers(raw: string, survey?: Survey) {
    try {
        const answers = JSON.parse(raw) as Record<string, unknown>
        return Object.entries(answers)
            .map(([key, value]) => {
                const question = survey?.questions[Number(key)]
                return `${question?.title || `Question ${Number(key) + 1}`}\n${String(value)}`
            })
            .join('\n\n')
    } catch {
        return raw
    }
}
