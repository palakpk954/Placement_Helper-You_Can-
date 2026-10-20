# 📜 You_can — Project Changelog & Release Notes
> Detailed chronological ledger of every enhancement, feature sprint, refactoring, and bug fix.

All notable changes to the **You_can — JECRC University Placement Portal** are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [1.2.0] - 2026-10-20 - Resume Bullet Enhancer & Job Description Matcher (Preparation Suite)

### Added
- **AI STAR Resume Bullet Enhancer**:
  - Added engineering domain selection (Full-Stack, Backend/Systems, Frontend/UI, Data & AI, Cloud & DevOps, Core CS).
  - Implemented automated weakness diagnosis detecting passive action verbs ("worked on", "helped", "did") and missing quantifiable metrics.
  - Generates 3 ATS-optimized STAR rewrites per bullet:
    - *Metric-Driven*: Quantified performance and business metrics (e.g., latency reduction %, active user scale).
    - *Technical Depth*: Architectural patterns, protocols, test coverage, and design principles.
    - *Concise One-Liner*: Impactful, punchy single-line format for tight resume layouts.
  - Added before/after scoring progression (e.g., 45 -> 94 / 100) and interview coaching tips.
  - 1-click clipboard copy buttons with live visual confirmation.
- **Job Description & Recruiter Keyword Matcher**:
  - Direct integration with Apache PDFBox `PDFTextStripper` in `ResumeService.extractResumeText()` automatically extracting text from student's active uploaded PDF resume.
  - "Quick-Select Upcoming Campus Drive" dropdown instantly pre-populating recruiter requirements from scheduled campus visits (e.g., Celebal, Microsoft, Deloitte).
  - Evaluates resume text against an industry catalog of 70+ technical keywords (languages, frameworks, cloud platforms, databases, DevOps, system design).
  - Real-time ATS match percentage calculation with dynamic progress bar and color-coded status badges (`Strong ATS Match`, `Moderate Match`, `Low Match`).
  - Visual breakdown rendering green pill badges for matched keywords and red pill badges for missing recruiter keywords.
  - Actionable keyword incorporation advice and resume length guidance.
- **Architecture & Resilient Fallback (AI-5)**:
  - Dual-mode execution: cloud-powered with Google Gemini (`gemini-1.5-flash`) and instant deterministic local fallback.
  - Dedicated REST endpoint controller `ResumeApiController` (`/api/resume/enhance-bullet` and `/api/resume/match-jd`).
  - Added comprehensive unit tests in `AiServiceTest.java` verifying 100% test pass rate (37/37 passing).

### Fixed
- **100% Desktop Zoom Navbar Clipping & Horizontal Alignment**:
  - Replaced restrictive `container-xl` (1140px max) with `container-xxl` (1360px max) in both navbar and footer.
  - Streamlined student navigation into 5 balanced clusters (`Companies`, `Upcoming`, `Calendar`, `Prep Tools ▾`, `Community ▾`), eliminating ~210px of redundant horizontal menu sprawl.
  - Added `flex-shrink: 0` to all right toolbar controls (Theme toggle, unread notification badge, AI Assistant pill, and Student Profile dropdown).
  - Unified container grid across `/upcoming` to align with the dashboard and navbar, eliminating horizontal scroll and right-edge avatar clipping.

---

## [1.1.0] - 2026-10-20 - Bug Fixes, Role Hardening & UI Overhaul

### Added
- **Notification Controls & Filter Tabs**:
  - Added "All" and "Unread" filter pills on `/notifications` for fast filtering.
  - Added single-click mark-as-read icon buttons and individual notification dismiss actions.
  - Direct `GET /notifications/{id}/open` endpoint automatically marking alerts as read and forwarding the user to the destination page.
  - Visual category icon badges distinguishing Mock Interviews, Deadlines, Campus Recruitment Drives, and Senior Reviews.
- **Dynamic Average Package Metric**:
  - Implemented real-time average package calculation in `StatsService.java` aggregating drives with valid CTC data.
  - Added a dedicated 5th KPI metric card to the Placement Statistics dashboard (`/statistics`).
- **Senior Mock Interview Shortcuts**:
  - Integrated "Host / Manage Mock Slots" access directly into the Alumni Mentors directory (`/alumni`) for registered mentors.
  - Added a quick link under Admin Tools in the top navigation bar for Placement Cell administrators.

### Fixed
- **Notification Heavy Bounding Boxes & Text Wrapping**:
  - Removed `border-primary border-4` that caused loud 4px blue square bounding boxes around list items.
  - Replaced with modern card styling featuring subtle left accents (`border-left: 4px solid var(--bs-primary)`) and "New" badges.
  - Fixed `View →` button wrapping into two lines by applying `text-nowrap`.
- **Hourly Deadline Reminder Duplication**:
  - Fixed `@Scheduled(cron = "0 0 * * * *")` in `NotificationService.checkDeadlineReminders()` to verify `!existsByUserIdAndNotificationTypeAndTitle(...)` before dispatching, preventing hourly spam.
  - Added startup deduplication in `DataInitializer` to purge historical duplicate notifications.
- **Mock Interview Slot Creation RBAC**:
  - Restricted "Offer a Mock Interview Slot" creation form and `+ Host a Mock Slot` button strictly to verified mentors (`AlumniProfile`) and placement cell admins (`ROLE_ADMIN`).
  - Standard students now only see booking capabilities. Added server-side authorization check rejecting unprivileged slot creation attempts.
- **Mock Booking SpEL Evaluation**:
  - Resolved `SpelEvaluationException: EL1008E: Property or field 'slot' cannot be found on MockBooking` by updating template bindings to `mockSlot` and adding `getSlot()` accessor on `MockBooking`.
- **Resume Scanner ATS Score & Checklist Display**:
  - Implemented transient JSON getters on `Resume.java` and controller mapping so ATS score, page count, and contact checklist details properly render in the UI.
- **Placement Calendar Seasonality Matrix**:
  - Fixed SpEL map access in `calendar/index.html` from `entry.value[y]` to `entry.value.get(y)`, resolving empty 0-count drive matrix cells across years.

---

## [1.0.0] - Post-Phase 4: Live Cloud LLM & Generative RAG

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
