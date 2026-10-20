# 📘 You_can — Master Project Documentation & Living Update Log
> **JECRC University Placement Portal**  
> *A unified campus recruitment intelligence platform connecting students, placed alumni, and placement cell administrators.*

---

## 📌 Document Overview & Purpose
This document serves as the **single source of truth** and **living development log** for the `Placement_Helper` (branded as **You_can**) application. It is maintained continuously to record:
- The architectural design, modules, and domain boundaries.
- Every development phase, feature sprint, and implementation milestone.
- All technical bug fixes, troubleshooting decisions, and configuration changes.
- Operational guides for testing, running, and extending the platform.

Whenever changes or new features are introduced to the repository, this file and the companion documents in the [`docs/`](./docs/) directory are updated to reflect the exact state of the codebase.

---

## 📑 Table of Contents
1. [System Identity & Core Principles](#-system-identity--core-principles)
2. [Technology Stack](#-technology-stack)
3. [Living Development Log & Sprint History](#-living-development-log--sprint-history)
   - [Sprint 0: Repository Setup & Git Synchronization](#sprint-0-repository-setup--git-synchronization)
   - [Sprint 1: Core Portal Architecture & RBAC (Phase 1)](#sprint-1-core-portal-architecture--rbac-phase-1)
   - [Sprint 2: Placement Calendar & Dynamic Eligibility Engine (Phase 2)](#sprint-2-placement-calendar--dynamic-eligibility-engine-phase-2)
   - [Sprint 3: Senior Mentorship & Verified Question Bank (Phase 3)](#sprint-3-senior-mentorship--verified-question-bank-phase-3)
   - [Sprint 4: AI Preparation Suite & Guardrails (Phase 4)](#sprint-4-ai-preparation-suite--guardrails-phase-4)
4. [Troubleshooting & Bug Fix Journal](#-troubleshooting--bug-fix-journal)
5. [System Architecture & Module Directory](#-system-architecture--module-directory)
6. [API & Endpoint Reference](#-api--endpoint-reference)
7. [Database Schema & Data Model](#-database-schema--data-model)
8. [Security & Privacy Governance](#-security--privacy-governance)
9. [Developer Runbook & Testing Guide](#-developer-runbook--testing-guide)
10. [Future Roadmap](#-future-roadmap)
11. [How to Maintain & Update This Documentation](#-how-to-maintain--update-this-documentation)

---

## 🎯 System Identity & Core Principles

| Attribute | Specification |
| :--- | :--- |
| **Project Name** | **You_can — JECRC University Placement Portal** |
| **Repository** | [`palakpk954/Placement_Helper-You_Can-`](https://github.com/palakpk954/Placement_Helper-You_Can-) |
| **Target Audience** | Students (pre-final & final year), Placed Alumni Mentors, Placement Cell Admins |
| **Primary Mission** | Democratize campus placement intelligence through verified past interview experiences, dynamic eligibility computation, upcoming drive tracking, and strictly grounded AI assistance. |

### Core Tenets
1. **100% Grounded Intelligence (Anti-Hallucination)**: The AI assistant and summarization modules only answer from verified campus records (hiring drives, rounds, questions, packages, mentor profiles). No fabricated cutoffs or invented rounds.
2. **Strict Privacy by Design (PRD AI-4)**: Student resumes, contact numbers, private emails, and raw identifiers are never shared with AI pipelines or unprivileged users.
3. **Role-Based Operational Safety**: Distinct role boundaries (`ADMIN`, `STUDENT`, `ALUMNI`) with granular Spring Security enforcement.
4. **Institutional Memory**: Placement questions, round structures, and package details are permanently archived as actionable intelligence for upcoming student batches.

---

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.3.4 (Java 17 LTS)
- **Security**: Spring Security 6.3 with BCrypt password hashing and custom RBAC handlers
- **Data Persistence**: Spring Data JPA with Hibernate ORM
- **Database**: Embedded H2 Database (File-backed: `./data/youcan.mv.db`) with active console support
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Surefire plugin

### Frontend
- **Template Engine**: Thymeleaf with Spring Security 6 dialect
- **UI Framework**: Bootstrap 5.3.3 & Bootstrap Icons 1.11.3
- **Styling**: Tailored Vanilla CSS design system (`src/main/resources/static/css/styles.css`) with glassmorphism, responsive typography (`Inter` & `Outfit`), custom CSS custom properties, and dark/light mode tokens.
- **Client Scripts**: Modular Vanilla JavaScript (`src/main/resources/static/js/app.js`) handling dynamic DOM updates, asynchronous chat streaming, modal flows, and filtering.

### AI Engine
- **Module**: `in.jecrcu.placement.service.ai` (`AiService` interface + `DefaultAiService` implementation)
- **Capabilities**:
  - Grounded question-answering with fuzzy keyword & semantic matching across JPA repositories.
  - Multi-experience synthesis and key-takeaway summarization.
  - Question domain auto-classification (DSA, DBMS, OS, Networks, HR, Aptitude, System Design).
  - Difficulty estimation based on technical complexity indicators.
  - Pluggable architecture ready for seamless drop-in integration with external LLMs (Google Gemini / OpenAI).

---

## 📅 Living Development Log & Sprint History

| Date / Sprint | Phase | Key Milestones | Status |
| :--- | :--- | :--- | :--- |
| **2026-10-15** | Sprint 0 | Initialized Git repository, connected GitHub remote, reconciled upstream README, and pushed initial codebase. | ✅ Completed |
| **2026-10-15** | Sprint 1 | Core portal setup: Spring Security RBAC, login/register, H2 file database, user entity, audit logging, admin portal foundation. | ✅ Completed |
| **2026-10-15** | Sprint 2 | Placement calendar, upcoming recruitment drives, dynamic eligibility check engine (CGPA/backlogs/branch), contribution submission & moderation workflow. | ✅ Completed |
| **2026-10-15** | Sprint 3 | Senior alumni mentorship directory, verified interview experiences, round-by-round breakdown, search & filterable interview question bank. | ✅ Completed |
| **2026-10-15** | Docs | Comprehensive README overhaul (`README.md`) featuring ASCII system architecture, ER diagrams, security matrix, and route tables. | ✅ Completed |
| **2026-10-16** | Sprint 4 | Phase 4 AI Preparation Suite: Grounded AI Assistant page (`/ai-assistant`), question classifier, experience summarizer, conversational greeting/typo handler, unit tests (`AiServiceTest`). | ✅ Completed & Deployed |
| **2026-10-16** | Docs | Master Documentation Hub (`DOCUMENTATION.md`) and dedicated `docs/` subsystem tracking ongoing updates. | ✅ Completed |
| **2026-10-20** | Sprint 6 | Placement intelligence bug fixes & hardening: Calendar Seasonality Matrix rendering, Resume Scanner score & checklist parsing, Statistics Average Package card, Mock Interview mentor RBAC and hosting visibility, and Notifications UI overhaul with hourly reminder deduplication. | ✅ Completed & Deployed |
| **2026-10-20** | Sprint 7 | AI Preparation Suite: AI STAR Resume Bullet Enhancer & Job Description Matcher, Apache PDFBox resume text extraction, dynamic ATS keyword scoring, upcoming drive pre-population. | ✅ Completed & Deployed |

---

### Sprint 0: Repository Setup & Git Synchronization
- **Goal**: Establish a clean Git repository, synchronize with the remote GitHub repository ([`palakpk954/Placement_Helper-You_Can-`](https://github.com/palakpk954/Placement_Helper-You_Can-)), and resolve remote divergence cleanly.
- **Actions Taken**:
  - Initialized local Git repository in `c:\Users\HP\Desktop\Placement_Helper`.
  - Added remote origin `https://github.com/palakpk954/Placement_Helper-You_Can-.git`.
  - Staged essential source files, `.gitignore`, and configuration.
  - Fetched and rebased remote `main` branch to integrate the initial GitHub README.
  - Pushed initial commit (`a2ff7e9`).

---

### Sprint 1: Core Portal Architecture & RBAC (Phase 1)
- **Goal**: Build foundational authentication, role authorization, and base data structures.
- **Key Deliverables**:
  - **Spring Security Configuration** (`SecurityConfig.java`): Defined role-based URL matchers (`/admin/**` restricted to `ROLE_ADMIN`, public routes permitted, all other pages requiring authentication).
  - **Domain Entities**: `User`, `Role`, `AuditLog`, `Company`, `Drive`, `Round`.
  - **Authentication Handlers**: Custom authentication success handler redirecting admins to `/admin` and students/alumni to `/dashboard`.
  - **Data Seeder** (`DataSeeder.java`): Preloaded demo accounts (`admin@jecrcu.edu.in`, `rahul.sharma@jecrcu.edu.in`, `alumni.amit@jecrcu.edu.in`) and top recruiter profiles (Microsoft, Google, Amazon, TCS, Infosys, Capgemini).

---

### Sprint 2: Placement Calendar & Dynamic Eligibility Engine (Phase 2)
- **Goal**: Enable students to discover upcoming drives, check their exact placement eligibility in real time, and submit recruitment contributions.
- **Key Deliverables**:
  - **Dynamic Eligibility Engine** (`EligibilityService.java`):
    - Computes real-time student qualification based on CGPA cutoffs, active backlog tolerance, and eligible engineering branches (CSE, IT, ECE, EE, ME, etc.).
    - Returns structured verdicts: `ELIGIBLE`, `CGPA_SHORTFALL`, `BACKLOG_DISQUALIFIED`, or `BRANCH_INELIGIBLE`.
  - **Placement Calendar & Drives** (`CalendarController.java`, `UpcomingDriveRepository.java`): Interactive timeline of scheduled written tests, technical interviews, and HR rounds.
  - **Contribution & Moderation Subsystem** (`ContributeController.java`, `ContributionRepository.java`): Allows students and alumni to submit questions and experiences, queued for admin verification before public display.

---

### Sprint 3: Senior Mentorship & Verified Question Bank (Phase 3)
- **Goal**: Connect students with successfully placed seniors and provide a searchable repository of authentic campus interview questions.
- **Key Deliverables**:
  - **Alumni Mentorship Directory** (`AlumniProfile.java`, `AlumniController.java`): Displays placed alumni mentors filtered by company, domain, and graduation year with direct mentorship request channels.
  - **Interview Experience Hub** (`Experience.java`, `Round.java`): Granular round-by-round documentation (Online Assessment, Technical Round 1, Technical Round 2, HR/Leadership) with tips, difficulty, and outcomes.
  - **Company Directory & Insights** (`CompanyController.java`): Dedicated company overview pages with average packages, highest packages, hiring patterns, and visiting history.

---

### Sprint 4: AI Preparation Suite & Guardrails (Phase 4)
- **Goal**: Build an institutional recruitment intelligence assistant, automated experience summarizer, and question tagger strictly grounded on database records.
- **Key Deliverables**:
  - **`AiService.java` Interface**: Declares `summarizeCompanyExperience()`, `classifyQuestion()`, `answerPrepQuestion()`, and health indicators.
  - **`DefaultAiService.java` Implementation**:
    - **Institutional Grounded Chatbot**: Reads from `CompanyRepository`, `DriveRepository`, `UpcomingDriveRepository`, `QuestionRepository`, and `AlumniProfileRepository` to answer queries regarding packages, rounds, cutoffs, upcoming drives, and top topics.
    - **Conversational Guidance & Greeting Engine**: Intercepts greetings (`hi`, `hello`, `hey`) and polite queries with welcoming capability overviews and clickable prompt suggestions instead of falling back to empty query errors.
    - **Anti-Hallucination Guarantee**: Out-of-domain or unrecorded queries return an honest campus fallback without inventing figures or policies.
    - **Privacy Protection (PRD AI-4)**: Resumes and sensitive personal identifiers are strictly filtered from intelligence pipelines.
    - **Experience Summarizer**: Aggregates verified student reviews into structured takeaways: overall difficulty, key focus areas, round structures, and tips for juniors.
    - **Question Auto-Classifier**: Parses question text to categorize into topics (`DSA`, `DBMS`, `OS`, `Networks`, `System Design`, `HR`) and predicts difficulty (`EASY`, `MEDIUM`, `HARD`).
  - **Frontend Interfaces**:
    - `/ai-assistant` (`templates/ai/assistant.html`): Interactive full-page chat interface with quick suggestion chips, message timestamps, grounded verification badges, and privacy indicators.
    - Company detail synthesis card (`templates/companies/detail.html`): "AI Synthesis: What to Expect" badge on company profile experience tabs.
    - Question submission auto-classifier button (`templates/contribute/form.html`): "✨ AI Classify" button providing one-click topic and difficulty filling.
  - **Testing**:
    - Created [`AiServiceTest.java`](file:///c:/Users/HP/Desktop/Placement_Helper/src/test/java/in/jecrcu/placement/AiServiceTest.java) containing 16 comprehensive unit tests covering greetings, conversational gratitude with typos, acknowledgments, farewells, company queries, upcoming drive lookups, classification heuristics, and fallbacks. All 16 tests pass with 0 errors.

---

### Sprint 5: Live Cloud LLM & Generative RAG (Post-Phase 4 Extra Feature)
- **Goal**: Elevate the institutional AI Placement Assistant with live cloud generative reasoning via Google Gemini API while preserving strict institutional grounding, anti-hallucination guardrails, and zero-downtime offline fallback.
- **Key Deliverables**:
  - **Google Gemini 1.5 Flash Integration**:
    - Direct cloud LLM REST integration via `DefaultAiService.java`.
    - Configured via environment variables / properties: `app.ai.api-key=${GEMINI_API_KEY:${APP_AI_API_KEY:}}`, `app.ai.model=${APP_AI_MODEL:gemini-1.5-flash}`, `temperature=0.2`, `max-tokens=1000`.
    - Robust Jackson `ObjectMapper` payload serialization/deserialization.
  - **Institutional Retrieval-Augmented Generation (RAG)**:
    - Context assembler pulls verified placement records from database: company tiers, latest drive packages/CTC, selection rounds, upcoming recruitment visits, placed alumni mentors, and student interview tips.
    - Strict System Prompt: Explicitly instructs Gemini to answer based strictly on verified records, never fabricate packages or cutoff criteria, and politely advise students when data is unrecorded.
  - **Privacy by Design (PRD AI-4)**:
    - Anonymizes student feedback and strips all PII (names, emails, phone numbers, roll numbers) before passing context to external LLMs.
  - **Zero-Downtime Offline Fallback (PRD AI-5)**:
    - If `GEMINI_API_KEY` is not provided or if the external API encounters network timeouts, rate limits, or quota exhaustion, the engine automatically falls back to the deterministic local grounded synthesis with zero errors.
  - **AI Status & Monitoring Endpoint**:
    - Public REST endpoint `GET /api/ai/status` returning active provider, model name, cloud availability flag, and system health status.
  - **Comprehensive Verification**:
    - Complete test suite passing: **32 tests executed with 0 failures, 0 errors**.

---

### Sprint 6: Placement Intelligence, Role Hardening, Notifications & ATS Polish (2026-10-20)
- **Goal**: Resolve critical UI/UX bugs, enforce strict mentor/admin RBAC on mock interview hosting, eliminate notification spam & harsh styling, and restore full visibility to resume ATS scoring and placement analytics.
- **Key Deliverables**:
  - **Mock Interview RBAC & Portal Visibility**:
    - Restricted "Offer a Mock Interview Slot" creation form and `+ Host a Mock Slot` button strictly to verified placed seniors / alumni mentors (`AlumniProfile`) and placement cell admins (`ROLE_ADMIN`).
    - Standard students now only see booking capabilities. Added backend authorization check rejecting unprivileged slot creation attempts.
    - Integrated direct "Host Mock Slots" callouts in the Alumni Mentors directory (`/alumni`) for active mentors, and added a quick link under Admin Tools in the navbar.
    - Fixed `MockBooking.getSlot()` property evaluation to resolve template SpEL parsing exceptions.
  - **Notification Subsystem Redesign & Deduplication**:
    - Eliminated gaudy 4px solid blue square bounding boxes around unread items; replaced with modern cards featuring subtle left accents (`border-left: 4px solid var(--bs-primary)`), soft background tints, and "New" badges.
    - Added category icon bubbles for Mock Interviews (calendar), Deadlines (alarm), Recruitment Drives (megaphone), and Approvals (patch-check).
    - Fixed `View →` button wrapping into two lines by applying `text-nowrap`.
    - Added direct route `GET /notifications/{id}/open` automatically marking alerts as read and forwarding the student to the target destination.
    - Added filter pills ("All" / "Unread") and inline buttons to mark single alerts as read or dismiss them.
    - Prevented hourly duplicate alerts in `NotificationService.checkDeadlineReminders()` by checking `!existsByUserIdAndNotificationTypeAndTitle(...)`.
  - **Resume Scanner & Score Rendering**:
    - Implemented transient JSON getters on `Resume.java` (`getScore()`, `getPageCount()`, `isHasContactInfo()`, etc.) and controller data attributes so ATS score, page count, and contact checklist details properly render in the UI.
  - **Placement Calendar Seasonality Matrix**:
    - Fixed SpEL map access in `calendar/index.html` from `entry.value[y]` to `entry.value.get(y)`, resolving empty 0-count drive matrix cells across years.
  - **Statistics Average Package Metric**:
    - Implemented real-time dynamic average CTC computation in `StatsService` and added a dedicated 5th KPI metric card on the statistics dashboard (`/statistics`).

---

### Sprint 7: AI Preparation Suite — Resume Bullet Enhancer & Job Description Matcher
- **Goal**: Empower students with self-service AI tools to optimize their resumes for applicant tracking systems (ATS) and match keywords directly against scheduled campus recruitment drives.
- **Key Deliverables**:
  - **AI STAR Resume Bullet Enhancer**:
    - Engineering domain selector (Full-Stack, Backend/Systems, Frontend/UI, Data Science & AI, Cloud & DevOps, Core CS).
    - Heuristic weakness diagnosis detecting passive/generic verb phrasing ("worked on", "helped", "did") and absence of quantifiable metrics (`%`, latency, user volume).
    - Generates 3 ATS-tailored STAR rewrites per bullet: *Metric-Driven*, *Technical Depth*, and *Concise One-Liner*.
    - Provides before/after score progression (e.g. 45 -> 94 / 100) and interview coaching tips explaining how to articulate the bullet during real rounds.
    - 1-click clipboard copy buttons with dynamic "Copied!" feedback.
  - **Job Description & Recruiter Keyword Matcher**:
    - Automatic PDF text extraction via Apache PDFBox `PDFTextStripper` (`ResumeService.extractResumeText()`).
    - Quick-select dropdown pre-filling job requirements from active upcoming drives (e.g. Celebal, Microsoft, Deloitte) or custom pasted JDs.
    - Tokenizes and cross-references against a 70+ technical skill catalog (languages, frameworks, cloud platforms, databases, DevOps tools).
    - Calculates real-time ATS match percentage with dynamic progress bar and color-coded status badges (`Strong ATS Match`, `Moderate Match`, `Low Match`).
    - Visual breakdown highlighting matched skills (green pill badges) and missing recruiter requirements (red '+ skill' pill badges) with actionable suggestions.
  - **Architecture & Resilient Fallback (AI-5)**:
    - Dual-engine support: live cloud LLM (Google Gemini `gemini-1.5-flash`) with automatic, zero-latency deterministic fallback if API keys or quotas are unavailable.
    - Clean separation of concerns with dedicated REST endpoint controller `ResumeApiController` (`/api/resume/enhance-bullet` and `/api/resume/match-jd`).
    - Exemption of `/api/**` in Spring Security CSRF filter for seamless AJAX communication.
    - 100% unit and integration test coverage with 37 tests passing across the suite.

---

## 🔧 Troubleshooting & Bug Fix Journal

During development and continuous verification, the following technical issues were identified, analyzed, and resolved:

### 1. Spring Boot Port 8080 Process Contention
- **Symptom**: New endpoints returned 404 or old page states, and `mvnw spring-boot:run` failed with `BindException: Address already in use: 8080`.
- **Root Cause**: A previously launched background Java process (PID `7216`) from an earlier IDE session was holding port `8080`.
- **Resolution**: Identified listening process via `netstat -ano | findstr :8080` and terminated it using `Stop-Process -Id 7216 -Force`, freeing port 8080 for the clean application instance.

### 2. Eclipse/IDE Bytecode Mismatch (`ClassFormatError` on `AuditLog.class`)
- **Symptom**: Compilation or startup failed citing invalid or unrecognized class file attributes on `target/classes/in/jecrcu/placement/model/AuditLog.class`.
- **Root Cause**: Stale `.class` files compiled with a different JDK/IDE toolchain lingered in the Maven target directory.
- **Resolution**: Executed a clean compilation cycle via `./mvnw clean compile`, rebuilding all classes under Java 17.

### 3. Entity Accessor Method Alignment
- **Symptom**: Compilation errors during early AI service implementation when calling `.getRoundOrder()` and `.getRoleName()`.
- **Root Cause**: `Round` entity declared `getRoundNumber()` and `getName()`. `AlumniProfile` declared `getJobRole()`.
- **Resolution**: Aligned all getter calls in `DefaultAiService.java` to match the exact JPA model signatures.

### 4. Chatbot Greeting Query Fallback ("hi" query returning no-data message)
- **Symptom**: Typing "hi" into the AI Placement Assistant caused it to respond: *"I don't have verified data on that in our campus recruitment records."*
- **Root Cause**: The strict anti-hallucination filter checked if the query matched a known company or topic keyword. Generic conversational pleasantries had no keyword match, triggering the ungrounded fallback.
- **Resolution**: Introduced dedicated greeting regex patterns (`\b(hi|hello|hey|greetings|good morning|who are you)\b`) in `DefaultAiService.java` that return a warm, structured overview of capabilities with sample query prompts.

### 5. API Authentication & CSRF Filter Rules
- **Symptom**: Unauthenticated command-line POST requests to `/api/ai/chat` were redirected to `/login`.
- **Root Cause**: `SecurityConfig.java` configured `.anyRequest().authenticated()` while only exempting `/api/**` from CSRF.
- **Resolution**: Confirmed that web clients use authenticated session cookies for `/api/ai/**` routes, ensuring full user audit tracking while protecting against malicious unauthorized scraping.

### 6. Conversational Gratitude with Typos & Casual Acknowledgments ("ok thank you so muhc")
- **Symptom**: Typing casual appreciation like *"ok thank you so muhc"* or acknowledgments like *"got it"* triggered the strict recruitment fallback *"I don't have verified data on that in our campus recruitment records."*
- **Root Cause**: The gratitude check previously used a rigid anchored regular expression (`^(thanks|thank you)$`) which failed when prefixed with *"ok"* or when containing common user typos (e.g., *"muhc"*, *"thnx"*).
- **Resolution**: Implemented flexible token matching in `DefaultAiService.java` (`isGratitude()`, `isAcknowledgment()`, `isFarewell()`, `isGreeting()`) that accommodates conversational prefixes, typos, and acknowledgments while preserving strict database grounding whenever recruiter or company terms are referenced. Added 4 dedicated unit tests in `AiServiceTest.java` (now 12 passing tests).

### 7. Render Cloud Native Java Runtime Missing (`bash: java: command not found`)
- **Symptom**: Initial deployment on Render failed with status 127: `bash: line 1: java: command not found`.
- **Root Cause**: Render does not offer native runtime environments for JVM languages (it only natively supports Node.js, Python, Ruby, Go, Rust). Running `java -jar ...` in a native Linux container fails because no JDK/JRE is installed.
- **Resolution**: Created a production-grade multi-stage `Dockerfile` based on official `maven:3.9.8-eclipse-temurin-21` and `eclipse-temurin:21-jre-jammy` with a non-root application user, Render 512MB RAM memory limits (`-Xmx384m`), and dynamic `$PORT` binding. Switched Render web service runtime to `Docker`.

### 8. 100% Desktop Zoom Layout Disconnect & User Profile Navbar Clipping
- **Symptom**: On standard 1080p and laptop screens at 100% zoom, the navbar horizontally overflowed, pushing the user profile dropdown off-screen and leaving the red notification badge touching the browser edge. The page cards were centered in a narrow container while the navbar stretched 100%, causing a visual disconnect.
- **Root Cause**: 
  1. The navbar had 8 individual top-level links plus 4 action buttons, exceeding ~1350px width.
  2. The navbar used `container-fluid px-lg-4` (edge-to-edge) while dashboard cards used `container` (max 1320px).
  3. Action buttons had no `flex-shrink-0`, allowing the user avatar to be clipped on window boundaries.
- **Resolution**:
  1. Unified both navbar and page layouts to use `container-xxl px-3 px-md-4` for perfect left-and-right symmetrical alignment.
  2. Consolidated `Stats` under the *Community* dropdown (`Placement Stats`), reducing top-level link crowding.
  3. Reduced `.nav-link` padding (`0.35rem 0.55rem`) and font size (`0.86rem`).
  4. Added `flex-shrink: 0; ms-auto` to the right action bar and made username text responsive (`d-none d-xxl-inline`), guaranteeing the user avatar circle and notification bell never clip.
  5. Compacted `.stat-widget` padding, icon dimensions (`2.5rem`), and grid gaps (`g-3`), keeping *Upcoming Campus Drives* and *Practice Tracker* immediately visible above the fold.

### 9. Seasonality Matrix 0-Count Drives in Placement Calendar
- **Symptom**: On the Placement Calendar (`/calendar`), the recruitment seasonality matrix table displayed 0 drives across all months and years despite verified recruitment drives existing in the database.
- **Root Cause**: The Thymeleaf template evaluated map lookup as `entry.value[y]`. In Spring Expression Language (SpEL), evaluating `[y]` where `y` is an integer loop variable failed or looked up `null`.
- **Resolution**: Changed expression in `frontend/templates/calendar/index.html` to `entry.value.get(y)`. This accurately resolves the nested map entry and renders exact drive tallies.

### 10. Resume Scanner Missing ATS Score & Checklist Flags
- **Symptom**: On the Resume Scanner page (`/resume`), uploaded resumes did not display ATS scores, page count, or contact information flags.
- **Root Cause**: `Resume.java` stored raw JSON in `checklistResultJson` without getter methods for computed properties (`getScore()`, `getPageCount()`, `isHasContactInfo()`, etc.), causing Thymeleaf property resolution to return empty strings.
- **Resolution**: Added transient JSON parser getters on `Resume.java` and populated model attributes in `ResumeController.java` to extract and display ATS score, keyword matching, and checklist items.

### 11. Placement Statistics Missing Average Package Metric
- **Symptom**: The Placement Statistics dashboard (`/statistics`) displayed Highest Package, Placed Students, Recruiters, and Drives, but lacked the critical Institutional Average Package metric card.
- **Root Cause**: `StatsService.java` aggregated summary statistics without computing an average CTC across drives with valid package figures.
- **Resolution**: Implemented dynamic average CTC calculation in `StatsService.java` (filtering drives with positive CTC, averaging values, formatting to 2 decimal places) and added a dedicated 5th KPI card to `statistics/index.html`.

### 12. Mock Interview Slot Creation Exposed to Students & SpEL Property Error
- **Symptom**: Logged-in regular students saw the "Offer a Mock Interview Slot" creation form and `+ Host a Mock Slot (Mentors)` button on `/mocks`. Clicking "View" on confirmed mock notifications threw `SpelEvaluationException: EL1008E: Property or field 'slot' cannot be found on object of type MockBooking`.
- **Root Cause**:
  1. The slot creation form had no role-based visibility restrictions in `mocks/index.html`.
  2. `MockBooking.java` named the field `mockSlot`, but `mocks/index.html` attempted to access `b.slot`.
- **Resolution**:
  1. Enclosed slot hosting controls in `th:if="${canHostSlots}"` computed as `isAdmin || isMentor` (verified via `AlumniProfile`).
  2. Added server-side permission checks in `MockInterviewController.createSlot()` rejecting unprivileged POST requests.
  3. Added `getSlot()` accessor to `MockBooking.java` and aligned template bindings to `b.mockSlot`.
  4. Added direct mentor slot hosting buttons in the Alumni Mentors directory (`/alumni`) and Admin Tools.

### 13. Notification Card Heavy 4px Blue Bounding Boxes & Hourly Duplicate Spam
- **Symptom**: Notifications on `/notifications` displayed with harsh, thick 4px blue rectangular bounding boxes on all four sides. The same "Deadline Reminder: Celebal Technologies" alert appeared repeatedly. The `View →` button wrapped awkwardly into `View` and `–`.
- **Root Cause**:
  1. `border-primary border-4` on `.list-group-item` set `border-color` and `border-width` on all four borders.
  2. The hourly `@Scheduled(cron = "0 0 * * * *")` deadline reminder task inserted a new notification every hour without checking if the student was already notified.
  3. `View →` lacked `text-nowrap`.
  4. Viewing an alert did not mark it as read.
- **Resolution**:
  1. Replaced heavy borders with modern cards featuring subtle left accents (`border-left: 4px solid var(--bs-primary)`) and category icons.
  2. Added deduplication check `!existsByUserIdAndNotificationTypeAndTitle(...)` in `NotificationService.checkDeadlineReminders()` and cleaned historical duplicates in `DataInitializer`.
  3. Added `text-nowrap` to prevent button text wrapping.
  4. Added `GET /notifications/{id}/open` endpoint automatically marking alerts as read on view.
  5. Added "All" / "Unread" filter pills and inline buttons to mark single alerts as read or dismiss them.

### 14. Spring MVC Class-Level `@RequestMapping("/resume")` API Route Nesting
- **Symptom**: Calling `POST /api/resume/enhance-bullet` or `POST /api/resume/match-jd` returned `404 Not Found`.
- **Root Cause**: The endpoints were initially declared inside `ResumeController.java`, which had a class-level `@RequestMapping("/resume")` annotation. Spring MVC automatically prepends class-level mappings, causing the routes to resolve to `/resume/api/resume/enhance-bullet`.
- **Resolution**: Refactored the architecture by extracting all resume intelligence JSON endpoints into a dedicated `@RestController` class `ResumeApiController.java` mapped cleanly to `@RequestMapping("/api/resume")`. Updated unit and integration test scripts to verify 200 OK responses.

---

## 🏛 System Architecture & Module Directory

### Project File Organization
```
Placement_Helper/
├── .mvn/                                # Maven wrapper configuration
├── data/                                # Embedded H2 database storage
│   └── youcan.mv.db                     # Persistent local database file
├── docs/                                # Detailed technical documents & guides
│   ├── ARCHITECTURE.md                  # Deep architectural blueprints
│   ├── CHANGELOG.md                     # Detailed release & commit notes
│   └── OPERATIONAL_RUNBOOK.md           # Deployment & configuration runbook
├── src/
│   ├── main/
│   │   ├── java/in/jecrcu/placement/
│   │   │   ├── YouCanApplication.java  # Application entrypoint
│   │   │   ├── config/                  # Security & web mvc configurations
│   │   │   │   ├── CustomUserDetails.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── DataSeeder.java      # Initial demo dataset seeder
│   │   │   │   └── SecurityConfig.java  # Spring Security 6 RBAC rules
│   │   │   ├── controller/              # MVC & REST web controllers
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AiController.java    # AI chat, summarizer, & classifier
│   │   │   │   ├── AlumniController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CalendarController.java
│   │   │   │   ├── CompanyController.java
│   │   │   │   ├── ContributeController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── ExperienceController.java
│   │   │   ├── model/                   # JPA domain entities
│   │   │   │   ├── AlumniProfile.java
│   │   │   │   ├── AuditLog.java
│   │   │   │   ├── Company.java
│   │   │   │   ├── Contribution.java
│   │   │   │   ├── Drive.java
│   │   │   │   ├── Experience.java
│   │   │   │   ├── Question.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Round.java
│   │   │   │   ├── UpcomingDrive.java
│   │   │   │   └── User.java
│   │   │   ├── repository/              # Spring Data JPA repositories
│   │   │   └── service/                 # Core business services
│   │   │       ├── EligibilityService.java
│   │   │       └── ai/                  # AI intelligence engine
│   │   │           ├── AiService.java
│   │   │           └── DefaultAiService.java
│   │   └── resources/
│   │       ├── application.properties   # App configuration & H2 settings
│   │       ├── static/                  # Client-side static assets
│   │       │   ├── css/styles.css       # Unified design system stylesheet
│   │       │   └── js/app.js            # Frontend interactivity scripts
│   │       └── templates/               # Thymeleaf HTML views
│   │           ├── admin/               # Admin dashboard & management
│   │           ├── ai/                  # AI assistant chat interface
│   │           ├── auth/                # Login, register, password reset
│   │           ├── calendar/            # Recruitment calendar & timeline
│   │           ├── companies/           # Company profiles & round breakdown
│   │           ├── contribute/          # Experience & question contribution
│   │           ├── dashboard/           # Student personalized dashboard
│   │           ├── experiences/         # Student interview experiences
│   │           ├── layout/              # Base layout & navigation partials
│   │           └── mentors/             # Placed alumni directory
│   └── test/
│       └── java/in/jecrcu/placement/
│           ├── AiServiceTest.java       # AI engine unit test suite
│           └── EligibilityServiceTest.java # Eligibility matrix tests
├── DOCUMENTATION.md                     # Master Living Documentation (this file)
├── PRD University Placement Portal.md   # Product Requirements Document
├── README.md                            # Public repository overview
└── pom.xml                              # Maven build specifications
```

---

## 📡 API & Endpoint Reference

### Public & Authentication Routes
| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `GET` | `/login` | Public | Sign-in page with 1-click demo credential buttons |
| `POST` | `/login` | Public | Standard form login processing |
| `GET` | `/register` | Public | University student / alumni registration |
| `POST` | `/register` | Public | Account creation with university domain validation |
| `GET` | `/logout` | Authenticated | Invalidates session and clears `JSESSIONID` |

### Core Portal Views
| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `GET` | `/dashboard` | `STUDENT`, `ALUMNI` | Personalized student dashboard with eligibility indicators |
| `GET` | `/calendar` | Authenticated | Placement calendar with upcoming recruitment drives |
| `GET` | `/companies` | Authenticated | Recruiter directory with packages, visits, and criteria |
| `GET` | `/companies/{id}` | Authenticated | Deep-dive company profile with round structure & AI synthesis |
| `GET` | `/experiences` | Authenticated | Verified interview reviews submitted by seniors |
| `GET` | `/mentors` | Authenticated | Senior alumni directory with role and company filters |
| `GET` | `/contribute` | Authenticated | Submission form for interview questions and experiences |
| `GET` | `/ai-assistant` | Authenticated | Full-page interactive AI Placement Assistant |

### AI REST Endpoints
| Method | Endpoint | Request Payload | Response | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/ai/chat` | `{"message": "string"}` | `{"answer": "string", "grounded": bool, "sources": []}` | Grounded placement Q&A chatbot response |
| `POST` | `/api/ai/classify` | `{"questionText": "string"}` | `{"topic": "string", "difficulty": "string"}` | Auto-tags question topic & technical difficulty |
| `GET` | `/api/ai/company-summary/{id}` | None | `{"summary": "string", "companyName": "string"}` | Synthesizes all student experiences for a company |

### Administrative Routes
| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `GET` | `/admin` | `ROLE_ADMIN` | Executive placement metrics, drive statistics, and audit logs |
| `GET` | `/admin/drives` | `ROLE_ADMIN` | Management interface to schedule and update drives |
| `GET` | `/admin/contributions` | `ROLE_ADMIN` | Approval queue for student questions and experiences |

---

## 💾 Database Schema & Data Model

The application uses an embedded **H2 Database** configured with JPA entities and automatic schema updates (`spring.jpa.hibernate.ddl-auto=update`).

```
 +------------------+           +------------------+
 |       User       |           |     Company      |
 +------------------+           +------------------+
 | id (PK)          |           | id (PK)          |
 | email (UQ)       |           | name (UQ)        |
 | password         |           | tier             |
 | fullName         |           | website          |
 | role (ENUM)      |           | minCgpa          |
 | branch           |           | maxBacklogs      |
 | cgpa             |           | packageRange     |
 | backlogs         |           +--------+---------+
 +--------+---------+                    | 1
          | 1                            |
          |                              | N
          | N                   +--------v---------+
 +--------v---------+           |      Drive       |
 |  AlumniProfile   |           +------------------+
 +------------------+           | id (PK)          |
 | id (PK)          |           | company_id (FK)  |
 | company          |           | driveDate        |
 | jobRole          |           | batchYear        |
 | packageOffered   |           | status           |
 | linkedInUrl      |           +--------+---------+
 +------------------+                    | 1
                                         | N
                                +--------v---------+
                                |      Round       |
                                +------------------+
                                | id (PK)          |
                                | drive_id (FK)    |
                                | roundNumber      |
                                | name             |
                                | roundType        |
                                | description      |
                                +------------------+
```

---

## 🛡 Security & Privacy Governance

1. **Role-Based Authorization**:
   - `ROLE_ADMIN`: Complete access to `/admin/**`, drive creation, eligibility overrides, and contribution approvals.
   - `ROLE_STUDENT`: Access to portal dashboard, eligibility checks, drive calendars, question banks, and AI prep assistant.
   - `ROLE_ALUMNI`: Permissions to submit verified experiences, manage mentor profiles, and answer student inquiries.
2. **Password Security**: Passwords are encrypted using Spring Security's `BCryptPasswordEncoder` with default work factor 10.
3. **Strict Privacy Scrubbing (PRD AI-4)**:
   - Student contact numbers, personal emails, and raw resume files are never exposed in AI prompts or public templates.
   - Only approved, anonymized interview experiences and technical question texts are synthesized by the AI engine.
4. **Data Grounding Guarantee (PRD AI-3)**:
   - The AI Assistant strictly checks verified database entities before generating answers.
   - If an unverified recruiter or unknown policy is queried, the engine explicitly alerts the student that no institutional record exists rather than hallucinating figures.

---

## 🚀 Developer Runbook & Testing Guide

### Prerequisites
- **JDK**: Java Development Kit 17 or higher (`java -version`)
- **Maven**: Bundled Maven wrapper (`./mvnw` or `mvnw.cmd`)
- **Browser**: Modern web browser (Chrome, Edge, Firefox)

### Running Locally
```powershell
# Navigate to project directory
cd c:\Users\HP\Desktop\Placement_Helper

# Run with Spring Boot Maven plugin
./mvnw spring-boot:run
```
The application will launch on **`http://localhost:8080`**.

### Accessing the Database Console
1. Navigate to: `http://localhost:8080/h2-console`
2. Enter the JDBC parameters:
   - **JDBC URL**: `jdbc:h2:file:./data/youcan`
   - **User Name**: `sa`
   - **Password**: *(leave blank)*
3. Click **Connect** to query raw tables, audit logs, and seeded records.

### Running Automated Test Suites
```powershell
# Run the entire test suite
./mvnw test

# Run AI service unit tests specifically
./mvnw test -Dtest=AiServiceTest

# Run eligibility engine unit tests specifically
./mvnw test -Dtest=EligibilityServiceTest
```

### Pre-Configured Demo Credentials
For testing convenience, the portal includes preloaded demo accounts accessible with 1-click buttons on the `/login` screen:

| Role | Email | Password | Pre-seeded Profile Details |
| :--- | :--- | :--- | :--- |
| **Placement Admin** | `admin@jecrcu.edu.in` | `admin123` | Institutional Admin Authority |
| **Pre-final Student** | `rahul.sharma@jecrcu.edu.in` | `student123` | B.Tech CSE, 8.42 CGPA, 0 Backlogs |
| **Placed Alumnus** | `alumni.amit@jecrcu.edu.in` | `mentor123` | Software Engineer at Microsoft (44 LPA) |

---

## 🔮 Future Roadmap

- [ ] **External LLM Toggle**: Add optional configuration keys in `application.properties` to connect `AiService` to Google Gemini API or OpenAI API when internet access is enabled.
- [ ] **Resume ATS Scanner**: Module allowing students to upload resumes for automated keyword matching against specific recruiter job descriptions.
- [ ] **Automated Email Notifications**: Spring Mail integration sending instant reminders 24 hours prior to scheduled recruitment drives.
- [x] **Docker Containerization**: Multi-stage `Dockerfile` and `.dockerignore` optimized for Render & cloud platforms.
- [ ] **Docker Compose**: `docker-compose.yml` for unified local multi-service container orchestration.

---

## 📝 How to Maintain & Update This Documentation

To ensure this documentation stays accurate as the project evolves, adhere to the following conventions:

1. **When adding a new feature or phase**:
   - Add a row in the [Living Development Log & Sprint History](#-living-development-log--sprint-history) table with the sprint date, phase name, and status.
   - Write a dedicated subsection detailing the goals, architecture, and deliverables.
2. **When resolving a bug or resolving an environment issue**:
   - Record the symptom, root cause, and precise resolution in the [Troubleshooting & Bug Fix Journal](#-troubleshooting--bug-fix-journal).
3. **When adding new endpoints or controllers**:
   - Update the [API & Endpoint Reference](#-api--endpoint-reference) table with the HTTP method, path, role requirements, and description.
4. **When updating database entities**:
   - Update the ASCII schema diagram and entity relationship breakdown in [Database Schema & Data Model](#-database-schema--data-model).

---
*Maintained by the You_can Engineering Team • JECRC University Placement Portal*
