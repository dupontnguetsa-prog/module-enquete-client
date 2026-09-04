import { useNavigate } from 'react-router-dom'
import { useTheme } from '../context/ThemeContext'
import '../styles/marketing.css'
import { IntercomWidget } from '../components/IntercomWidget'

export default function HomePage() {
  const navigate = useNavigate()
  const { theme, toggle } = useTheme()

  return (
      <div
          className={`marketing-home ${
              theme === 'dark' ? 'marketing-home--dark' : ''
          }`}
      >
        <header className="marketing-home__nav">
          <button
              className="marketing-home__brand"
              type="button"
              onClick={() =>
                  window.scrollTo({
                    top: 0,
                    behavior: 'smooth',
                  })
              }
              aria-label="Retour en haut"
          >
            <img
                src="/logo-afriland.png"
                alt="Afriland First Bank"
            />

            <span>
            <strong>Afriland First Bank</strong>
            <small>Enquêtes &amp; Feedback Client</small>
          </span>
          </button>

          <nav
              className="marketing-home__links"
              aria-label="Navigation principale"
          >
            <a href="#plateforme">Plateforme</a>
            <a href="#parcours">Parcours</a>
            <a href="#analyses">Analyses</a>
            <a href="#securite">Sécurité</a>

            <button
                type="button"
                onClick={() => navigate('/aide')}
            >
              Aide
            </button>
          </nav>

          <div className="marketing-home__actions">
            <button
                className="marketing-icon-button"
                type="button"
                onClick={toggle}
                aria-label="Changer de thème"
            >
              {theme === 'dark' ? '☀' : '☾'}
            </button>

            <button
                className="marketing-button marketing-button--ghost"
                type="button"
                onClick={() => navigate('/identification')}
            >
              Se connecter
            </button>

            <button
                className="marketing-button marketing-button--primary"
                type="button"
                onClick={() => navigate('/inscription')}
            >
              Créer mon espace <span>→</span>
            </button>
          </div>
        </header>

        <main>
          <section className="marketing-home__hero">
            <div className="marketing-home__hero-background" aria-hidden="true">
              <img
                  className="marketing-home__hero-background-image"
                  src="/background1.png"
                  alt=""
                  loading="eager"
                  decoding="async"
              />
              <div className="marketing-home__hero-overlay" />
            </div>

            <div className="marketing-home__hero-copy">
            <span className="marketing-eyebrow">
              CUSTOMER EXPERIENCE · AFRILAND FIRST BANK
            </span>

              <h1>
                Écoutez vos clients.
                <br />
                <span>Décidez avec confiance.</span>
              </h1>

              <p>
                Une plateforme pensée pour créer des enquêtes,
                cibler les bons clients, déclencher les bons parcours
                et transformer les retours en décisions concrètes.
              </p>

              <div className="marketing-home__hero-actions">
                <button
                    className="marketing-button marketing-button--primary marketing-button--large"
                    type="button"
                    onClick={() => navigate('/inscription')}
                >
                  Commencer une enquête <span>→</span>
                </button>

                <button
                    className="marketing-button marketing-button--light marketing-button--large"
                    type="button"
                    onClick={() => navigate('/identification')}
                >
                  Accéder à mon espace
                </button>
              </div>

              <div className="marketing-home__trust">
                <span>✓ Session sécurisée</span>
                <span>✓ Données centralisées</span>
                <span>✓ Pilotage en temps réel</span>
              </div>
            </div>

            <div
                className="marketing-home__visual"
                aria-label="Aperçu de la plateforme"
            >
              <div className="marketing-floating-card marketing-floating-card--top">
                <small>Taux de réponse</small>
                <strong>73,6 %</strong>
                <span>+4,1 %</span>
              </div>

              <div className="marketing-device">
                <div className="marketing-device__header">
                  <div>
                    <small>AFRILAND FIRST BANK</small>
                    <strong>Enquête · Satisfaction agence</strong>
                  </div>

                  <span>ACTIVE</span>
                </div>

                <div className="marketing-device__body">
                  <div className="marketing-score">
                    <small>Satisfaction moyenne</small>
                    <strong>
                      8,4 <span>/10</span>
                    </strong>
                  </div>

                  <div
                      className="marketing-chart"
                      aria-hidden="true"
                  >
                    <i />
                    <i />
                    <i />
                    <i />
                    <i />
                    <i />
                    <i />
                    <i />
                  </div>

                  <div className="marketing-device__stats">
                    <div>
                      <strong>1 284</strong>
                      <span>Réponses</span>
                    </div>

                    <div>
                      <strong>82 %</strong>
                      <span>Complétion</span>
                    </div>

                    <div>
                      <strong>+42</strong>
                      <span>NPS</span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="marketing-floating-card marketing-floating-card--bottom">
                <small>Enquêtes actives</small>
                <strong>12</strong>
              </div>
            </div>
          </section>

          <section
              id="plateforme"
              className="marketing-section marketing-section--white"
          >
            <div className="marketing-section__inner">
            <span className="marketing-eyebrow">
              01 · PLATEFORME
            </span>

              <h2>
                Tout le cycle de feedback au même endroit.
              </h2>

              <p className="marketing-section__lead">
                Construisez, ciblez, diffusez et analysez
                sans quitter votre espace de travail.
              </p>

              <div className="marketing-feature-grid">
                <article>
                  <b>01</b>
                  <h3>Créer</h3>
                  <p>
                    Questions, logique et aperçu LIVE dans
                    un même builder.
                  </p>
                </article>

                <article>
                  <b>02</b>
                  <h3>Cibler</h3>
                  <p>
                    Audiences fixes ou dynamiques avec filtres métier.
                  </p>
                </article>

                <article>
                  <b>03</b>
                  <h3>Déclencher</h3>
                  <p>
                    Événement, date, visite, temps sur page,
                    API ou manuel.
                  </p>
                </article>

                <article>
                  <b>04</b>
                  <h3>Analyser</h3>
                  <p>
                    Réponses, tendances, taux de complétion
                    et indicateurs.
                  </p>
                </article>
              </div>
            </div>
          </section>

          <section
              id="parcours"
              className="marketing-section marketing-section--dark"
          >
            <div className="marketing-section__inner marketing-section__inner--split">
              <div>
              <span className="marketing-eyebrow">
                02 · PARCOURS
              </span>

                <h2>
                  Une expérience cohérente du premier clic
                  à l’analyse.
                </h2>

                <p className="marketing-section__lead">
                  Chaque enquête garde son contexte, son audience,
                  ses règles et ses canaux de diffusion.
                </p>
              </div>

              <div className="marketing-flow-card">
                <div>
                  <span>01</span>
                  <strong>Informations</strong>
                  <small>Contexte</small>
                </div>

                <div>
                  <span>02</span>
                  <strong>Questions</strong>
                  <small>Contenu</small>
                </div>

                <div>
                  <span>03</span>
                  <strong>Logique</strong>
                  <small>Parcours</small>
                </div>

                <div>
                  <span>04</span>
                  <strong>Déclenchement</strong>
                  <small>Quand</small>
                </div>

                <div>
                  <span>05</span>
                  <strong>Audience</strong>
                  <small>Qui</small>
                </div>

                <div>
                  <span>06</span>
                  <strong>Diffusion</strong>
                  <small>Où</small>
                </div>
              </div>
            </div>
          </section>

          <section
              id="analyses"
              className="marketing-section marketing-section--white"
          >
            <div className="marketing-section__inner marketing-analysis">
              <div>
              <span className="marketing-eyebrow">
                03 · ANALYSES
              </span>

                <h2>
                  Voyez ce qui change réellement.
                </h2>

                <p className="marketing-section__lead">
                  Des indicateurs lisibles pour passer du commentaire
                  à l’action.
                </p>
              </div>

              <div className="marketing-kpi-grid">
                <article>
                  <small>Réponses</small>
                  <strong>1 284</strong>
                  <span>+12,4 %</span>
                </article>

                <article>
                  <small>Complétion</small>
                  <strong>82 %</strong>
                  <span>+5,2 %</span>
                </article>

                <article>
                  <small>NPS</small>
                  <strong>+42</strong>
                  <span>+8 pts</span>
                </article>
              </div>
            </div>
          </section>

          <section
              id="securite"
              className="marketing-security"
          >
            <div className="marketing-section__inner marketing-security__inner">
              <div>
              <span className="marketing-eyebrow">
                04 · SÉCURITÉ
              </span>

                <h2>
                  Un espace de travail protégé.
                </h2>

                <p>
                  Votre compte, vos enquêtes et vos résultats
                  restent séparés par session et par utilisateur.
                </p>
              </div>

              <div className="marketing-security__badge">
                ✓ Données centralisées
                <br />
                ✓ Accès authentifié
                <br />
                ✓ Session liée au compte
              </div>
            </div>
          </section>
        </main>

        <footer className="marketing-home__footer">
          <img
              src="/logo-afriland.png"
              alt="Afriland First Bank"
          />

          <span>Enquêtes &amp; Feedback Client</span>

          <small>© 2026 Afriland First Bank</small>
        </footer>

        <IntercomWidget />
      </div>
  )
}
