# 🤝 Contributing to You_can

Thank you for your interest in contributing to the **You_can (JECRC University Placement Portal)**! We welcome contributions from students, alumni, and open-source contributors to help streamline campus placements.

---

## 🧭 Code of Conduct
We are committed to providing a welcoming, inclusive, and harassment-free environment for everyone. Please be respectful and constructive in all communications and code reviews.

---

## 🛠️ Development Workflow

### 1. Prerequisites
- **Java**: OpenJDK 21 LTS (`java -version`)
- **Build Tool**: Apache Maven 3.9+ (or use the included `./mvnw` wrapper)
- **Database**: Embedded H2 (pre-configured) or PostgreSQL 15+
- **Git**: Configured with your official campus or personal email

### 2. Fork & Clone
```bash
git clone https://github.com/palakpk954/Placement_Helper-You_Can-.git
cd Placement_Helper-You_Can-
```

### 3. Branching Strategy
Always create a dedicated feature or bugfix branch off `main`:
- `feat/feature-name` (e.g., `feat/add-leetcode-integration`)
- `fix/issue-description` (e.g., `fix/login-session-timeout`)
- `docs/documentation-update` (e.g., `docs/update-api-guide`)

```bash
git checkout -b feat/your-feature-name
```

---

## 🏗️ Building and Testing Locally

### Run the Application
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
The portal will be available at: **`http://localhost:8080`**  
The H2 Database Console is accessible at: **`http://localhost:8080/h2-console`**

### Run Automated Tests
```bash
./mvnw test
```

---

## 📐 Coding & Architectural Guidelines

1. **Migration-Driven Schema**:
   - Never modify entity tables manually via Hibernate auto-ddl.
   - All schema changes must be declared as a new Flyway migration script under `backend/src/main/resources/db/migration/` (e.g., `V8__new_feature_tables.sql`).
2. **Layered Separation of Concerns**:
   - **Controllers**: Handle HTTP routing, authentication context, and model binding.
   - **Services**: Enforce domain validation, business rules, and transactions (`@Transactional`).
   - **Repositories**: Extend Spring Data JPA interfaces without business logic.
3. **Security & Zero PII Leakage**:
   - Never commit API keys, secrets, or real passwords to version control.
   - Use environment variable fallbacks (e.g., `${GEMINI_API_KEY:}`).
   - Passwords must be hashed using `BCryptPasswordEncoder`.

---

## 🚀 Submitting a Pull Request (PR)

1. Ensure all tests compile and pass:
   ```bash
   ./mvnw clean test-compile
   ```
2. Commit your changes using conventional commit messages:
   ```bash
   git commit -m "feat(opportunities): add filter by hackathon batch eligibility"
   ```
3. Push to your branch and open a PR against `main`.
4. Provide a clear description of:
   - What changed and why.
   - Testing steps performed locally.
   - Any related issue numbers.
