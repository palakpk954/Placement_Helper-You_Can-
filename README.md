# You_can — Campus Placement Helper & Intelligence Portal

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-green.svg)](https://www.thymeleaf.org/)
[![HTMX](https://img.shields.io/badge/HTMX-Instant%20Search-blue.svg)](https://htmx.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple.svg)](https://getbootstrap.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Compatible-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Academic%20Use-lightgrey.svg)]()

> **You_can** is a production-grade campus recruitment archive, preparation intelligence suite, and alumni mentorship web platform designed for university students, placed alumni, and placement cell administrators.

---

## 📌 Problem Solved

Placement preparation at most universities is fragmented: interview experiences get lost in WhatsApp chats, eligibility rules are hard to track, juniors struggle to contact placed seniors, and historical selection patterns disappear across batches. 

**You_can** solves this by establishing a single trusted portal to archive past recruitment drives, evaluate real-time drive eligibility, practice verified interview questions, generate prep roadmaps, book alumni mock interviews, and empower the placement cell with audit logs and bulk data exports.

---

## 🚀 Key Features by Project Phase

```
                                      YOU_CAN PLATFORM ARCHITECTURE
 ┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
 │                                              Web Browser                                               │
 │                         (Bootstrap 5.3 + Custom Design Tokens + HTMX + Dark/Light Theme)               │
 └───────────────────────────────────────────────────┬────────────────────────────────────────────────────┘
                                                     │ HTTP / REST
 ┌───────────────────────────────────────────────────▼────────────────────────────────────────────────────┐
 │                                   Spring Boot 3.x Web Application                                     │
 │  ┌───────────────────────┬────────────────────────┬───────────────────────┬─────────────────────────┐  │
 │  │      Controllers      │    Security & Auth     │       Services        │       Repositories      │  │
 │  │ (Company, Prep, Admin)│   (BCrypt, Roles, CSRF)│ (Drive, Stats, AI...) │   (Spring Data JPA)     │  │
 │  └───────────────────────┴────────────────────────┴───────────────────────┴─────────────────────────┘  │
 └───────────────────────────────────────────────────┬────────────────────────────────────────────────────┘
                                                     │ JPA / Flyway
 ┌───────────────────────────────────────────────────▼────────────────────────────────────────────────────┐
 │                                          Database Layer                                                │
 │                           PostgreSQL (Production) / H2 Postgres Mode (Dev)                             │
 └────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

### 🏛️ Phase 1: Core Recruitment Archive & Administration
- **Company Directory & Smart Search**: Real-time searching and multi-faceted filtering (by Year, Month, Industry, Role, Job Type, Branch Eligibility, and CTC Range) powered by HTMX instant updates without page reloads.
- **Company Deep-Dive Pages**: Detailed visit timelines, selection rounds (Online Assessment, Technical, HR, Coding), package breakdowns (base, stipend, bond, work mode), and approved student interview experiences with upvoting.
- **Placement Calendar**: Month-by-month recruitment calendar and multi-year visit frequency heatmaps.
- **Statistics Dashboard**: Interactive charts tracking annual visiting recruiters, CTC distribution (highest, median, average), branch-wise selections, and top question topics.
- **Experience Contribution Workflow**: Guided submission form capturing rounds, questions, tips, and mistakes to avoid, with full **anonymous author support** (names hidden from public UI and data exports).
- **Placement Cell Admin Panel**:
  - Full CRUD control over companies, drives, rounds, and interview questions.
  - Moderation queue to review, approve, or reject student submissions.
  - Bulk CSV/Excel data import with dry-run preview and row-level validation errors before saving.
- **Comparison & Checklists**: Side-by-side comparison of any two companies (rounds, eligibility, difficulty, CTC) and auto-generated preparation checklists.

### 🎯 Phase 2: Preparation & Community Suite
- **Upcoming Recruitment Drives**: Real-time notifications for scheduled visits, CTC packages, deadlines, direct registration links, and one-click `.ics` calendar sync.
- **Automated Eligibility Evaluator & "What-If" Simulator**:
  - Instantly checks whether a student meets CGPA cutoffs, allowed branches, and max backlog limits.
  - Interactive "What-If" simulator allowing students to test future CGPA scores or branch switches to check changing eligibility.
- **Campus Question Bank**: Searchable repository of real interview questions categorized by topic, difficulty, company, and round type.
- **Interactive Practice Mode**:
  - Practice questions one by one with expandable hints and answers.
  - Mark questions as *Solved*, *Unsure*, or *Skipped* with visual topic progress tracking.
- **Personalized 1-to-8 Week Prep Roadmaps**: Generates custom week-by-week study roadmaps for target companies with checklist progress tracking.
- **Resume Scanner & Checklist**: PDF resume uploader (with size/type validation) and checklist scanning for contact details, projects, skills, and length.
- **Alumni Mentors & Mock Interviews**:
  - Opt-in alumni directory highlighting placed roles, LinkedIn profiles, and areas of guidance.
  - Mentor slot calendar where juniors book 1-on-1 mock interviews with confirmation tracking.
- **Student Dashboard & Gamification**:
  - Centralized student cockpit displaying eligible upcoming drives, saved bookmarks, practice statistics, and notification alerts.
  - Gamification with achievement badges (*First Contributor*, *Master Solver*, *Community Mentor*) and a yearly contributor leaderboard.

### 🛡️ Phase 3: Quality, Governance & Reporting
- **Placement Cell Audit Trail**: Comprehensive audit log recording creation, edits, deletions, and approval actions with actor and timestamp.
- **Security & Rate Limiting**: In-memory sliding window rate limiting on authentication attempts, submissions, and comments.
- **Duplicate Detection**: Flags near-duplicate or spam submissions for administrator review.
- **Executive Report Exports**: One-click generation and export of official placement reports in **Excel (`.xlsx`)** and **PDF** formats.

### 🤖 Phase 4: AI Preparation Layer
- **AI-1: Company Experience Summarizer**: Synthesizes verified student experiences into a concise "What to Expect" summary covering key interview focus areas, typical round progressions, and recurring pitfalls.
- **AI-2: Question Classifier**: Intelligent topic and difficulty classifier that auto-detects concepts (DSA, DBMS, OS, Networks, System Design, HR) with one-click suggestion.
- **AI-3: Grounded Prep Chatbot**: Answers student preparation questions strictly grounded in the portal's verified campus records (with explicit guardrails to avoid hallucinations).
- **AI-4 & AI-5: Privacy & Offline Fallback**: Zero student personal data/resumes are transmitted; features function in offline heuristic mode and optionally plug into Google Gemini (`gemini-1.5-flash`).

### 📱 UI/UX & Mobile-First Design
- **Curated Theme System**: Deep Royal Indigo & Slate palette with dark and light mode toggle persisted in local storage.
- **Dedicated Mobile Bottom Bar**: Ergonomic touch navigation on mobile viewports (`< 768px`) for immediate access to Home, Companies, Drives, Practice, and Eligibility.
- **Collapsible Filter Drawers**: Mobile-friendly filter toggles to prevent endless scrolling on small phone screens.
- **Micro-Interactions**: Floating back-to-top button, keyboard shortcuts (`Ctrl+K` or `/` for global search), and card hover elevations.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Language & Runtime** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.4.3 (Web, Data JPA, Security, Mail, Actuator, Validation) |
| **Template Engine** | Thymeleaf + Thymeleaf Extras Spring Security 6 |
| **Frontend Dynamics** | HTMX 1.9, Vanilla JavaScript (`app.js`), Bootstrap 5.3, Bootstrap Icons |
| **Database** | PostgreSQL (Production) / H2 Database (Local Dev in PostgreSQL mode) |
| **Database Migrations** | Flyway (`classpath:db/migration`) |
| **File & Document Processing** | Apache POI 5.3 (Excel), OpenCSV 5.9 (CSV), OpenPDF 2.0 (PDF Reports), Apache PDFBox 3.0 (Resume parsing) |
| **Build & Dependency Tool** | Apache Maven 3.x (with Maven Wrapper `./mvnw`) |

---

## 📂 Project Directory Structure

```text
Placement_Helper/
├── .mvn/wrapper/                 # Maven wrapper binaries
├── data/                         # Local database storage (git-ignored)
├── uploads/                      # Uploaded resumes and assets (git-ignored)
├── src/
│   ├── main/
│   │   ├── java/in/jecrcu/placement/
│   │   │   ├── YouCanApplication.java       # Application entry point
│   │   │   ├── config/                      # Security, DataInitializer, CustomUserDetails
│   │   │   ├── controller/                  # Web and REST controllers (Admin, Company, Practice...)
│   │   │   ├── entity/                      # JPA Entities (Company, Drive, Question, Experience...)
│   │   │   │   └── enums/                   # Status, Role, Difficulty, RoundType enums
│   │   │   ├── repository/                  # Spring Data JPA repositories
│   │   │   └── service/                     # Business logic, Rate limiting, Audit, Exports, AI
│   │   │       └── ai/                      # AiService interface & DefaultAiService implementation
│   │   └── resources/
│   │       ├── application.properties       # Environment configuration & feature toggles
│   │       ├── db/migration/                # Flyway SQL migrations (V1 schema, V2 seed, V3 drives)
│   │       ├── static/                      # CSS (styles.css), JS (app.js), icons, images
│   │       └── templates/                   # Thymeleaf templates (layout, admin, auth, companies...)
│   └── test/
│       └── java/in/jecrcu/placement/        # Service, integration, and security test suites
├── pom.xml                                  # Maven dependencies and build plugins
├── PRD University Placement Portal.md       # Complete Product Requirements Document
└── README.md                                # Platform overview & documentation
```

---

## 💻 Getting Started Locally

### Prerequisites
- **Java 21** or higher installed (`java -version`)
- Git installed (`git --version`)

### 1. Clone the Repository
```bash
git clone https://github.com/palakpk954/Placement_Helper-You_Can-.git
cd Placement_Helper-You_Can-
```

### 2. Run the Application
You can launch the application directly using the included Maven wrapper:

```bash
# On Linux / macOS:
./mvnw spring-boot:run

# On Windows (PowerShell / Command Prompt):
./mvnw.cmd spring-boot:run
```

The application will start on:
👉 **`http://localhost:8080`**

*(Note: On first startup, Flyway automatically runs database migrations and seeds initial historical campus companies, drives, questions, and sample user accounts.)*

---

## 🔐 Default Demo Accounts

The portal comes pre-seeded with role-based accounts for instant testing:

| Role | Email Address | Password | Permissions |
|---|---|---|---|
| **Placement Admin** | `admin@jecrcu.edu.in` | `admin123` | Full access to `/admin/**`, approvals, audit log, imports, and drive creation |
| **Student** | `rahul.sharma@jecrcu.edu.in` | `student123` | Student dashboard, upcoming drives, practice mode, roadmaps, bookmarking |
| **Placed Senior / Mentor** | `alumni.amit@jecrcu.edu.in` | `mentor123` | Experience author, alumni profile, mock interview slot management |

*(New student accounts can also be created via `/register` using any approved university domain e.g., `@jecrcu.edu.in` or `@university.edu`.)*

---

## ⚙️ Configuration Properties

Key configuration settings in `src/main/resources/application.properties`:

| Property | Default Value | Description |
|---|---|---|
| `server.port` | `8080` | HTTP web server port |
| `spring.datasource.url` | `jdbc:h2:file:./data/youcan...` | Local database (easily swapped with PostgreSQL in production) |
| `app.security.allowed-domains` | `jecrcu.edu.in,university.edu` | Restricts registration to verified university email addresses |
| `app.ai.enabled` | `false` | Toggles AI features (Phase 4) on or off |
| `app.ai.api-key` | *(optional)* | Google Gemini API key for live LLM synthesis |
| `app.ai.model` | `gemini-1.5-flash` | AI model target |

---

## 📄 License & Attribution

Designed and developed for **JECRC University** campus placement preparation.  
Created for academic and university recruitment administration use.
