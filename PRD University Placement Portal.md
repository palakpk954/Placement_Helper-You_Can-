# Product Requirements Document: University Placement Portal

Version 1.0 · Draft · October 15, 2026 · Owner: Soumil Agarwal · Institution: JECRC

## 1. Overview

The University Placement Portal is a web application that gives students of \[University Name\] a searchable archive of past campus recruitment drives. For each company it shows which year and month the company visited, how many interview rounds there were, what each round looked like, and what questions were asked. On top of this archive, the portal adds preparation tools (eligibility checker, question bank, practice mode, prep roadmaps), community features (alumni directory, Q&A, mock interviews) and admin tools for the placement cell.

The product is built as a single Java Spring Boot application with a PostgreSQL database and server-rendered pages (Thymeleaf with HTMX), so it is simple to host, maintain and hand over to future student teams.

## 2. Problem Statement

Placement knowledge at most universities is scattered and informal. Seniors share experiences in WhatsApp groups, spreadsheets are passed between batches, and details such as which month a company visits or how many rounds it conducts are lost each year. As a result:

- Students prepare blindly, without knowing the typical process for a company.
- Juniors cannot easily reach seniors who were placed at a company they are targeting.
- The placement cell has no single place to keep historical data, announce upcoming drives or produce reports.
- Valuable experience data disappears when a batch graduates.

## 3. Goals and Non-Goals

### Goals

1. Give every student one trusted place to look up any past company's visit history, rounds and questions.
2. Help students prepare efficiently with practice tools tied to real past questions.
3. Make it easy and safe for placed students to contribute experiences, so the dataset grows every year.
4. Give the placement cell tools to manage data, announce drives, moderate content and export reports.
5. Keep the product maintainable by a small student team.

### Non-Goals (version 1)

- Running the placement process itself (applications, shortlisting, offer letters).
- Hosting coding tests or live interviews.
- Replacing the placement cell's existing registration system.
- A native mobile app (the web app will be mobile-responsive instead).
- Public access outside the university.

## 4. Users and Personas

| Persona | Description | Key needs |
| --- | --- | --- |
| Student (prep-seeker) | Pre-final or final-year student preparing for placements | Find company history, practice questions, know eligibility, reach seniors |
| Contributor | A placed student or alumnus who shares experience | Easy submission form, option to stay anonymous, recognition |
| Mentor | A placed senior who offers mock interviews | Set availability, manage bookings |
| Admin | Placement cell member or faculty coordinator | Manage data, approve content, post drives, export reports |

## 5. Scope and Release Phases

The product is delivered in four phases. Each phase must be complete and tested before the next begins.

| Phase | Theme | Contents |
| --- | --- | --- |
| 1 | Core archive | Authentication, company directory and detail, calendar, statistics, contribute form, admin panel and import, bookmarks, comparison, prep checklist |
| 2 | Prepare and connect | Upcoming drives, eligibility checker, student dashboard, question bank, practice mode, prep roadmap, alumni directory, Q&A, badges, notifications, calendar sync, resume tips, mock interviews |
| 3 | Quality and reporting | Audit log, report-wrong-entry, duplicate and spam detection, rate limiting, PDF and Excel reports |
| 4 | AI (optional) | Experience summarizer, question topic classifier, prep chatbot, all behind a feature flag |

## 6. Functional Requirements

Priority key: P0 = must have for the phase, P1 = should have, P2 = nice to have.

### 6.1 Authentication and Accounts (Phase 1)

| ID | Requirement | Priority |
| --- | --- | --- |
| AUTH-1 | Users register only with a university email address (\[@university.edu\]) and must verify it by email before logging in | P0 |
| AUTH-2 | Login with email and password; passwords stored with BCrypt | P0 |
| AUTH-3 | Password reset by email link | P0 |
| AUTH-4 | Roles: student and admin. Contributor status is derived from having an approved experience | P0 |
| AUTH-5 | All content requires login; /admin/\*\* is restricted to admins | P0 |
| AUTH-6 | Student profile with branch, graduation year, CGPA and active backlogs | P0 |

### 6.2 Company Directory (Phase 1)

| ID | Requirement | Priority |
| --- | --- | --- |
| DIR-1 | Grid or list of all companies showing logo, name, industry, roles, number of visits, most recent visit and average difficulty | P0 |
| DIR-2 | Search by company name with instant results (HTMX) | P0 |
| DIR-3 | Filters: year, month, industry, role type, job type (placement, internship, both), branch eligibility, CTC range | P0 |
| DIR-4 | Sort by most recent, most frequent visitor, highest CTC, difficulty | P1 |

### 6.3 Company Detail Page (Phase 1)

| ID | Requirement | Priority |
| --- | --- | --- |
| DET-1 | Overview: about the company, roles, eligibility (CGPA cutoff, branches, backlog policy), package range | P0 |
| DET-2 | Visit history timeline with year, month, roles and students selected | P0 |
| DET-3 | Per-visit selection process: total rounds, and for each round the name, type, duration and difficulty | P0 |
| DET-4 | Questions grouped by round with topic tags and difficulty | P0 |
| DET-5 | Offer details: joining location, work mode, bond or service agreement, internship conversion | P1 |
| DET-6 | Approved student experiences with upvotes | P0 |
| DET-7 | Difficulty rating: students vote 1 to 5, average displayed | P1 |
| DET-8 | Report a wrong entry button on drives, rounds and questions | P1 |

### 6.4 Placement Calendar (Phase 1)

| ID | Requirement | Priority |
| --- | --- | --- |
| CAL-1 | Month-by-month view of companies that visited in a selected year | P0 |
| CAL-2 | Heatmap or table across years showing which companies come in which month | P1 |
| CAL-3 | Year toggle | P0 |

### 6.5 Statistics Dashboard (Phase 1)

| ID | Requirement | Priority |
| --- | --- | --- |
| STAT-1 | Companies per year and per month (bar charts) | P0 |
| STAT-2 | Average rounds, top recruiters by selections | P1 |
| STAT-3 | Package distribution and trends over years (average, median, highest) | P1 |
| STAT-4 | Branch-wise placement numbers and percentages | P1 |
| STAT-5 | Most commonly asked question topics | P1 |

### 6.6 Contribute Experience (Phase 1)

| ID | Requirement | Priority |
| --- | --- | --- |
| CON-1 | Form capturing company, year, month, role, number of rounds, round-by-round details, questions, outcome, tips and difficulty rating | P0 |
| CON-2 | Option to post anonymously; the author is never exposed in UI, API or exports (admins see the author only in the approval queue) | P0 |
| CON-3 | Every submission enters a pending queue and appears publicly only after admin approval | P0 |
| CON-4 | Rejected submissions show a reason to the author | P1 |

### 6.7 Admin Panel (Phase 1)

| ID | Requirement | Priority |
| --- | --- | --- |
| ADM-1 | Create, edit and delete companies, drives, rounds and questions | P0 |
| ADM-2 | Approval queue to approve or reject submissions | P0 |
| ADM-3 | Bulk import of historical data from CSV or Excel with a preview and row-level validation errors before saving | P0 |
| ADM-4 | Manage tags, branches and industries | P1 |
| ADM-5 | Review and resolve reported entries | P1 |

### 6.8 Bookmarks and Comparison (Phase 1)

| ID | Requirement | Priority |
| --- | --- | --- |
| BKM-1 | Bookmark companies and questions | P1 |
| BKM-2 | Compare two companies side by side (rounds, topics, CTC, difficulty, eligibility) | P1 |
| BKM-3 | Preparation checklist generated from a company's past rounds and topics | P1 |

### 6.9 Preparation Features (Phase 2)

| ID | Requirement | Priority |
| --- | --- | --- |
| UPD-1 | Admin posts upcoming drives with date, roles, eligibility, CTC, registration link and deadline | P0 |
| UPD-2 | Students see upcoming drives sorted by date with an "eligible for me" badge and a link to the company's past data | P0 |
| ELG-1 | Eligibility checker lists past and upcoming companies the student is or was eligible for, with reasons for exclusion | P0 |
| ELG-2 | "What if" mode to change CGPA or branch and see the list update | P2 |
| QB-1 | Searchable question bank filtered by company, topic, difficulty and round type | P0 |
| QB-2 | Practice mode: one question at a time, reveal answer or hint, mark as solved, unsure or skipped | P0 |
| QB-3 | Progress tracker by topic with weak topics highlighted | P1 |
| RM-1 | Generate a week-by-week prep roadmap for a company for 1 to 8 weeks, with a tickable checklist and links to practice questions | P1 |
| RES-1 | Upload a PDF resume (max 2 MB), stored privately, visible only to the student and admins | P1 |
| RES-2 | Automatic checklist (contact info, length, projects, skills section) | P2 |
| RES-3 | Optional consent toggle to share the resume as an example for juniors | P2 |
| MOCK-1 | Mentors add availability slots and list the companies they can mock-interview for | P1 |
| MOCK-2 | Students book and cancel slots; both parties receive email confirmation | P1 |
| MOCK-3 | Student can rate the session and leave feedback | P2 |
| DASH-1 | Student dashboard showing eligible upcoming drives, practice progress, bookmarks and notifications | P0 |

### 6.10 Community Features (Phase 2)

| ID | Requirement | Priority |
| --- | --- | --- |
| ALU-1 | Contributors can opt in to a public alumni profile (name, branch, year, company, role, optional LinkedIn, topics they can help with) | P1 |
| ALU-2 | Searchable alumni directory; contact details shown only if the alumnus opted in | P1 |
| QA-1 | Threaded comments on each company page | P1 |
| QA-2 | Verified contributor badge for users with an approved experience | P2 |
| QA-3 | Report abusive comments; admin can hide or delete | P1 |
| GAM-1 | Points for approved experiences, upvotes received and answered questions | P2 |
| GAM-2 | Badges (for example First Contributor, Helpful, Mentor) and a yearly leaderboard that excludes anonymous posts | P2 |
| NOT-1 | In-app notification bell and email for: matching upcoming drives, deadlines within 48 hours, new approved experience on a bookmarked company, submission outcome, mock interview booking or cancellation | P1 |
| NOT-2 | User settings to turn each notification type on or off | P1 |
| CAL-4 | "Add to calendar" (.ics download) for upcoming drives and booked mock interviews | P2 |

### 6.11 Quality and Reporting (Phase 3)

| ID | Requirement | Priority |
| --- | --- | --- |
| AUD-1 | Audit log of who created, edited, deleted or approved what, with timestamp and old and new values for key fields | P1 |
| AUD-2 | Admin page to view and filter the audit log | P1 |
| SPM-1 | Flag near-duplicate submissions (same company and year with highly similar text) for admin review | P1 |
| SPM-2 | Rate limiting on logins, submissions and comments | P0 |
| RPT-1 | Yearly placement report export as PDF and Excel: companies visited, selections, package statistics, branch-wise numbers and month-wise calendar | P1 |

### 6.12 AI Features (Phase 4, optional)

| ID | Requirement | Priority |
| --- | --- | --- |
| AI-1 | Experience summarizer: short "what to expect" summary per company from approved experiences, labelled as AI-generated, refreshed when new experiences are approved | P2 |
| AI-2 | Question classifier: suggests topic tags and difficulty for admin confirmation | P2 |
| AI-3 | Prep chatbot answering only from the portal's own data; responds "I don't have data on that" instead of guessing | P2 |
| AI-4 | Resumes, personal contact details and anonymous authors' identities are never sent to the AI provider | P0 (if AI enabled) |
| AI-5 | The whole app works with no AI key; AI features are simply hidden | P0 |

## 7. Non-Functional Requirements

| Area | Requirement |
| --- | --- |
| Performance | Directory search and filters respond in under 500 ms for up to 5,000 drives; pages load in under 2 seconds on a typical campus connection |
| Availability | Target 99% uptime during placement season; daily database backups |
| Security | HTTPS only, CSRF protection, output escaping, input validation, secure file uploads (type and size checks, storage outside the web root), role-based access on every route |
| Privacy | Anonymous posts never reveal the author; resumes private by default; alumni contact details opt-in; users can delete their account and personal data |
| Accessibility | Keyboard navigation, sufficient contrast and alt text, aiming for WCAG 2.1 AA |
| Responsiveness | Mobile-first layout; light and dark mode |
| Maintainability | Layered code structure, Flyway migrations, unit and integration tests for services and security rules, README with setup and deployment steps |
| Scalability | Designed for a single university (a few thousand users); stateless app that can be containerised |

## 8. Technical Overview

- Language and framework: Java 21 with Spring Boot 3.x (Web, Data JPA, Security, Validation, Mail, Scheduling).
- Database: PostgreSQL with Flyway migrations.
- Frontend: Thymeleaf templates, Bootstrap 5, HTMX for instant search and partial updates, Chart.js for dashboards. One project, no separate frontend build.
- Libraries: Lombok, Apache POI and OpenCSV (import and export), OpenPDF or iText (PDF reports), Apache PDFBox (resume checks).
- Architecture: browser to Spring Security to controllers to services to repositories to PostgreSQL, with SMTP email and file storage as external dependencies.
- Deployment: Dockerfile and docker-compose (app and database); deployable on Render, Railway or a college server.

### Key data entities

Company, Drive, UpcomingDrive, Round, Question, Experience, DifficultyVote, User, AlumniProfile, Bookmark, PracticeAttempt, Roadmap and RoadmapItem, Resume, MockSlot and MockBooking, Comment, Notification and NotificationPreference, Badge and UserBadge, Report, AuditLog.

Core relationships: a Company has many Drives; a Drive has many Rounds and Experiences; a Round has many Questions; a User has many Bookmarks, PracticeAttempts and Notifications.

## 9. Success Metrics

| Metric | Target (first placement season) |
| --- | --- |
| Registered students out of eligible students | 70% |
| Weekly active students during placement season | 40% of registered |
| Companies with complete data (visit, rounds, at least 5 questions) | 80% of companies from the last 3 years |
| Approved student experiences collected | 100 in the first season |
| Median time for admin to approve a submission | Under 48 hours |
| Students using practice mode at least once | 30% of registered |
| Student satisfaction (short in-app survey, 1 to 5) | 4.0 or higher |
| Reported data errors resolved within 7 days | 90% |

## 10. Risks and Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Historical data is hard to obtain or incomplete | App feels empty at launch | Partner with the placement cell early; use bulk import; run a contribution drive among final-year students |
| Placement cell does not approve sharing package or selection data | Features removed or limited | Get written approval before launch; make CTC visibility configurable (exact, range or hidden) |
| Low student contribution | Dataset stops growing | Badges and leaderboard; reminders after placement; make the form short and anonymous-friendly |
| Inaccurate or fake submissions | Loss of trust | Admin approval queue, duplicate detection, report-wrong-entry flow, audit log |
| Privacy complaints (resumes, alumni details) | Legal and reputational risk | Opt-in for sharing, private storage, clear privacy notice, account deletion |
| Scope too large for a small team | Delays and an unfinished product | Strict phased delivery; ship Phase 1 first and collect feedback |
| AI gives wrong answers | Misleading students | Answer only from portal data, label AI content, keep AI optional and behind a feature flag |
| Server load during peak season | Slow or down at the worst time | Caching for read-heavy pages, indexed filters, load test before the season |

## 11. Assumptions and Dependencies

- The university provides email addresses on a single domain that can be used for verification.
- The placement cell will supply historical data in CSV or Excel form and nominate at least one admin.
- An SMTP provider (for example the university mail server) is available for verification and notification emails.
- Hosting is available (cloud platform or college server) with PostgreSQL and a persistent file storage path.
- If AI features are enabled, an LLM provider API key is supplied through an environment variable.

## 12. Open Questions

1. Which CTC details may be shown to students: exact figures, ranges, or only averages?
2. Should students from all batches see all data, or only the final two years?
3. Who owns moderation after launch: the placement cell or a student committee?
4. Are alumni (already graduated) allowed to log in with a personal email, or only current students with university emails?
5. Should the portal integrate with the university's existing student information system for CGPA and branch, or rely on self-entered values?
6. How long should resumes and personal data be retained after graduation?
7. Is there a university branding guide to follow for colours and logos?

## 13. Release Plan

| Milestone | Deliverable | Exit criteria |
| --- | --- | --- |
| M1 | Phase 1 internal build with seed data | All P0 items in 6.1 to 6.8 working; tests passing |
| M2 | Pilot with 30 to 50 students and the placement cell | Feedback collected; critical bugs fixed; real historical data imported |
| M3 | Campus-wide launch of Phase 1 | Privacy notice published; backups and monitoring in place |
| M4 | Phase 2 release | P0 items in 6.9 and 6.10 working; notifications tested |
| M5 | Phase 3 release | Audit log, rate limiting and reports live |
| M6 | Phase 4 (optional) | AI features behind a flag, with privacy review complete |

## 14. Approval

| Role | Name | Date | Sign-off |
| --- | --- | --- | --- |
| Product owner | \[Your Name\] |  |  |
| Placement cell representative | \[Name\] |  |  |
| Faculty coordinator | \[Name\] |  |  |
| Tech lead | \[Name\] |  |  |
