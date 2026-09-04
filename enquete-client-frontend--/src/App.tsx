import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext'

import HomePage from './pages/HomePage'
import IdentificationPage from './pages/IdentificationPage'
import RegistrationPage from './pages/RegistrationPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
const DashboardPage=lazy(()=>import('./pages/DashboardPage'));const SurveysPage=lazy(()=>import('./pages/SurveysPage'));const SurveyBuilderPage=lazy(()=>import('./pages/SurveyBuilderPage'));const ResponsesPage=lazy(()=>import('./pages/ResponsesPage'));const AnalyticsPage=lazy(()=>import('./pages/AnalyticsPage'));const SettingsPage=lazy(()=>import('./pages/SettingsPage'));const ProfilePage=lazy(()=>import('./pages/ProfilePage'));const HelpPage=lazy(()=>import('./pages/HelpPage'));const PublicSurveyPage=lazy(()=>import('./pages/PublicSurveyPage'));const InboxPage=lazy(()=>import('./pages/InboxPage'))

import { WorkspaceLayout } from './components/WorkspaceLayout'

function ProtectedLayout() {
    const { user, loading } = useAuth()

    if (loading) {
        return <div className="public-center">Chargement…</div>
    }

    if (!user) {
        return <Navigate to="/identification" replace />
    }

    return <WorkspaceLayout />
}

export default function App() {
    return (
        <Suspense fallback={<div className="public-center">Chargement de la page…</div>}><Routes>
            {/* Pages publiques */}
            <Route path="/" element={<HomePage />} />
            <Route path="/identification" element={<IdentificationPage />} />
            <Route path="/inscription" element={<RegistrationPage />} />
            <Route path="/mot-de-passe-oublie" element={<ForgotPasswordPage />} />
            <Route path="/reinitialiser-mot-de-passe" element={<ResetPasswordPage />} />

            {/* Centre d'aide public : accessible depuis l'accueil sans connexion */}
            <Route path="/aide" element={<HelpPage />} />

            {/* Enquête publique */}
            <Route path="/survey/:key" element={<PublicSurveyPage />} />

            {/* Espace connecté */}
            <Route path="/bureau" element={<ProtectedLayout />}>
                <Route index element={<DashboardPage />} />
                <Route path="enquetes" element={<SurveysPage />} />
                <Route path="enquetes/nouvelle" element={<SurveyBuilderPage />} />
                <Route path="enquetes/:id" element={<SurveyBuilderPage />} />
                <Route path="reponses" element={<ResponsesPage />} />
                <Route path="inbox" element={<InboxPage />} />
                <Route path="analytics" element={<AnalyticsPage />} />
                <Route path="parametres" element={<SettingsPage />} />
                <Route path="profil" element={<ProfilePage />} />

                {/* Aide aussi disponible depuis l'espace connecté */}
                <Route path="aide" element={<HelpPage />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes></Suspense>
    )
}
