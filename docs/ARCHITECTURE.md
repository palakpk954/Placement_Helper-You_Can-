# 🏗 You_can — System Architecture & Design Specification
> Detailed architectural blueprint for the JECRC University Placement Portal.

---

## 📐 High-Level Architecture

The **You_can** placement portal follows an enterprise Layered Architecture built on the Spring Boot ecosystem, maintaining strict separation between presentation, security, business logic, data persistence, and intelligence services.

```
+-------------------------------------------------------------------------+
|                           CLIENT LAYER                                  |
|   Modern Browser (Desktop & Mobile) • HTML5 / CSS3 / Vanilla JS         |
+------------------------------------+------------------------------------+
                                     | HTTPS / HTTP Requests
                                     v
+-------------------------------------------------------------------------+
|                         SECURITY & FILTER LAYER                         |
|   Spring Security 6.3 • BCrypt • RBAC (ADMIN, STUDENT, ALUMNI) • CSRF   |
+------------------------------------+------------------------------------+
                                     | Authenticated Principals
                                     v
+-------------------------------------------------------------------------+
|                         CONTROLLER LAYER (WEB & API)                    |
|   - AuthController        - DashboardController    - CompanyController  |
|   - CalendarController    - ExperienceController   - AlumniController   |
|   - ContributeController  - AdminController        - AiController       |
+------------------------------------+------------------------------------+
                                     | Service Invocations
                                     v
+-------------------------------------------------------------------------+
|                           SERVICE LAYER                                 |
|   - CustomUserDetailsService (Identity & Credentials)                   |
|   - EligibilityService (Dynamic CGPA / Backlog / Branch Matrix Engine)  |
|   - AiService / DefaultAiService (Grounded Q&A, Summarizer, Classifier) |
+------------------------------------+------------------------------------+
                                     | JPA Repositories
                                     v
+-------------------------------------------------------------------------+
|                         DATA ACCESS LAYER (JPA / ORM)                   |
|   - UserRepository         - CompanyRepository      - DriveRepository   |
|   - RoundRepository        - ExperienceRepository   - QuestionRepository|
|   - AlumniProfileRepo      - UpcomingDriveRepo      - ContributionRepo  |
+------------------------------------+------------------------------------+
                                     | JDBC
                                     v
+-------------------------------------------------------------------------+
|                        PERSISTENCE / STORAGE                            |
|   Embedded File-backed H2 Database (`./data/youcan.mv.db`)              |
+-------------------------------------------------------------------------+
```

---

## 🧠 AI Intelligence Architecture & Grounding Pipeline

The Phase 4 AI module is intentionally built with a **Grounded-First Architecture** to satisfy PRD AI-3 (Zero Hallucination) and PRD AI-4 (Zero PII Leakage).

```
                      +-----------------------+
                      | Incoming User Query   |
                      | (e.g. "Google rounds")|
                      +-----------+-----------+
                                  |
                                  v
                     +-------------------------+
                     | Intent & Entity Parser  |
                     +------------+------------+
                                  |
               +------------------+------------------+
               |                                     |
               v (Greeting Intent)                   v (Recruitment Query)
    +----------------------+              +----------------------+
    | Warm Greeting &      |              | JPA Knowledge Query  |
    | Guidance Dispatcher  |              | - Companies          |
    +----------+-----------+              | - Drives & Rounds    |
               |                          | - Questions & Cutoffs|
               |                          +----------+-----------+
               |                                     |
               |                         +-----------+-----------+
               |                         | Data Found in Portal? |
               |                         +-----+-----------+-----+
               |                           YES |           | NO
               |                               v           v
               |                    +--------------+   +---------------+
               |                    | Grounded     |   | Explicit      |
               |                    | Synthesis &  |   | Unrecorded    |
               |                    | Direct Facts |   | Fallback Alert|
               |                    +-------+------+   +-------+-------+
               |                            |                  |
               +----------------------------+------------------+
                                            |
                                            v
                               +-------------------------+
                               | Response Delivery (JSON)|
                               | - Verified Content      |
                               | - Grounded Boolean Flag |
                               | - Record Citations      |
                               +-------------------------+
```

### Key AI Safety Guardrails:
1. **No External PII Exposure**: Resumes, mobile numbers, and personal identifiers never reach synthesis prompts.
2. **Entity Validation**: Answers regarding packages, rounds, or criteria are pulled directly from verified database records.
3. **Graceful Out-of-Bounds Fallback**: When an unverified query is received, the assistant acknowledges the absence of campus records and guides the student to the official Company Directory or Alumni Mentorship Network.

---

## 🗃 Domain Model & Relationships

```
User (1) <---> (0..1) AlumniProfile
User (1) <---> (0..N) Experience
User (1) <---> (0..N) Contribution
User (1) <---> (0..N) AuditLog

Company (1) <---> (0..N) Drive
Company (1) <---> (0..N) UpcomingDrive
Company (1) <---> (0..N) Experience

Drive (1) <---> (1..N) Round
Round (1) <---> (0..N) Question
```

---

## 🔐 Security Boundaries & Authorization Flow

1. **Unauthenticated Perimeter**:
   - `/login`, `/register`, `/forgot-password`, `/reset-password`
   - Static assets: `/css/**`, `/js/**`, `/images/**`, `/favicon.ico`
2. **Authenticated Student/Alumni Perimeter**:
   - `/dashboard`, `/companies/**`, `/calendar/**`, `/experiences/**`, `/mentors/**`, `/contribute/**`, `/ai-assistant`
   - REST API: `POST /api/ai/chat`, `POST /api/ai/classify`, `GET /api/ai/company-summary/{id}`
3. **Elevated Administrative Perimeter (`ROLE_ADMIN`)**:
   - `/admin`, `/admin/drives`, `/admin/contributions`, `/admin/users`, `/admin/audit-logs`
