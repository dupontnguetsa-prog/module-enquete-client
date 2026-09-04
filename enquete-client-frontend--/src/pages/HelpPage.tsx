import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTheme } from '../context/ThemeContext'

type Faq = {
    id: string
    category: string
    question: string
    answer: string
}

const faqs: Faq[] = [
    {
        id: 'faq-premiere-enquete',
        category: 'Premiers pas',
        question: 'Comment créer ma première enquête ?',
        answer:
            'Depuis votre espace, ouvrez « Enquêtes » puis « Nouvelle enquête ». Renseignez les informations, ajoutez les questions, configurez la logique, l’aperçu LIVE, le déclenchement, l’audience et la diffusion avant de publier.',
    },
    {
        id: 'faq-modifier-enquete',
        category: 'Enquêtes',
        question: 'Comment modifier une enquête existante ?',
        answer:
            'Dans « Enquêtes », choisissez l’enquête concernée et ouvrez son espace de modification. Les brouillons peuvent être ajustés avant leur publication.',
    },
    {
        id: 'faq-declencheurs',
        category: 'Déclenchement',
        question: 'Quels déclencheurs puis-je utiliser ?',
        answer:
            'Une enquête peut être déclenchée par un événement, une date ou une heure, une visite de page, le temps passé sur une page, une entrée d’audience, un lancement manuel ou un événement API externe.',
    },
    {
        id: 'faq-audience',
        category: 'Audience',
        question: 'Puis-je cibler seulement certains clients ?',
        answer:
            'Oui. Vous pouvez utiliser une audience fixe ou dynamique et appliquer des filtres selon les informations disponibles dans votre espace.',
    },
    {
        id: 'faq-reponses',
        category: 'Réponses',
        question: 'Où consulter les réponses reçues ?',
        answer:
            'La rubrique « Réponses » rassemble les retours reçus. Vous pouvez consulter les réponses individuelles et suivre l’évolution de vos enquêtes.',
    },
    {
        id: 'faq-analyses',
        category: 'Analyses',
        question: 'À quoi servent les analyses ?',
        answer:
            'Les analyses permettent de suivre les réponses, le taux de réponse, la complétion, les consultations, les abandons, le temps moyen et les tendances par question.',
    },
    {
        id: 'faq-securite',
        category: 'Compte & sécurité',
        question: 'Comment protéger mon espace de travail ?',
        answer:
            'L’accès à la plateforme repose sur une session authentifiée. Vos enquêtes et leurs résultats restent liés à votre espace de travail.',
    },
]

const categories = [
    {
        number: '01',
        icon: '✦',
        title: 'Premiers pas',
        description: 'Découvrez rapidement votre espace et lancez votre première enquête.',
    },
    {
        number: '02',
        icon: '◈',
        title: 'Créer une enquête',
        description: 'Construisez les questions, la logique et le parcours de diffusion.',
    },
    {
        number: '03',
        icon: '⌁',
        title: 'Ciblage & diffusion',
        description: 'Choisissez les bons clients et le bon canal au bon moment.',
    },
    {
        number: '04',
        icon: '↗',
        title: 'Réponses & analyses',
        description: 'Transformez les retours collectés en indicateurs utiles.',
    },
]

const guides = [
    {
        number: '01',
        title: 'Créer une enquête',
        text: 'Informations → Questions → Logique → Aperçu LIVE → Déclenchement → Audience → Diffusion.',
    },
    {
        number: '02',
        title: 'Choisir un déclencheur',
        text: 'Adaptez le moment d’envoi au contexte client : événement, date, visite, API ou manuel.',
    },
    {
        number: '03',
        title: 'Lire les résultats',
        text: 'Passez des réponses individuelles aux indicateurs pour identifier les tendances importantes.',
    },
]

export default function HelpPage() {
    const navigate = useNavigate()
    const { theme, toggle } = useTheme()

    const [search, setSearch] = useState('')
    const [submittedSearch, setSubmittedSearch] = useState('')
    const [openFaq, setOpenFaq] = useState<string | null>('faq-premiere-enquete')
    const inputRef = useRef<HTMLInputElement | null>(null)

    const filteredFaqs = useMemo(() => {
        const term = submittedSearch.trim().toLowerCase()

        if (!term) {
            return faqs
        }

        return faqs.filter((faq) =>
            `${faq.question} ${faq.answer} ${faq.category}`
                .toLowerCase()
                .includes(term),
        )
    }, [submittedSearch])

    useEffect(() => {
        const handleKeyboard = (event: KeyboardEvent) => {
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
                event.preventDefault()
                inputRef.current?.focus()
                inputRef.current?.select()
            }
        }

        window.addEventListener('keydown', handleKeyboard)

        return () => {
            window.removeEventListener('keydown', handleKeyboard)
        }
    }, [])

    const runSearch = () => {
        const value = search.trim()
        setSubmittedSearch(value)

        window.requestAnimationFrame(() => {
            document
                .getElementById('help-results')
                ?.scrollIntoView({ behavior: 'smooth', block: 'start' })
        })
    }

    const clearSearch = () => {
        setSearch('')
        setSubmittedSearch('')
        inputRef.current?.focus()
    }

    const openResult = (faq: Faq) => {
        setOpenFaq(faq.id)

        window.setTimeout(() => {
            document
                .getElementById(faq.id)
                ?.scrollIntoView({ behavior: 'smooth', block: 'center' })
        }, 60)
    }

    return (
        <>
            <style>{`
        .help-page {
          --help-red: #c8102e;
          --help-red-dark: #a60b25;
          --help-black: #101010;
          --help-ink: #171717;
          --help-muted: #70767e;
          --help-line: rgba(16,16,16,.09);
          --help-soft: #f6f6f4;
          min-height: 100vh;
          overflow-x: hidden;
          background: #fff;
          color: var(--help-ink);
          font-family: Inter, Arial, Helvetica, sans-serif;
        }

        .help-page *,
        .help-page *::before,
        .help-page *::after {
          box-sizing: border-box;
        }

        .help-page button,
        .help-page input {
          font: inherit;
        }

        .help-nav {
          position: sticky;
          top: 0;
          z-index: 50;
          min-height: 78px;
          display: flex;
          align-items: center;
          gap: 26px;
          padding: 14px 5vw;
          background: rgba(255,255,255,.92);
          border-bottom: 1px solid rgba(16,16,16,.08);
          backdrop-filter: blur(18px);
          -webkit-backdrop-filter: blur(18px);
        }

        .help-brand {
          display: flex;
          align-items: center;
          gap: 13px;
          padding: 0;
          border: 0;
          background: transparent;
          color: inherit;
          cursor: pointer;
          text-align: left;
        }

        .help-brand img {
          display: block;
          width: 148px;
          height: auto;
        }

        .help-brand-text {
          display: grid;
          gap: 3px;
        }

        .help-brand-text strong {
          font-size: 13px;
          font-weight: 900;
          line-height: 1.1;
        }

        .help-brand-text small {
          font-size: 10px;
          color: #7d838a;
        }

        .help-nav-links {
          display: flex;
          align-items: center;
          gap: 4px;
          margin-left: auto;
        }

        .help-nav-link {
          padding: 10px 12px;
          border: 0;
          border-radius: 9px;
          background: transparent;
          color: #666d75;
          cursor: pointer;
          font-size: 12px;
          font-weight: 800;
          transition: .2s ease;
        }

        .help-nav-link:hover,
        .help-nav-link.active {
          color: var(--help-red);
          background: rgba(200,16,46,.06);
        }

        .help-nav-actions {
          display: flex;
          align-items: center;
          gap: 9px;
        }

        .help-theme-button,
        .help-login-button {
          border-radius: 10px;
          cursor: pointer;
          font-weight: 800;
          transition: .2s ease;
        }

        .help-theme-button {
          width: 42px;
          height: 42px;
          border: 1px solid rgba(16,16,16,.10);
          background: #fff;
          color: #171717;
        }

        .help-login-button {
          padding: 11px 15px;
          border: 1px solid var(--help-black);
          background: var(--help-black);
          color: #fff;
          font-size: 11px;
        }

        .help-theme-button:hover,
        .help-login-button:hover {
          transform: translateY(-2px);
        }

        .help-hero {
          position: relative;
          min-height: 650px;
          display: flex;
          align-items: center;
          overflow: hidden;
          isolation: isolate;
        }

        .help-hero-image {
          position: absolute;
          inset: 0;
          width: 100%;
          height: 100%;
          display: block;
          object-fit: cover;
          object-position: center center;
          z-index: 0;
        }

        .help-hero-overlay {
          position: absolute;
          inset: 0;
          z-index: 1;
          background:
            linear-gradient(
              90deg,
              rgba(255,255,255,.96) 0%,
              rgba(255,255,255,.89) 24%,
              rgba(255,255,255,.65) 49%,
              rgba(255,255,255,.23) 76%,
              rgba(255,255,255,.04) 100%
            );
        }

        .help-hero-ring {
          position: absolute;
          border: 1px solid rgba(200,16,46,.19);
          border-radius: 50%;
          z-index: 2;
          pointer-events: none;
        }

        .help-hero-ring.one {
          width: 520px;
          height: 520px;
          right: -170px;
          top: -240px;
        }

        .help-hero-ring.two {
          width: 250px;
          height: 250px;
          right: 10%;
          bottom: -135px;
          border-color: rgba(255,255,255,.48);
        }

        .help-hero-content {
          position: relative;
          z-index: 5;
          width: min(1180px, calc(100% - 48px));
          margin: 0 auto;
          padding: 96px 0 110px;
        }

        .help-eyebrow {
          display: inline-flex;
          align-items: center;
          gap: 10px;
          color: var(--help-red);
          font-size: 10px;
          font-weight: 900;
          letter-spacing: .18em;
        }

        .help-eyebrow span {
          width: 7px;
          height: 7px;
          border-radius: 50%;
          background: var(--help-red);
          box-shadow: 0 0 0 6px rgba(200,16,46,.08);
        }

        .help-hero h1 {
          max-width: 920px;
          margin: 21px 0 20px;
          font-size: clamp(48px, 6.25vw, 88px);
          line-height: .97;
          letter-spacing: -.06em;
          font-weight: 900;
        }

        .help-hero h1 span {
          display: block;
          color: var(--help-red);
        }

        .help-hero-description {
          max-width: 670px;
          margin: 0;
          color: #5e6670;
          font-size: 16px;
          line-height: 1.8;
        }

        .help-search {
          position: relative;
          width: min(760px, 100%);
          margin-top: 30px;
        }

        .help-search-shell {
          min-height: 68px;
          display: flex;
          align-items: center;
          gap: 11px;
          padding: 0 10px 0 18px;
          background: rgba(255,255,255,.97);
          border: 1px solid rgba(0,0,0,.10);
          border-radius: 17px;
          box-shadow: 0 26px 70px rgba(0,0,0,.14);
          transition: .25s ease;
        }

        .help-search-shell:focus-within {
          border-color: rgba(200,16,46,.35);
          box-shadow: 0 30px 78px rgba(200,16,46,.15);
        }

        .help-search-icon {
          flex: 0 0 auto;
          color: var(--help-red);
          font-size: 27px;
          line-height: 1;
        }

        .help-search-input {
          flex: 1;
          min-width: 0;
          height: 56px;
          border: 0;
          outline: 0;
          background: transparent;
          color: #141414;
          font-size: 13px;
        }

        .help-search-input::placeholder {
          color: #92989f;
        }

        .help-search-clear {
          flex: 0 0 auto;
          width: 30px;
          height: 30px;
          border: 0;
          border-radius: 8px;
          background: transparent;
          color: #8d9399;
          cursor: pointer;
          font-size: 22px;
        }

        .help-search-clear:hover {
          color: var(--help-red);
          background: rgba(200,16,46,.06);
        }

        .help-search-key {
          flex: 0 0 auto;
          padding: 7px 9px;
          border: 1px solid #e8e8e8;
          border-radius: 7px;
          color: #858b91;
          font-size: 8px;
          font-weight: 900;
          letter-spacing: .06em;
        }

        .help-search-submit {
          flex: 0 0 auto;
          min-height: 46px;
          padding: 0 19px;
          border: 1px solid var(--help-red);
          border-radius: 11px;
          background: var(--help-red);
          color: #fff;
          cursor: pointer;
          font-size: 11px;
          font-weight: 900;
          box-shadow: 0 9px 22px rgba(200,16,46,.20);
          transition: .2s ease;
        }

        .help-search-submit:hover {
          background: var(--help-red-dark);
          border-color: var(--help-red-dark);
          transform: translateY(-2px);
        }

        .help-search-meta {
          margin-top: 10px;
          display: flex;
          align-items: center;
          gap: 14px;
          color: #6b727a;
          font-size: 11px;
          font-weight: 700;
        }

        .help-search-results {
          margin-top: 12px;
          padding: 10px;
          border: 1px solid rgba(0,0,0,.09);
          border-radius: 15px;
          background: rgba(255,255,255,.97);
          box-shadow: 0 22px 55px rgba(0,0,0,.12);
        }

        .help-search-results-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 6px 8px 10px;
          color: #727880;
          font-size: 10px;
          font-weight: 800;
        }

        .help-result-item {
          width: 100%;
          display: grid;
          grid-template-columns: 34px 1fr 20px;
          align-items: center;
          gap: 10px;
          padding: 12px;
          border: 0;
          border-radius: 11px;
          background: transparent;
          color: inherit;
          cursor: pointer;
          text-align: left;
          transition: .2s ease;
        }

        .help-result-item:hover {
          background: #faf0f2;
        }

        .help-result-number {
          width: 28px;
          height: 28px;
          display: grid;
          place-items: center;
          border-radius: 8px;
          background: #fff0f2;
          color: var(--help-red);
          font-size: 9px;
          font-weight: 900;
        }

        .help-result-copy {
          display: grid;
          gap: 3px;
        }

        .help-result-copy small {
          color: #999fa5;
          font-size: 8px;
          font-weight: 900;
          letter-spacing: .10em;
          text-transform: uppercase;
        }

        .help-result-copy strong {
          color: #1a1a1a;
          font-size: 12px;
          font-weight: 850;
        }

        .help-result-arrow {
          color: var(--help-red);
          font-size: 17px;
        }

        .help-search-empty {
          padding: 22px 12px;
          color: #737a81;
          font-size: 11px;
          line-height: 1.6;
          text-align: center;
        }

        .help-trust {
          display: flex;
          flex-wrap: wrap;
          gap: 22px;
          margin-top: 18px;
          color: #697179;
          font-size: 11px;
          font-weight: 800;
        }

        .help-section {
          padding: 104px 0;
        }

        .help-white {
          background: #fff;
        }

        .help-soft {
          background: #f5f5f3;
        }

        .help-dark {
          background: #0b0b0b;
          color: #fff;
        }

        .help-container {
          width: min(1180px, calc(100% - 48px));
          margin: 0 auto;
        }

        .help-heading {
          display: grid;
          grid-template-columns: 1.2fr .8fr;
          align-items: end;
          gap: 60px;
        }

        .help-kicker {
          color: var(--help-red);
          font-size: 10px;
          font-weight: 900;
          letter-spacing: .18em;
        }

        .help-heading h2,
        .help-guide-heading h2,
        .help-support h2 {
          margin: 15px 0 0;
          font-size: clamp(39px, 4.9vw, 66px);
          line-height: .98;
          letter-spacing: -.055em;
          font-weight: 900;
        }

        .help-heading h2 span,
        .help-guide-heading h2 span,
        .help-support h2 span {
          color: var(--help-red);
        }

        .help-heading p {
          margin: 0;
          color: #767c83;
          font-size: 13px;
          line-height: 1.8;
        }

        .help-card-grid {
          display: grid;
          grid-template-columns: repeat(4,1fr);
          gap: 14px;
          margin-top: 48px;
        }

        .help-card {
          position: relative;
          min-height: 285px;
          padding: 25px;
          overflow: hidden;
          border: 1px solid rgba(0,0,0,.09);
          border-radius: 20px;
          background: #fff;
          transition: .3s ease;
        }

        .help-card:hover {
          transform: translateY(-8px);
          box-shadow: 0 28px 62px rgba(0,0,0,.10);
          border-color: rgba(200,16,46,.22);
        }

        .help-card.dark {
          background: #121212;
          color: #fff;
          border-color: #292929;
        }

        .help-card.soft-card {
          background: #f4f4f1;
        }

        .help-card.green-card {
          background: #f2f9f5;
        }

        .help-card-top {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .help-card-icon {
          width: 46px;
          height: 46px;
          display: grid;
          place-items: center;
          border-radius: 14px;
          background: #fff0f2;
          color: var(--help-red);
          font-size: 17px;
          font-weight: 900;
        }

        .help-card.dark .help-card-icon {
          background: #242424;
        }

        .help-card.soft-card .help-card-icon {
          background: #fff;
          color: #424242;
        }

        .help-card.green-card .help-card-icon {
          background: #e5f5eb;
          color: #21784a;
        }

        .help-card-arrow {
          color: var(--help-red);
          font-size: 20px;
        }

        .help-card-number {
          position: absolute;
          right: 18px;
          bottom: 13px;
          color: rgba(0,0,0,.055);
          font-size: 52px;
          font-weight: 900;
        }

        .help-card.dark .help-card-number {
          color: rgba(255,255,255,.07);
        }

        .help-card h3 {
          margin: 58px 0 10px;
          font-size: 21px;
          font-weight: 900;
          letter-spacing: -.02em;
        }

        .help-card p {
          max-width: 230px;
          margin: 0;
          color: #727981;
          font-size: 12px;
          line-height: 1.8;
        }

        .help-card.dark p {
          color: #9ca3aa;
        }

        .help-guide-layout {
          display: grid;
          grid-template-columns: .82fr 1.18fr;
          gap: 80px;
          align-items: center;
        }

        .help-guide-heading > p {
          max-width: 530px;
          margin: 23px 0 0;
          color: #a9aeb4;
          font-size: 13px;
          line-height: 1.8;
        }

        .help-primary-button {
          margin-top: 27px;
          padding: 14px 17px;
          border: 0;
          border-radius: 10px;
          background: #fff;
          color: #111;
          cursor: pointer;
          font-size: 11px;
          font-weight: 900;
          transition: .2s ease;
        }

        .help-primary-button:hover {
          transform: translateY(-3px);
        }

        .help-guide-list {
          display: grid;
          gap: 10px;
        }

        .help-guide {
          display: grid;
          grid-template-columns: 45px 1fr 25px;
          align-items: center;
          gap: 15px;
          padding: 18px;
          border: 1px solid #292929;
          border-radius: 15px;
          background: #111;
        }

        .help-guide-number {
          width: 42px;
          height: 42px;
          display: grid;
          place-items: center;
          border-radius: 12px;
          background: #1d1d1d;
          color: #ff6179;
          font-size: 10px;
          font-weight: 900;
        }

        .help-guide h3 {
          margin: 0;
          color: #fff;
          font-size: 14px;
          font-weight: 900;
        }

        .help-guide p {
          margin: 5px 0 0;
          color: #949aa1;
          font-size: 11px;
          line-height: 1.7;
        }

        .help-guide-arrow {
          color: #ff6179;
          font-size: 18px;
        }

        .help-faq-list {
          margin-top: 52px;
          border-top: 1px solid #d9d9d6;
        }

        .help-faq-row {
          border-bottom: 1px solid #d9d9d6;
        }

        .help-faq-button {
          width: 100%;
          min-height: 94px;
          display: grid;
          grid-template-columns: 48px 1fr 40px;
          align-items: center;
          gap: 15px;
          padding: 0;
          border: 0;
          background: transparent;
          color: inherit;
          cursor: pointer;
          text-align: left;
        }

        .help-faq-index {
          color: var(--help-red);
          font-size: 10px;
          font-weight: 900;
        }

        .help-faq-copy {
          display: grid;
          gap: 4px;
        }

        .help-faq-copy small {
          color: #93999f;
          font-size: 9px;
          font-weight: 900;
          letter-spacing: .12em;
          text-transform: uppercase;
        }

        .help-faq-copy strong {
          color: #171717;
          font-size: 15px;
          font-weight: 850;
        }

        .help-page.dark .help-faq-copy strong {
          color: #fff;
        }

        .help-faq-button:hover .help-faq-copy strong {
          color: var(--help-red);
        }

        .help-faq-toggle {
          width: 34px;
          height: 34px;
          display: grid;
          place-items: center;
          border: 1px solid #d7d7d7;
          border-radius: 50%;
          color: var(--help-red);
          font-size: 18px;
          transition: .2s ease;
        }

        .help-faq-row.open .help-faq-toggle {
          background: var(--help-red);
          border-color: var(--help-red);
          color: #fff;
        }

        .help-faq-answer {
          max-height: 0;
          overflow: hidden;
          opacity: 0;
          transition: max-height .35s ease, opacity .25s ease;
        }

        .help-faq-row.open .help-faq-answer {
          max-height: 260px;
          opacity: 1;
        }

        .help-faq-answer p {
          max-width: 820px;
          margin: 0;
          padding: 0 62px 29px;
          color: #71787f;
          font-size: 12px;
          line-height: 1.9;
        }

        .help-page.dark .help-faq-answer p {
          color: #a5abb2;
        }

        .help-empty {
          padding: 60px 20px;
          border: 1px dashed #d5d5d1;
          border-radius: 17px;
          background: #fff;
          text-align: center;
        }

        .help-empty-icon {
          color: var(--help-red);
          font-size: 35px;
        }

        .help-empty h3 {
          margin: 10px 0 0;
          font-size: 20px;
        }

        .help-empty p {
          margin: 7px 0 0;
          color: #8b9197;
          font-size: 12px;
        }

        .help-support {
          position: relative;
          min-height: 380px;
          overflow: hidden;
          padding: 60px;
          border-radius: 26px;
          background: linear-gradient(125deg, #101010 0%, #220b10 100%);
          color: #fff;
          box-shadow: 0 28px 75px rgba(0,0,0,.14);
        }

        .help-support-glow {
          position: absolute;
          width: 430px;
          height: 430px;
          right: -160px;
          top: -230px;
          border-radius: 50%;
          background: rgba(200,16,46,.36);
          filter: blur(48px);
        }

        .help-support-content {
          position: relative;
          z-index: 2;
          max-width: 680px;
        }

        .help-support > .help-kicker {
          position: relative;
          z-index: 2;
        }

        .help-support p {
          max-width: 590px;
          margin: 21px 0 0;
          color: #aeb4ba;
          font-size: 12px;
          line-height: 1.85;
        }

        .help-support-actions {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          margin-top: 28px;
        }

        .help-support-actions button {
          padding: 13px 17px;
          border-radius: 10px;
          cursor: pointer;
          font-size: 11px;
          font-weight: 900;
          transition: .2s ease;
        }

        .help-support-primary {
          border: 0;
          background: #fff;
          color: #111;
        }

        .help-support-secondary {
          border: 1px solid rgba(255,255,255,.16);
          background: transparent;
          color: #fff;
        }

        .help-support-actions button:hover {
          transform: translateY(-3px);
        }

        .help-support-mark {
          position: absolute;
          right: 90px;
          top: 50%;
          width: 190px;
          height: 190px;
          display: grid;
          place-items: center;
          transform: translateY(-50%);
          border: 1px solid rgba(255,255,255,.14);
          border-radius: 50%;
          color: rgba(255,97,121,.68);
          font-size: 104px;
          font-weight: 900;
        }

        .help-footer {
          width: min(1180px, calc(100% - 48px));
          margin: 0 auto;
          padding: 30px 0 38px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 20px;
          border-top: 1px solid #e9e9e7;
          color: #858b91;
          font-size: 10px;
        }

        .help-footer-brand {
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .help-footer-brand img {
          width: 120px;
          height: auto;
        }

        .help-footer-brand-text {
          display: grid;
          gap: 3px;
        }

        .help-footer-brand-text strong {
          color: var(--help-red);
          font-size: 11px;
        }

        .help-footer-brand-text span {
          color: #8a9096;
        }

        /* DARK */
        .help-page.dark {
          background: #0e0e0e;
          color: #fff;
        }

        .help-page.dark .help-nav {
          background: rgba(14,14,14,.92);
          border-color: #292929;
        }

        .help-page.dark .help-brand-text small,
        .help-page.dark .help-nav-link {
          color: #aab0b7;
        }

        .help-page.dark .help-nav-link.active,
        .help-page.dark .help-nav-link:hover {
          color: #ff6179;
          background: rgba(200,16,46,.08);
        }

        .help-page.dark .help-theme-button {
          background: #161616;
          color: #fff;
          border-color: #2d2d2d;
        }

        .help-page.dark .help-hero-image {
          filter: brightness(.62) saturate(.82) contrast(.96);
        }

        .help-page.dark .help-hero-overlay {
          background:
            linear-gradient(
              90deg,
              rgba(10,10,10,.93) 0%,
              rgba(10,10,10,.81) 28%,
              rgba(10,10,10,.50) 57%,
              rgba(10,10,10,.15) 82%,
              rgba(10,10,10,.03) 100%
            );
        }

        .help-page.dark .help-hero-description,
        .help-page.dark .help-trust {
          color: #c1c6cb;
        }

        .help-page.dark .help-search-shell,
        .help-page.dark .help-search-results {
          background: rgba(20,20,20,.96);
          border-color: #333;
        }

        .help-page.dark .help-search-input {
          color: #fff;
        }

        .help-page.dark .help-search-key {
          border-color: #383838;
          color: #a1a7ae;
        }

        .help-page.dark .help-search-results-header,
        .help-page.dark .help-result-copy small {
          color: #8f969d;
        }

        .help-page.dark .help-result-item:hover {
          background: #1b0e11;
        }

        .help-page.dark .help-result-copy strong {
          color: #fff;
        }

        .help-page.dark .help-section.help-white {
          background: #0e0e0e;
        }

        .help-page.dark .help-section.help-soft {
          background: #151515;
        }

        .help-page.dark .help-heading p {
          color: #9ea5ac;
        }

        .help-page.dark .help-card {
          background: #151515;
          color: #fff;
          border-color: #2a2a2a;
        }

        .help-page.dark .help-card.soft-card {
          background: #171717;
        }

        .help-page.dark .help-card.green-card {
          background: #101914;
        }

        .help-page.dark .help-card p {
          color: #9ea4ab;
        }

        .help-page.dark .help-faq-list,
        .help-page.dark .help-faq-row {
          border-color: #353535;
        }

        .help-page.dark .help-empty {
          background: #111;
          border-color: #333;
        }

        .help-page.dark .help-empty p {
          color: #969ca3;
        }

        .help-page.dark .help-footer {
          border-color: #2d2d2d;
          color: #8f969d;
        }

        @media (max-width: 1050px) {
          .help-card-grid {
            grid-template-columns: repeat(2,1fr);
          }

          .help-heading,
          .help-guide-layout {
            grid-template-columns: 1fr;
            gap: 30px;
          }

          .help-support-mark {
            right: 28px;
          }
        }

        @media (max-width: 760px) {
          .help-nav {
            padding: 12px 18px;
          }

          .help-brand-text,
          .help-nav-links {
            display: none;
          }

          .help-nav-actions {
            margin-left: auto;
          }

          .help-hero {
            min-height: 680px;
          }

          .help-hero-overlay {
            background:
              linear-gradient(
                180deg,
                rgba(255,255,255,.95) 0%,
                rgba(255,255,255,.81) 43%,
                rgba(255,255,255,.39) 74%,
                rgba(255,255,255,.08) 100%
              );
          }

          .help-hero-content {
            width: min(calc(100% - 36px), 650px);
            padding: 78px 0 92px;
          }

          .help-hero h1 {
            font-size: 51px;
          }

          .help-search-shell {
            flex-wrap: wrap;
            padding: 10px 12px;
          }

          .help-search-input {
            height: 44px;
          }

          .help-search-submit {
            width: 100%;
          }

          .help-search-key {
            display: none;
          }

          .help-section {
            padding: 78px 0;
          }

          .help-container,
          .help-footer {
            width: min(calc(100% - 36px), 650px);
          }

          .help-card-grid {
            grid-template-columns: 1fr;
          }

          .help-card {
            min-height: 220px;
          }

          .help-support {
            padding: 43px 28px;
          }

          .help-support-mark {
            display: none;
          }

          .help-footer {
            flex-direction: column;
            align-items: flex-start;
          }
        }

        @media (max-width: 520px) {
          .help-login-button {
            display: none;
          }

          .help-brand img {
            width: 128px;
          }

          .help-hero h1 {
            font-size: 45px;
          }

          .help-hero-description {
            font-size: 14px;
          }

          .help-search-meta {
            flex-wrap: wrap;
          }

          .help-result-item {
            grid-template-columns: 30px 1fr 16px;
          }

          .help-faq-button {
            grid-template-columns: 32px 1fr 35px;
            gap: 9px;
          }

          .help-faq-copy strong {
            font-size: 13px;
          }

          .help-faq-answer p {
            padding: 0 41px 24px;
          }

          .help-guide {
            grid-template-columns: 40px 1fr 15px;
            gap: 10px;
          }
        }
      `}</style>

            <div className={`help-page ${theme === 'dark' ? 'dark' : ''}`}>
                <header className="help-nav">
                    <button className="help-brand" type="button" onClick={() => navigate('/')}>
                        <img src="/logo-afriland.png" alt="Afriland First Bank" />

                        <span className="help-brand-text">
              <strong>Afriland First Bank</strong>
              <small>Enquêtes &amp; Feedback Client</small>
            </span>
                    </button>

                    <nav className="help-nav-links" aria-label="Navigation">
                        <button className="help-nav-link" type="button" onClick={() => navigate('/')}>
                            Accueil
                        </button>

                        <button className="help-nav-link" type="button" onClick={() => navigate('/bureau')}>
                            Mon espace
                        </button>

                        <button className="help-nav-link active" type="button">
                            Centre d’aide
                        </button>
                    </nav>

                    <div className="help-nav-actions">
                        <button
                            className="help-theme-button"
                            type="button"
                            onClick={toggle}
                            aria-label="Changer de thème"
                        >
                            {theme === 'dark' ? '☀' : '☾'}
                        </button>

                        <button
                            className="help-login-button"
                            type="button"
                            onClick={() => navigate('/identification')}
                        >
                            Se connecter
                        </button>
                    </div>
                </header>

                <main>
                    <section className="help-hero">
                        <img
                            className="help-hero-image"
                            src="/background2.png"
                            alt=""
                            aria-hidden="true"
                        />

                        <div className="help-hero-overlay" aria-hidden="true" />

                        <div className="help-hero-ring one" aria-hidden="true" />
                        <div className="help-hero-ring two" aria-hidden="true" />

                        <div className="help-hero-content">
                            <div className="help-eyebrow">
                                <span />
                                CENTRE D’AIDE AFRILAND
                            </div>

                            <h1>
                                Besoin d’un coup de main ?
                                <span>Vous êtes au bon endroit.</span>
                            </h1>

                            <p className="help-hero-description">
                                Retrouvez les repères essentiels pour créer, diffuser et analyser vos enquêtes
                                clients dans un espace simple, structuré et sécurisé.
                            </p>

                            <div className="help-search" id="help-search">
                                <div className="help-search-shell">
                  <span className="help-search-icon" aria-hidden="true">
                    ⌕
                  </span>

                                    <input
                                        ref={inputRef}
                                        className="help-search-input"
                                        type="search"
                                        value={search}
                                        onChange={(event) => setSearch(event.target.value)}
                                        onKeyDown={(event) => {
                                            if (event.key === 'Enter') {
                                                event.preventDefault()
                                                runSearch()
                                            }
                                        }}
                                        placeholder="Rechercher une question, une fonctionnalité…"
                                        aria-label="Rechercher dans le centre d'aide"
                                    />

                                    {search && (
                                        <button
                                            className="help-search-clear"
                                            type="button"
                                            onClick={clearSearch}
                                            aria-label="Effacer la recherche"
                                        >
                                            ×
                                        </button>
                                    )}

                                    <span className="help-search-key">
                    CTRL K
                  </span>

                                    <button
                                        className="help-search-submit"
                                        type="button"
                                        onClick={runSearch}
                                    >
                                        Rechercher
                                    </button>
                                </div>

                                {submittedSearch && (
                                    <div className="help-search-results" id="help-results">
                                        <div className="help-search-results-header">
                      <span>
                        {filteredFaqs.length} résultat
                          {filteredFaqs.length > 1 ? 's' : ''}
                      </span>

                                            <span>
                        Cliquez sur une réponse pour y accéder
                      </span>
                                        </div>

                                        {filteredFaqs.length > 0 ? (
                                            filteredFaqs.map((faq, index) => (
                                                <button
                                                    className="help-result-item"
                                                    type="button"
                                                    key={faq.id}
                                                    onClick={() => openResult(faq)}
                                                >
                          <span className="help-result-number">
                            {String(index + 1).padStart(2, '0')}
                          </span>

                                                    <span className="help-result-copy">
                            <small>{faq.category}</small>
                            <strong>{faq.question}</strong>
                          </span>

                                                    <span className="help-result-arrow">
                            →
                          </span>
                                                </button>
                                            ))
                                        ) : (
                                            <div className="help-search-empty">
                                                Aucun résultat. Essayez avec « audience », « réponses »,
                                                « déclenchement » ou « enquête ».
                                            </div>
                                        )}
                                    </div>
                                )}

                                <div className="help-search-meta">
                  <span>
                    ✓ Recherche dans les questions fréquentes
                  </span>

                                    <span>
                    ↵ Entrée pour rechercher
                  </span>
                                </div>
                            </div>

                            <div className="help-trust">
                                <span>✓ Guides pratiques</span>
                                <span>✓ FAQ interactive</span>
                                <span>✓ Assistance centralisée</span>
                            </div>
                        </div>
                    </section>

                    <section className="help-section help-white">
                        <div className="help-container">
                            <div className="help-heading">
                                <div>
                                    <span className="help-kicker">01 · PAR OÙ COMMENCER ?</span>

                                    <h2>
                                        Trouvez rapidement
                                        <span> ce dont vous avez besoin.</span>
                                    </h2>
                                </div>

                                <p>
                                    Des repères clairs pour comprendre la plateforme et avancer rapidement.
                                </p>
                            </div>

                            <div className="help-card-grid">
                                {categories.map((category, index) => (
                                    <article
                                        className={`help-card ${
                                            index === 1
                                                ? 'dark'
                                                : index === 2
                                                    ? 'soft-card'
                                                    : index === 3
                                                        ? 'green-card'
                                                        : ''
                                        }`}
                                        key={category.number}
                                    >
                                        <div className="help-card-top">
                                            <span className="help-card-icon">{category.icon}</span>
                                            <span className="help-card-arrow">↗</span>
                                        </div>

                                        <span className="help-card-number">{category.number}</span>

                                        <h3>{category.title}</h3>
                                        <p>{category.description}</p>
                                    </article>
                                ))}
                            </div>
                        </div>
                    </section>

                    <section className="help-section help-dark">
                        <div className="help-container">
                            <div className="help-guide-layout">
                                <div className="help-guide-heading">
                                    <span className="help-kicker">02 · GUIDES RAPIDES</span>

                                    <h2>
                                        Une méthode simple pour
                                        <span> chaque étape.</span>
                                    </h2>

                                    <p>
                                        La plateforme suit un parcours cohérent pour vous accompagner de l’idée
                                        jusqu’à l’exploitation des retours.
                                    </p>

                                    <button
                                        className="help-primary-button"
                                        type="button"
                                        onClick={() => navigate('/bureau/enquetes/nouvelle')}
                                    >
                                        Créer ma première enquête →
                                    </button>
                                </div>

                                <div className="help-guide-list">
                                    {guides.map((guide) => (
                                        <article className="help-guide" key={guide.number}>
                                            <span className="help-guide-number">{guide.number}</span>

                                            <div>
                                                <h3>{guide.title}</h3>
                                                <p>{guide.text}</p>
                                            </div>

                                            <span className="help-guide-arrow">→</span>
                                        </article>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </section>

                    <section className="help-section help-soft" id="help-results-section">
                        <div className="help-container">
                            <div className="help-heading">
                                <div>
                                    <span className="help-kicker">03 · QUESTIONS FRÉQUENTES</span>

                                    <h2>
                                        Les réponses aux
                                        <span> questions essentielles.</span>
                                    </h2>
                                </div>

                                <p>
                                    Ouvrez une question pour afficher sa réponse complète.
                                    Les résultats de recherche arrivent directement ici.
                                </p>
                            </div>

                            <div className="help-faq-list">
                                {filteredFaqs.length > 0 ? (
                                    filteredFaqs.map((faq, index) => {
                                        const isOpen = openFaq === faq.id

                                        return (
                                            <article
                                                className={`help-faq-row ${isOpen ? 'open' : ''}`}
                                                id={faq.id}
                                                key={faq.id}
                                            >
                                                <button
                                                    className="help-faq-button"
                                                    type="button"
                                                    onClick={() => setOpenFaq(isOpen ? null : faq.id)}
                                                >
                          <span className="help-faq-index">
                            {String(index + 1).padStart(2, '0')}
                          </span>

                                                    <span className="help-faq-copy">
                            <small>{faq.category}</small>
                            <strong>{faq.question}</strong>
                          </span>

                                                    <span className="help-faq-toggle">
                            {isOpen ? '−' : '+'}
                          </span>
                                                </button>

                                                <div className="help-faq-answer">
                                                    <p>{faq.answer}</p>
                                                </div>
                                            </article>
                                        )
                                    })
                                ) : (
                                    <div className="help-empty">
                                        <div className="help-empty-icon">⌕</div>
                                        <h3>Aucun résultat</h3>
                                        <p>
                                            Aucune question ne correspond à votre recherche actuelle.
                                        </p>
                                    </div>
                                )}
                            </div>
                        </div>
                    </section>

                    <section className="help-section help-white">
                        <div className="help-container">
                            <div className="help-support">
                                <div className="help-support-glow" />

                                <div className="help-support-content">
                                    <span className="help-kicker">04 · ASSISTANCE</span>

                                    <h2>
                                        Une question qui reste
                                        <span> sans réponse ?</span>
                                    </h2>

                                    <p>
                                        Consultez les questions fréquentes ou revenez dans votre espace de travail
                                        pour continuer à gérer vos enquêtes.
                                    </p>

                                    <div className="help-support-actions">
                                        <button
                                            className="help-support-primary"
                                            type="button"
                                            onClick={() => navigate('/bureau')}
                                        >
                                            Ouvrir mon espace →
                                        </button>

                                        <button
                                            className="help-support-secondary"
                                            type="button"
                                            onClick={() => navigate('/')}
                                        >
                                            Retour à l’accueil
                                        </button>
                                    </div>
                                </div>

                                <div className="help-support-mark" aria-hidden="true">
                                    ?
                                </div>
                            </div>
                        </div>
                    </section>
                </main>

                <footer className="help-footer">
                    <div className="help-footer-brand">
                        <img src="/logo-afriland.png" alt="Afriland First Bank" />

                        <div className="help-footer-brand-text">
                            <strong>Afriland First Bank</strong>
                            <span>Plateforme d’enquêtes &amp; feedback client</span>
                        </div>
                    </div>

                    <span>Centre d’aide · 2026</span>
                </footer>
            </div>
        </>
    )
}
