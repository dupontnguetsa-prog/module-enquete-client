import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import '../styles/auth.css'

export default function ForgotPasswordPage() {
    const nav = useNavigate()
    const [email, setEmail] = useState('')
    const [message, setMessage] = useState('')
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    async function submit(event: FormEvent) {
        event.preventDefault()
        setMessage('')
        setError('')
        setBusy(true)
        try {
            const result = await api<{ message: string }>('/api/auth/forgot-password', {
                method: 'POST',
                body: JSON.stringify({ email }),
            })
            setMessage(result.message)
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Impossible de traiter la demande.')
        } finally {
            setBusy(false)
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-brand"><img src="/logo-afriland.png" alt="Afriland First Bank" /></div>
            <div className="auth-visual">
                <img className="auth-background-image" src="/background1.png" alt="" />
                <span className="eyebrow">ACCÈS SÉCURISÉ</span>
                <h1>Retrouvez votre<br /><em>espace de travail.</em></h1>
                <p>Nous vous envoyons un lien temporaire pour définir un nouveau mot de passe.</p>
            </div>
            <div className="auth-card">
                <div className="eyebrow">MOT DE PASSE OUBLIÉ</div>
                <h2>Réinitialiser votre mot de passe</h2>
                <p className="sub">Saisissez l’adresse e-mail associée à votre compte.</p>
                {message ? <div className="form-success">{message}</div> : (
                    <form onSubmit={submit}>
                        <label>Adresse e-mail
                            <input type="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="vous@entreprise.com" autoComplete="email" />
                        </label>
                        {error && <div className="form-error">{error}</div>}
                        <button className="btn btn-primary auth-submit" disabled={busy}>{busy ? 'Envoi...' : 'Envoyer le lien →'}</button>
                    </form>
                )}
                <div className="auth-footer"><button onClick={() => nav('/identification')}>← Retour à la connexion</button></div>
            </div>
        </div>
    )
}
