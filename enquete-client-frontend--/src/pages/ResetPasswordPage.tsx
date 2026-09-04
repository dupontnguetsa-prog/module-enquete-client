import { FormEvent, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import '../styles/auth.css'

export default function ResetPasswordPage() {
    const nav = useNavigate()
    const [params] = useSearchParams()
    const token = params.get('token') || ''
    const [password, setPassword] = useState('')
    const [confirmation, setConfirmation] = useState('')
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    async function submit(event: FormEvent) {
        event.preventDefault()
        setError('')
        if (password.length < 8) {
            setError('Le mot de passe doit contenir au moins 8 caractères.')
            return
        }
        if (password !== confirmation) {
            setError('Les mots de passe ne correspondent pas.')
            return
        }
        setBusy(true)
        try {
            await api('/api/auth/reset-password', {
                method: 'POST',
                body: JSON.stringify({ token, password }),
            })
            nav('/identification?reset=success', { replace: true })
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Impossible de réinitialiser le mot de passe.')
        } finally {
            setBusy(false)
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-brand"><img src="/logo-afriland.png" alt="Afriland First Bank" /></div>
            <div className="auth-visual">
                <img className="auth-background-image" src="/background2.png" alt="" />
                <span className="eyebrow">NOUVEAU MOT DE PASSE</span>
                <h1>Un accès<br /><em>à nouveau sécurisé.</em></h1>
                <p>Choisissez un mot de passe personnel d’au moins 8 caractères.</p>
            </div>
            <div className="auth-card">
                <div className="eyebrow">RÉINITIALISATION</div>
                <h2>Choisissez un nouveau mot de passe</h2>
                <p className="sub">Ce lien est valable 30 minutes et ne peut être utilisé qu’une fois.</p>
                <form onSubmit={submit}>
                    <label>Nouveau mot de passe
                        <input type="password" required minLength={8} value={password} onChange={event => setPassword(event.target.value)} autoComplete="new-password" />
                    </label>
                    <label>Confirmer le mot de passe
                        <input type="password" required minLength={8} value={confirmation} onChange={event => setConfirmation(event.target.value)} autoComplete="new-password" />
                    </label>
                    {error && <div className="form-error">{error}</div>}
                    <button className="btn btn-primary auth-submit" disabled={busy || !token}>{busy ? 'Enregistrement...' : 'Enregistrer le mot de passe →'}</button>
                </form>
                <div className="auth-footer"><button onClick={() => nav('/identification')}>← Retour à la connexion</button></div>
            </div>
        </div>
    )
}
