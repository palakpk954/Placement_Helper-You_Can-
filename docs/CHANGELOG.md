# 📜 You_can — Project Changelog & Release Notes
> Detailed chronological ledger of every enhancement, feature sprint, refactoring, and bug fix.

All notable changes to the **You_can — JECRC University Placement Portal** are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased] - Post-Phase 4: Live Cloud LLM & Generative RAG

### Added
- **Google Gemini Cloud LLM Connectivity**:
  - Direct REST integration with Google Gemini API (`gemini-1.5-flash`) via `DefaultAiService.java`.
  - Configured in `application.properties`: `app.ai.api-key=${GEMINI_API_KEY:${APP_AI_API_KEY:}}`, `app.ai.model=${APP_AI_MODEL:gemini-1.5-flash}`, `temperature=0.2`, `max-tokens=1000`.
  - Upgraded HTTP client with robust Jackson `ObjectMapper` payload serialization/deserialization.
- **Institutional Retrieval-Augmented Generation (RAG)**:
  - Deep grounding pipeline dynamically assembling verified institutional records (company tiers, latest packages/CTC, hiring rounds, active upcoming visits, alumni mentors, and student interview tips).
  - Strict Anti-Hallucination system prompt ensuring Gemini never invents compensation figures or cutoff policies.
  - Privacy by Design (AI-4): Anonymizes student feedback and scrubs all PII (names, emails, roll numbers) before prompting external models.
- **Resilient Zero-Downtime Offline Fallback (AI-5)**:
  - Automatic, seamless fallback to the local deterministic grounded database engine if the API key is missing, invalid, or hits network/quota limits.
- **AI Health & Status Endpoint**:
  - `GET /api/ai/status` returning provider type, active model, cloud availability flag, and system health.
- **Expanded AI Unit Test Suite**:
  - Expanded `AiServiceTest.java` to 16 comprehensive unit tests covering cloud provider metadata, offline engine reporting, conversational greeting/gratitude fast-paths, and graceful fallback on simulated API errors.

- **Conversational Guidance & Greeting Engine**:
  - Natural greeting pattern matching (`hi`, `hello`, `hey`, `greetings`) providing an introductory tour and suggested queries.
- **AI Experience Summarizer**:
  - Multi-student review synthesis engine (`summarizeCompanyExperience()`).
  - REST endpoint `GET /api/ai/company-summary/{id}`.
  - "AI Synthesis: What to Expect" badge and card in company details (`templates/companies/detail.html`).
- **Question Topic & Difficulty Auto-Classifier**:
  - Heuristic classifier (`classifyQuestion()`) categorizing interview questions into DSA, DBMS, OS, Networks, System Design, or HR, plus predicting difficulty level.
  - REST endpoint `POST /api/ai/classify`.
  - "✨ AI Classify" instant-fill button on the contribution form (`templates/contribute/form.html`).
- **Automated AI Test Suite**:
  - Created `AiServiceTest.java` with 12 unit tests covering greetings, gratitude with typos, acknowledgments, farewells, company packages, upcoming drives, classification, and strict fallback.
- **Master Project Documentation**:
  - Created root [`DOCUMENTATION.md`](../DOCUMENTATION.md) and [`docs/`](./) repository tracking architecture, sprints, bug fixes, and runbooks.
- **Docker Containerization for Cloud (Render)**:
  - Added multi-stage `Dockerfile` (Maven builder + Eclipse Temurin 21 JRE runtime) with non-root user, container memory limits (`-Xmx384m`), and dynamic `$PORT` binding.
  - Added `.dockerignore` for minimal, lightning-fast container context transfers.

### Fixed
- Fixed port 8080 process conflict caused by leftover background Java processes from earlier IDE sessions.
- Cleaned stale `.class` files in Maven `target/classes` resolving bytecode mismatch errors (`ClassFormatError` on `AuditLog`).
- Aligned accessor methods in `DefaultAiService` to match JPA entities (`Round.getRoundNumber()`, `AlumniProfile.getJobRole()`).
- Addressed chatbot strict fallback triggering on greetings by adding dedicated greeting and capability guidance filters.
- Fixed conversational appreciation and acknowledgments (e.g. *"ok thank you so muhc"* or *"got it"*) incorrectly falling back to the strict "no verified data" message by implementing resilient token matching (`isGratitude()`, `isAcknowledgment()`, `isFarewell()`).
- Resolved Render Linux native deployment failure (`bash: java: command not found`) by switching to standard Docker runtime containerization.
- Fixed 100% desktop zoom navbar overflow, profile avatar clipping, and container alignment by unifying layout to `container-xxl`, adding `flex-shrink: 0` to actions, and compacting stat widgets.
- Fixed Roadmap 500 internal server error, removed redundant nested navbar layout, and implemented live progress tracking bars and status badges.
- Resolved IDE false-positive diagnostics by configuring `.vscode/settings.json` (`nullAnalysis.mode: disabled`, `updateBuildConfiguration: automatic`).
- Eliminated all unused imports, unused fields, and potential null-pointer warnings across controllers, entities, and test suites, achieving a zero-warning, 100% passing test build (28/28 tests).

---

## [Phase 3] - 2026-10-15 - Senior Mentorship & Verified Question Bank

### Added
- **Alumni Mentorship Network**:
  - `AlumniProfile` entity linking placed graduates with their current company, job role, package, and contact channels.
  - Alumni directory view (`/mentors`) with multi-facet filters by company and department.
- **Interview Experience Hub**:
  - Round-by-round interview breakdown (`Round` entity) capturing difficulty, question types, and outcomes.
  - Student interview review archive (`/experiences`) with company tags.
- **Interview Question Bank**:
  - Question repository categorized by topic and difficulty.
  - Search and filter capabilities for practice preparation.

---

## [Phase 2] - 2026-10-15 - Placement Calendar & Dynamic Eligibility Engine

### Added
- **Dynamic Eligibility Matrix Engine**:
  - `EligibilityService.java` calculating live student eligibility against recruiter criteria (CGPA cutoff, active backlogs, engineering branch).
  - Clear rejection codes explaining shortfalls (e.g., `CGPA_SHORTFALL`, `BACKLOG_DISQUALIFIED`).
- **Placement Calendar & Drives**:
  - Interactive placement timeline view (`/calendar`) displaying scheduled written tests, technical interviews, and HR discussions.
  - Recruiter upcoming drive tracker (`UpcomingDrive` entity).
- **Student Contribution & Admin Moderation Subsystem**:
  - Contribution form (`/contribute`) allowing students and alumni to submit questions and interview experiences.
  - Admin approval workflow (`/admin/contributions`) ensuring unverified data never enters public archives.

---

## [Phase 1] - 2026-10-15 - Core Portal Architecture & RBAC

### Added
- **Security & Authorization**:
  - Spring Security 6 integration with role hierarchy (`ROLE_ADMIN`, `ROLE_STUDENT`, `ROLE_ALUMNI`).
  - Custom user authentication service with BCrypt password encryption.
  - 1-click demo login buttons for rapid local testing on `/login`.
- **Database & Data Layer**:
  - Embedded file-backed H2 database configuration.
  - `DataSeeder.java` pre-populating institutional recruiters, demo users, past drives, and rounds.
- **Admin Management Portal**:
  - Admin dashboard (`/admin`) displaying active drives, student registration metrics, and audit logs.
  - Audit logging system tracking administrative actions.
- **Styling & Design System**:
  - Custom modern Vanilla CSS design system (`styles.css`) featuring responsive layouts, dark/light themes, and university branding.

---

## [Initial Setup] - 2026-10-15 - Project Genesis & Git Sync

### Added
- Initialized local Git repository and synchronized with GitHub remote `palakpk954/Placement_Helper-You_Can-`.
- Overhauled [`README.md`](../README.md) with comprehensive system architecture, ER diagrams, security model, and API route tables.
