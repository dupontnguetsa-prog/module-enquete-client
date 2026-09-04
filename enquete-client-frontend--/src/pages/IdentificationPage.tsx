import { FormEvent, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useTheme } from '../context/ThemeContext'
import { useAuth } from '../context/AuthContext'

export default function IdentificationPage() {
    const nav = useNavigate()
    const { refresh } = useAuth()
    const { theme } = useTheme()

    const [identifier, setIdentifier] = useState('')
    const [password, setPassword] = useState('')
    const [step, setStep] = useState(1)
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)
    const [showPassword, setShowPassword] = useState(false)

    const showcaseCards = [
        { image: '/background1.png', label: 'AFRILAND FIRST BANK' },
        { image: '/background2.png', label: 'NOTRE ENVIRONNEMENT' },
        { image: '/background2.png', label: 'AFRILAND FIRST BANK' },
        { image: '/background1.png', label: 'NOTRE ENVIRONNEMENT' },
    ]

    const continueStep = async (event?: FormEvent<HTMLFormElement>) => {
        event?.preventDefault()
        setError('')

        if (!identifier.trim()) {
            setError('Votre identifiant est obligatoire.')
            return
        }

        if (step === 1) {
            setBusy(true)

            try {
                await api('/api/auth/identifier', {
                    method: 'POST',
                    body: JSON.stringify({
                        identifiant: identifier.trim(),
                    }),
                })

                setStep(2)
            } catch (err) {
                setError((err as Error).message)
            } finally {
                setBusy(false)
            }

            return
        }

        if (!password) {
            setError('Votre mot de passe est obligatoire.')
            return
        }

        setBusy(true)

        try {
            await api('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify({
                    identifiant: identifier.trim(),
                    password,
                }),
            })

            const user = await refresh()

            if (user) {
                nav('/bureau')
            }
        } catch (err) {
            setError((err as Error).message)
        } finally {
            setBusy(false)
        }
    }

    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape' && step === 2 && !busy) {
                setPassword('')
                setError('')
                setStep(1)
            }
        }

        window.addEventListener('keydown', handleKeyDown)

        return () => {
            window.removeEventListener('keydown', handleKeyDown)
        }
    }, [step, busy])

    return (
        <>
            <style>{`
        :root {
          --af-red: #cf0a2c;
          --af-red-dark: #a90825;
          --af-black: #111111;
          --af-ink: #171717;
          --af-muted: #707984;
          --af-soft: #f5f5f5;
          --af-white: #ffffff;
        }

        .identification-premium {
          min-height: 100vh;
          width: 100%;
          overflow-x: hidden;
          background:
            radial-gradient(circle at 0% 30%, rgba(217, 18, 56, 0.08), transparent 30%),
            linear-gradient(180deg, #ffffff 0%, #f8f8f6 100%);
          color: var(--af-ink);
          font-family: Inter, Arial, Helvetica, sans-serif;
        }

        .identification-premium *,
        .identification-premium *::before,
        .identification-premium *::after {
          box-sizing: border-box;
        }

        .identification-premium button,
        .identification-premium input {
          font: inherit;
        }

        .identification-topbar {
          position: relative;
          z-index: 20;
          min-height: 74px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 24px;
          padding: 14px clamp(22px, 5vw, 72px);
          background: rgba(255, 255, 255, 0.90);
          border-bottom: 1px solid rgba(0, 0, 0, 0.07);
          backdrop-filter: blur(18px);
          -webkit-backdrop-filter: blur(18px);
        }

        .identification-brand {
          border: 0;
          padding: 0;
          background: transparent;
          color: inherit;
          display: inline-flex;
          align-items: center;
          gap: 13px;
          cursor: pointer;
          text-align: left;
        }

        .identification-brand img {
          width: 154px;
          height: auto;
          display: block;
        }

        .identification-brand-copy {
          display: grid;
          gap: 3px;
        }

        .identification-brand-copy strong {
          font-size: 13px;
          font-weight: 900;
          letter-spacing: -0.01em;
        }

        .identification-brand-copy span {
          color: #7b8289;
          font-size: 10px;
        }

        .identification-back {
          border: 1px solid rgba(16,16,16,.10);
          border-radius: 10px;
          padding: 10px 14px;
          background: rgba(255,255,255,.82);
          color: #4f565e;
          cursor: pointer;
          font-size: 11px;
          font-weight: 800;
          transition: .25s ease;
        }

        .identification-back:hover {
          transform: translateY(-2px);
          color: var(--af-red);
          border-color: rgba(207,10,44,.25);
        }

        .bank-showcase {
          position: relative;
          height: 280px;
          margin: 0;
          overflow: hidden;
          background: #eceae5;
          box-shadow: 0 20px 60px rgba(17, 17, 17, 0.12);
        }

        .bank-showcase::before {
          content: "";
          position: absolute;
          inset: 0;
          z-index: 2;
          pointer-events: none;
          background: linear-gradient(90deg, rgba(11, 11, 11, 0.18), rgba(11, 11, 11, 0.04) 30%, rgba(11, 11, 11, 0.12));
        }

        .bank-showcase-track {
          position: relative;
          z-index: 1;
          display: flex;
          width: max-content;
          height: 100%;
          animation: showcaseTrack 26s linear infinite;
          will-change: transform;
        }

        .bank-showcase-panel-group {
          display: flex;
          width: max-content;
          height: 100%;
          flex: 0 0 auto;
        }

        .bank-showcase-panel {
          position: relative;
          width: min(52vw, 820px);
          min-width: min(52vw, 820px);
          height: 100%;
          overflow: hidden;
          isolation: isolate;
        }

        .bank-showcase-panel img {
          width: 100%;
          height: 100%;
          display: block;
          object-fit: cover;
          object-position: center;
          transform: scale(1.02);
          filter: saturate(0.92) contrast(1.02) brightness(0.96);
          transition: transform 1.1s ease, filter 1.1s ease;
        }

        .bank-showcase-panel:hover img {
          transform: scale(1.06);
          filter: saturate(1) contrast(1.04) brightness(1);
        }

        .bank-showcase-label {
          position: absolute;
          left: 22px;
          bottom: 18px;
          z-index: 4;
          padding: 8px 12px;
          border: 1px solid rgba(255, 255, 255, 0.20);
          border-radius: 10px;
          background: rgba(10, 10, 10, 0.32);
          color: #fff;
          font-size: 9px;
          font-weight: 800;
          letter-spacing: 0.15em;
          text-transform: uppercase;
          backdrop-filter: blur(8px);
          -webkit-backdrop-filter: blur(8px);
        }

        .showcase-dots {
          position: absolute;
          z-index: 6;
          left: 50%;
          bottom: 14px;
          transform: translateX(-50%);
          display: flex;
          gap: 8px;
        }

        .showcase-dots span {
          width: 7px;
          height: 7px;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.64);
          box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08);
          animation: dotPulse 12s ease-in-out infinite;
        }

        .showcase-dots span:last-child {
          animation-delay: 6s;
        }

        @keyframes showcaseTrack {
          0% {
            transform: translateX(0);
          }

          100% {
            transform: translateX(-50%);
          }
        }

        @keyframes dotPulse {
          0%, 42%, 92%, 100% {
            transform: scale(1);
            opacity: 0.48;
          }

          50%, 84% {
            transform: scale(1.35);
            opacity: 1;
          }
        }

        .identification-main {
          width: min(1240px, calc(100% - 48px));
          margin: 0 auto;
          padding: 68px 0 80px;
          display: grid;
          grid-template-columns: 1fr 500px;
          align-items: center;
          gap: 90px;
        }

        .identification-intro {
          animation: introIn .75s ease both;
        }

        @keyframes introIn {
          from {
            opacity: 0;
            transform: translateX(-24px);
          }

          to {
            opacity: 1;
            transform: translateX(0);
          }
        }

        .identification-intro-label {
          display: inline-flex;
          align-items: center;
          gap: 10px;
          color: var(--af-red);
          font-size: 10px;
          font-weight: 900;
          letter-spacing: .18em;
        }

        .identification-intro-label::before {
          content: "";
          width: 7px;
          height: 7px;
          border-radius: 50%;
          background: var(--af-red);
          box-shadow: 0 0 0 6px rgba(207,10,44,.08);
        }

        .identification-intro h1 {
          max-width: 670px;
          margin: 20px 0 20px;
          font-size: clamp(54px, 6vw, 86px);
          line-height: .94;
          letter-spacing: -.065em;
          font-weight: 900;
        }

        .identification-intro h1 span {
          display: block;
          color: var(--af-red);
        }

        .identification-intro p {
          max-width: 630px;
          margin: 0;
          color: #626a73;
          font-size: 16px;
          line-height: 1.8;
        }

        .identification-proof {
          margin-top: 30px;
          display: flex;
          align-items: center;
          gap: 14px;
        }

        .identification-proof-icon {
          width: 50px;
          height: 50px;
          display: grid;
          place-items: center;
          border-radius: 15px;
          background: #fff0f2;
          color: var(--af-red);
          font-size: 20px;
          font-weight: 900;
          box-shadow: 0 9px 25px rgba(207,10,44,.08);
        }

        .identification-proof-copy {
          display: grid;
          gap: 4px;
        }

        .identification-proof-copy strong {
          font-size: 13px;
          font-weight: 900;
        }

        .identification-proof-copy span {
          color: #777f87;
          font-size: 11px;
        }

        .identification-card {
          position: relative;
          padding: 37px;
          border-radius: 25px;
          background: rgba(255,255,255,.96);
          border: 1px solid rgba(17,17,17,.08);
          box-shadow: 0 30px 80px rgba(17,17,17,.10), 0 0 0 1px rgba(217,18,56,.04);
          animation: cardIn .85s .08s ease both;
        }

        @keyframes cardIn {
          from {
            opacity: 0;
            transform: translateY(25px) scale(.985);
          }

          to {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
        }

        .identification-card::before {
          content: "";
          position: absolute;
          left: 0;
          top: 30px;
          bottom: 30px;
          width: 4px;
          border-radius: 0 5px 5px 0;
          background: linear-gradient(180deg, var(--af-red), #ec3555);
        }

        .identification-card-kicker {
          color: var(--af-red);
          font-size: 10px;
          font-weight: 900;
          letter-spacing: .18em;
        }

        .identification-card h2 {
          margin: 13px 0 8px;
          font-size: 34px;
          line-height: 1;
          letter-spacing: -.035em;
        }

        .identification-card-sub {
          margin: 0 0 27px;
          color: #727b84;
          font-size: 13px;
          line-height: 1.65;
        }

        .identification-form {
          display: grid;
          gap: 17px;
        }

        .identification-field {
          display: grid;
          gap: 8px;
        }

        .identification-field label {
          color: #222;
          font-size: 11px;
          font-weight: 900;
        }

        .identification-input-wrap {
          position: relative;
        }

        .identification-input {
          width: 100%;
          height: 52px;
          padding: 0 15px;
          border: 1px solid rgba(17,17,17,.12);
          border-radius: 12px;
          outline: none;
          background: #ffffff;
          color: #171717;
          font-size: 13px;
          transition: .2s ease;
        }

        .identification-input:focus {
          background: #fff;
          border-color: rgba(217,18,56,.55);
          box-shadow: 0 0 0 4px rgba(217,18,56,.08);
        }

        .identification-input:disabled {
          opacity: .65;
          cursor: not-allowed;
        }

        .identification-password-toggle {
          position: absolute;
          top: 50%;
          right: 7px;
          transform: translateY(-50%);
          min-width: 52px;
          height: 34px;
          padding: 0 9px;
          border: 0;
          border-radius: 8px;
          background: #f0f0f0;
          color: #626970;
          cursor: pointer;
          font-size: 9px;
          font-weight: 900;
        }

        .identification-error {
          display: flex;
          align-items: flex-start;
          gap: 9px;
          padding: 11px 12px;
          border: 1px solid rgba(200,16,46,.16);
          border-radius: 11px;
          background: #fff3f5;
          color: #ad1731;
          font-size: 10px;
          line-height: 1.5;
        }

        .identification-error-icon {
          width: 17px;
          height: 17px;
          flex: 0 0 17px;
          display: grid;
          place-items: center;
          border-radius: 50%;
          background: var(--af-red);
          color: white;
          font-size: 9px;
          font-weight: 900;
        }

        .identification-submit {
          min-height: 52px;
          display: flex;
          justify-content: center;
          align-items: center;
          gap: 13px;
          border: 1px solid var(--af-red);
          border-radius: 12px;
          background: var(--af-red);
          color: #fff;
          cursor: pointer;
          font-size: 13px;
          font-weight: 900;
          box-shadow: 0 13px 28px rgba(207,10,44,.20);
          transition: .22s ease;
        }

        .identification-submit:hover:not(:disabled) {
          transform: translateY(-2px);
          background: var(--af-red-dark);
          border-color: var(--af-red-dark);
        }

        .identification-submit:disabled {
          opacity: .65;
          cursor: not-allowed;
        }

        .identification-spinner {
          width: 16px;
          height: 16px;
          border: 2px solid rgba(255,255,255,.38);
          border-top-color: #fff;
          border-radius: 50%;
          animation: spin .75s linear infinite;
        }

        @keyframes spin {
          to {
            transform: rotate(360deg);
          }
        }

        .identification-step-back {
          justify-self: start;
          border: 0;
          background: transparent;
          color: #787f86;
          padding: 0;
          cursor: pointer;
          font-size: 10px;
          font-weight: 800;
        }

        .identification-step-back:hover {
          color: var(--af-red);
        }

        .identification-divider {
          margin: 23px 0 18px;
          display: grid;
          grid-template-columns: 1fr auto 1fr;
          align-items: center;
          gap: 10px;
        }

        .identification-divider::before,
        .identification-divider::after {
          content: "";
          height: 1px;
          background: #e9e9e9;
        }

        .identification-divider span {
          color: #92979c;
          font-size: 9px;
          font-weight: 900;
          text-transform: uppercase;
        }

        .identification-google {
          width: 100%;
          min-height: 50px;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 10px;
          border: 1px solid #e4e4e4;
          border-radius: 12px;
          background: #fff;
          color: #202020;
          text-decoration: none;
          font-size: 12px;
          font-weight: 850;
          transition: .2s ease;
        }

        .identification-google:hover {
          transform: translateY(-2px);
          border-color: rgba(0,0,0,.16);
          box-shadow: 0 12px 24px rgba(0,0,0,.07);
        }

        .google-icon {
          font-size: 18px;
          font-weight: 950;
          color: #4285f4;
        }

        .identification-register {
          margin-top: 20px;
          padding-top: 18px;
          border-top: 1px solid #ededed;
          display: flex;
          flex-wrap: wrap;
          gap: 5px;
          justify-content: center;
          color: #858b92;
          font-size: 9px;
          text-align: center;
        }

        .identification-register button {
          border: 0;
          padding: 0;
          background: transparent;
          color: var(--af-red);
          cursor: pointer;
          font-size: 9px;
          font-weight: 900;
        }

        .identification-register button:hover {
          text-decoration: underline;
        }

        .identification-footer {
          width: min(1240px, calc(100% - 48px));
          margin: 0 auto;
          padding: 24px 0 32px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 15px;
          color: #8a9096;
          font-size: 9px;
        }

        .identification-footer strong {
          color: var(--af-red);
        }

        /* DARK MODE */

        .identification-premium.dark {
          background: #0c0c0c;
          color: #fff;
        }

        .identification-premium.dark .identification-topbar {
          background: rgba(12,12,12,.92);
          border-color: #242424;
        }

        .identification-premium.dark .identification-brand-copy span,
        .identification-premium.dark .identification-back {
          color: #9ea5ad;
        }

        .identification-premium.dark .identification-back {
          background: #141414;
          border-color: #2d2d2d;
        }

        .identification-premium.dark .identification-main {
          background: transparent;
        }

        .identification-premium.dark .identification-intro p,
        .identification-premium.dark .identification-proof-copy span {
          color: #aeb4bb;
        }

        .identification-premium.dark .identification-card {
          background: rgba(20,20,20,.96);
          border-color: #2a2a2a;
        }

        .identification-premium.dark .identification-field label,
        .identification-premium.dark .identification-step-back {
          color: #d8dce0;
        }

        .identification-premium.dark .identification-input {
          background: #151515;
          border-color: #363636;
          color: #fff;
        }

        .identification-premium.dark .identification-input:focus {
          background: #181818;
        }

        .identification-premium.dark .identification-password-toggle {
          background: #282828;
          color: #c5cbd0;
        }

        .identification-premium.dark .identification-divider::before,
        .identification-premium.dark .identification-divider::after,
        .identification-premium.dark .identification-register {
          border-color: #2b2b2b;
        }

        .identification-premium.dark .identification-google {
          background: #151515;
          color: #fff;
          border-color: #313131;
        }

        .identification-premium.dark .identification-footer {
          border-color: #262626;
          color: #868d95;
        }

        @media (max-width: 980px) {
          .identification-main {
            grid-template-columns: 1fr;
            gap: 46px;
            width: min(720px, calc(100% - 40px));
          }

          .identification-intro {
            text-align: center;
          }

          .identification-intro h1,
          .identification-intro p {
            margin-left: auto;
            margin-right: auto;
          }

          .identification-proof {
            justify-content: center;
          }

          .identification-card {
            width: min(520px, 100%);
            margin: 0 auto;
          }

          .bank-showcase {
            height: 220px;
          }
        }

        @media (max-width: 650px) {
          .identification-topbar {
            min-height: 66px;
            padding: 11px 16px;
          }

          .identification-brand-copy {
            display: none;
          }

          .identification-brand img {
            width: 132px;
          }

          .identification-back {
            padding: 9px 11px;
            font-size: 9px;
          }

          .bank-showcase {
            height: 175px;
          }

          .bank-showcase-pair {
            padding: 8px;
            gap: 8px;
          }

          .bank-showcase-card {
            border-radius: 12px;
          }

          .bank-showcase-label {
            left: 8px;
            bottom: 8px;
            font-size: 7px;
            padding: 6px 7px;
          }

          .identification-main {
            width: min(calc(100% - 30px), 520px);
            padding: 52px 0 55px;
            gap: 38px;
          }

          .identification-intro h1 {
            font-size: 49px;
          }

          .identification-intro p {
            font-size: 14px;
          }

          .identification-card {
            padding: 28px 23px;
            border-radius: 21px;
          }

          .identification-card h2 {
            font-size: 30px;
          }

          .identification-footer {
            width: min(calc(100% - 30px), 520px);
            flex-direction: column;
            align-items: flex-start;
          }
        }

        @media (prefers-reduced-motion: reduce) {
          .bank-showcase-track,
          .bank-showcase-card img,
          .showcase-dots span,
          .identification-intro,
          .identification-card,
          .identification-spinner {
            animation: none !important;
          }
        }
      `}</style>

            <div className={`identification-premium ${theme === 'dark' ? 'dark' : ''}`}>
                <header className="identification-topbar">
                    <button
                        className="identification-brand"
                        type="button"
                        onClick={() => nav('/')}
                        aria-label="Retour à l'accueil"
                    >
                        <img src="/logo-afriland.png" alt="Afriland First Bank" />

                        <span className="identification-brand-copy">
              <strong>Afriland First Bank</strong>
              <span>Enquêtes &amp; Feedback Client</span>
            </span>
                    </button>

                    <button
                        className="identification-back"
                        type="button"
                        onClick={() => nav('/')}
                    >
                        ← Retour à l'accueil
                    </button>
                </header>

                <section className="bank-showcase" aria-label="Présentation Afriland First Bank">
                    <div className="bank-showcase-track" aria-live="polite">
                        {[0, 1].map((groupIndex) => (
                            <div key={groupIndex} className="bank-showcase-panel-group" aria-hidden={groupIndex > 0}>
                                {showcaseCards.map((card, index) => (
                                    <div key={`${groupIndex}-${index}`} className="bank-showcase-panel">
                                        <img src={card.image} alt={card.label} />
                                        <span className="bank-showcase-label">{card.label}</span>
                                    </div>
                                ))}
                            </div>
                        ))}
                    </div>

                    <div className="showcase-dots" aria-hidden="true">
                        <span />
                        <span />
                    </div>
                </section>

                <main className="identification-main">
                    <section className="identification-intro">
            <span className="identification-intro-label">
              ESPACE UTILISATEUR
            </span>

                        <h1>
                            Bienvenue dans
                            <span>votre espace.</span>
                        </h1>

                        <p>
                            Identifiez-vous pour accéder à vos enquêtes, vos réponses et vos
                            analyses dans un environnement pensé pour votre travail.
                        </p>

                        <div className="identification-proof">
                            <div className="identification-proof-icon">✓</div>

                            <div className="identification-proof-copy">
                                <strong>Environnement sécurisé</strong>
                                <span>Votre session reste liée à votre compte.</span>
                            </div>
                        </div>
                    </section>

                    <section className="identification-card">
            <span className="identification-card-kicker">
              IDENTIFICATION
            </span>

                        <h2>Connectez-vous</h2>

                        <p className="identification-card-sub">
                            {step === 1
                                ? 'Entrez votre identifiant pour continuer.'
                                : 'Entrez votre mot de passe pour ouvrir votre espace.'}
                        </p>

                        <form
                            className="identification-form"
                            onSubmit={continueStep}
                            noValidate
                        >
                            <div className="identification-field">
                                <label htmlFor="identifier">
                                    Identifiant
                                </label>

                                <div className="identification-input-wrap">
                                    <input
                                        className="identification-input"
                                        id="identifier"
                                        name="identifier"
                                        type="text"
                                        value={identifier}
                                        onChange={(event) => {
                                            setIdentifier(event.target.value)
                                            setError('')
                                        }}
                                        placeholder="Entrez votre identifiant"
                                        autoComplete="username"
                                        spellCheck={false}
                                        autoFocus
                                        disabled={step === 2 || busy}
                                    />
                                </div>
                            </div>

                            {step === 2 && (
                                <>
                                    <div className="identification-field">
                                        <label htmlFor="password">
                                            Mot de passe
                                        </label>

                                        <div className="identification-input-wrap">
                                            <input
                                                className="identification-input"
                                                id="password"
                                                name="password"
                                                type={showPassword ? 'text' : 'password'}
                                                value={password}
                                                onChange={(event) => {
                                                    setPassword(event.target.value)
                                                    setError('')
                                                }}
                                                placeholder="Entrez votre mot de passe"
                                                autoComplete="current-password"
                                                autoFocus
                                                disabled={busy}
                                            />

                                            <button
                                                className="identification-password-toggle"
                                                type="button"
                                                onClick={() => setShowPassword((value) => !value)}
                                                disabled={busy}
                                            >
                                                {showPassword ? 'Masquer' : 'Voir'}
                                            </button>
                                        </div>
                                    </div>

                                    <button
                                        className="identification-step-back"
                                        type="button"
                                        onClick={() => {
                                            if (!busy) {
                                                setPassword('')
                                                setError('')
                                                setStep(1)
                                            }
                                        }}
                                        disabled={busy}
                                    >
                                        ← Modifier l'identifiant
                                    </button>
                                    <button className="identification-forgot" type="button" onClick={() => nav('/mot-de-passe-oublie')} disabled={busy}>
                                        Mot de passe oublié ?
                                    </button>
                                </>
                            )}

                            {error && (
                                <div className="identification-error" role="alert">
                                    <span className="identification-error-icon">!</span>
                                    <span>{error}</span>
                                </div>
                            )}

                            <button
                                className="identification-submit"
                                type="submit"
                                disabled={busy}
                            >
                                {busy ? (
                                    <>
                                        <span className="identification-spinner" />
                                        <span>{step === 1 ? 'Vérification...' : 'Connexion...'}</span>
                                    </>
                                ) : (
                                    <>
                    <span>
                      {step === 1 ? 'Continuer' : 'Ouvrir mon espace'}
                    </span>
                                        <span>→</span>
                                    </>
                                )}
                            </button>
                        </form>

                        <div className="identification-divider">
                            <span>ou</span>
                        </div>

                        <a
                            className="identification-google"
                            href="/oauth2/authorization/google"
                        >
                            <span className="google-icon">G</span>
                            <span>Continuer avec Google</span>
                        </a>

                        <div className="identification-register">
                            <span>Vous n’avez pas encore de compte ?</span>

                            <button
                                type="button"
                                onClick={() => nav('/inscription')}
                            >
                                Créer un compte →
                            </button>
                        </div>
                    </section>
                </main>

                <footer className="identification-footer">
                    <strong>Afriland First Bank</strong>
                    <span>Plateforme d'enquêtes clients</span>
                    <span>© 2026</span>
                </footer>
            </div>
        </>
    )
}
