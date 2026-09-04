# Afriland First Bank — Enquêtes & Feedback Client

Projet nettoyé et séparé en pages React indépendantes, avec backend Spring Boot + PostgreSQL.

## Structure

- `src/main/java/cm/afriland/enquete/model` : entités métier.
- `src/main/java/cm/afriland/enquete/repository` : accès PostgreSQL.
- `src/main/java/cm/afriland/enquete/Service` : logique métier.
- `src/main/java/cm/afriland/enquete/controller` : API REST.
- `enquete-client-frontend--/src/pages` : une page/codage séparé pour Dashboard, Enquêtes, Builder, Réponses, Analytics, Paramètres, Profil, etc.

## Authentification

La source de vérité du compte connecté est la session serveur. Le frontend utilise `/api/*` via le proxy Vite, sans stocker l'identité du compte dans `localStorage`.

Les comptes classiques utilisent identifiant + mot de passe. Google OAuth2 peut fournir automatiquement une photo quand Google renvoie l'attribut `picture`. Les comptes classiques peuvent ajouter leur photo depuis `Mon profil`.

## PostgreSQL

Base attendue : `enquete_db` sur `localhost:5432`.

Le projet utilise `spring.jpa.hibernate.ddl-auto=update` pour créer/mettre à jour les tables automatiquement.

Avant de démarrer, fournissez les secrets via variables d’environnement : `DATABASE_PASSWORD`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `MAIL_USERNAME` et `MAIL_PASSWORD`. En production, utilisez `JPA_DDL_AUTO=validate`, `FRONTEND_URL` en HTTPS et `SESSION_COOKIE_SECURE=true`. Le secret Google précédemment exposé doit être révoqué dans Google Cloud Console.

## Démarrage

## Mise en ligne avec GitHub et Render

Le fichier `render.yaml` déclare automatiquement PostgreSQL, le backend Spring Boot et le frontend Nginx. Après avoir créé le dépôt GitHub, dans Render choisissez **New > Blueprint** et sélectionnez ce dépôt.

Dans Render, renseignez les variables marquées `sync: false` (Google OAuth, SMTP et éventuellement OpenAI). Ajoutez ensuite dans Google Cloud l’URI :
`https://afriland-enquetes-frontend.onrender.com/login/oauth2/code/google`

Le dépôt ne doit contenir aucun secret. Les valeurs de production se configurent uniquement dans Render.

Backend :

```powershell
.\\gradlew.bat bootRun
```

Frontend :

```powershell
cd .\\enquete-client-frontend--
npm install
npm run dev
```

URL frontend : `http://localhost:5173`

URL backend : `http://localhost:8080`

Diagnostic : `http://localhost:5173/api/health`

## Cycle d'une enquête

Informations → Questions → Logique → Aperçu LIVE → Déclenchement → Audience → Diffusion → Paramètres → Publication.

Une enquête sauvegardée est liée au compte connecté. Le constructeur transmet les questions et règles avec des index de questions, ce qui évite les problèmes d'identifiants temporaires lors d'une sauvegarde avant publication.

## Déclenchement

La configuration supporte : visite de page, temps sur page, événement API/externe, entrée d'audience, date/heure et manuel. La campagne planifiée est activée automatiquement par le scheduler Spring lorsqu'elle atteint sa date/heure.

## Audience

Le constructeur permet une audience dynamique ou fixe avec filtres métier. La prévisualisation utilise la table `customer_profiles` lorsque des profils sont disponibles.

## Diffusion

Les canaux disponibles sont le lien partageable, le widget, l’email SMTP, l’API et l’envoi manuel. Push, bannière et mobile restent désactivés tant qu’un fournisseur externe n’est pas configuré.

## Réponses et analytics

Une visite publique est enregistrée dans `survey_delivery_events`. Une réponse terminée est enregistrée dans `survey_responses`. Les statistiques du tableau Analytics sont calculées à partir de ces données.
