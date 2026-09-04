# Copilot instructions

## Project shape

This is a two-part Afriland First Bank survey and customer-feedback application:

- The backend is a Spring Boot 4 application in `src/main/java`, built with Gradle and Java 21.
- The frontend is a React 19 + TypeScript application in `enquete-client-frontend--`, built with Vite.
- PostgreSQL is the runtime database. Local development expects database `enquete_db` on `localhost:5432`.

Keep backend and frontend changes aligned: API response shapes are mirrored in `enquete-client-frontend--/src/types.ts`, and frontend requests go through `enquete-client-frontend--/src/api.ts`.

## Build, run, test, and lint

Run backend commands from the repository root:

```powershell
.\gradlew.bat bootRun
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat test --tests "cm.afriland.enquete.SomeTest"
```

The Gradle build uses Java 21 and JUnit Platform. There are currently no project-owned test classes, but keep new tests under `src/test` and use the focused `--tests` form when iterating.

Run frontend commands from `enquete-client-frontend--`:

```powershell
npm install
npm run dev
npm run build
npm run lint
npm run preview
```

The frontend package has no test script or project-owned frontend test suite. `npm run build` runs the TypeScript project build before Vite production bundling; `npm run lint` uses Oxlint.

Vite serves the frontend on `http://localhost:5173` and proxies `/api`, `/oauth2`, `/login`, and `/logout` to the backend on `http://localhost:8080`. The backend health check is available through `http://localhost:5173/api/health`.

## Architecture

### Backend request flow

HTTP controllers under `src/main/java/cm/afriland/enquete/controller` expose REST endpoints. They resolve the current user through the server-side HTTP session, validate ownership, and delegate business rules to services under `Service` (note the existing capitalized package name). Services use Spring Data JPA repositories under `repository` to read and write entities under `model`.

The main protected survey API is `/api/surveys`. It owns survey CRUD, draft saving, publication state changes, response export, and analytics. Public respondents use `/api/public/surveys/{key}` to load a published survey, record a `VIEWED` delivery event, and submit a response. Audience preview/options, dashboard data, support conversations, authentication, and trigger handling have separate controllers.

Survey data is intentionally split across `Survey`, `SurveyQuestion`, `SurveyLogicRule`, `SurveyResponse`, and `SurveyDeliveryEvent`. Questions and logic rules are serialized/transported with question indexes so a builder can save a draft before database IDs exist. Analytics are derived from delivery events and completed responses rather than stored counters.

`ModuleEnqueteClientApplication` enables scheduling. `SurveyScheduler` periodically promotes scheduled surveys to `Active` when their configured date/time has arrived. `DatabaseMigration` performs the small startup schema adjustment currently required for `users.photo_url`.

Authentication supports identifier/password accounts and Google OAuth2. `SecurityConfig` permits public/auth/support entry points and requires authentication for other endpoints; the frontend must use the session cookie rather than storing identity in `localStorage`.

### Frontend request and route flow

`App.tsx` defines public pages, public survey routes, and the protected `/bureau` workspace. `ProtectedLayout` uses `AuthContext` to load `/api/auth/me` and redirects unauthenticated users to `/identification`; `WorkspaceLayout` supplies the shared navigation and outlet.

Pages are intentionally split by product area (`DashboardPage`, `SurveysPage`, `SurveyBuilderPage`, `ResponsesPage`, `AnalyticsPage`, `InboxPage`, settings/profile/help). API calls should use the shared `api()` helper, which includes credentials, disables caching, parses JSON/text responses, and turns non-2xx responses into errors.

## Repository-specific conventions

- Preserve the existing API contract when changing backend DTOs. Frontend types in `src/types.ts` use the backend’s serialized field names and user-facing survey status strings (`Brouillon`, `Programmée`, `Active`, etc.).
- Enforce ownership in backend service/controller calls using the authenticated user ID; do not accept an owner ID from the browser as authority.
- Keep public survey endpoints separate from authenticated workspace endpoints. A public survey is addressed by `publicKey`, while workspace operations use the numeric survey ID.
- Use the existing session model: frontend requests include credentials, and logout invalidates the server session. Do not add client-side identity persistence.
- Keep business logic in services and persistence in repositories; controllers should remain thin endpoint adapters. Use the existing Java record request/view DTO style where applicable.
- When adding survey fields, update the JPA model, service request/response mapping, frontend TypeScript types, and the relevant builder/list/detail page together.
- Survey builder changes must preserve the draft/publish lifecycle and question-index logic. Trigger, audience, channel, and settings JSON are part of the saved survey configuration.
- For audience behavior, use the established filter field names (`customerType`, `agency`, `city`, `relationshipStatus`, `product`) and case-insensitive matching.
- For frontend pages, follow the existing plain CSS class-based styling and shared components/contexts. Avoid introducing a new state-management or UI framework for a single page.
- Configuration is environment-specific. Keep database credentials and OAuth/OpenAI secrets out of source changes; use local properties/environment variables, especially `OPENAI_BASE_URL`, `OPENAI_API_KEY`, and `OPENAI_MODEL` for the optional support assistant.
- The project’s user-facing copy is primarily French; preserve existing French labels and status values when extending the UI or API.
