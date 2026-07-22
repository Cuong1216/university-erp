# 🎓 University ERP — Enterprise-Grade Monorepo

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React_18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![CI/CD Pipeline](https://img.shields.io/badge/CI%2FCD_Pipeline-GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

**A comprehensive, production-ready University Enterprise Resource Planning (ERP) platform architected with clean domain-driven design, zero N+1 database queries, robust JWT RBAC security, and multi-stage containerized deployment.**

[Explore Architecture](#-architecture--system-design) •
[Engineering Highlights](#-key-engineering-highlights) •
[Quick Start](#-quick-start--developer-experience) •
[Demo Accounts](#-demo-accounts) •
[API Reference](#-core-api-reference)

</div>

---

## 🏛️ Architecture & System Design

The application follows an **Enterprise Monorepo Architecture** unifying a Java 17 Spring Boot REST API and a React Vite SPA client. In production, both services communicate seamlessly inside a Docker bridge network where an optimized Nginx container serves compressed static assets and acts as a reverse proxy for API traffic (`/api/*`), eliminating cross-origin (CORS) overhead and hiding internal backend ports from public internet exposure.

```mermaid
graph LR
    Client([🌐 Web Browser / Client]) -->|HTTP :80| Nginx[🛡️ Nginx Alpine SPA Router]
    
    subgraph Docker Bridge Network (erp-network)
        Nginx -->|Reverse Proxy /api/v1/* :8080| API[🚀 Spring Boot 3 Backend API]
        Nginx -->|Try Files SPA Fallback| Static[📦 React Vite Static Dist]
        API -->|JDBC / HikariCP :5432| DB[(🐘 PostgreSQL 15 Database)]
    end
```

---

## ✨ Key Engineering Highlights

### 1. 🏗️ Clean Architecture & SOLID Principles
* **Layered Separation of Concerns:** Strict decoupling between `Controller` (HTTP request handling), `Service` (pure business logic & transaction management), and `Repository` (data persistence layers).
* **Enterprise Custom Exception Hierarchy:** Centralized error handling via `@ControllerAdvice` (`GlobalExceptionHandler`) utilizing domain-specific exceptions (`BusinessException`, `ResourceNotFoundException` [404], and `BusinessRuleViolationException` [400]) with consistent `ErrorResponseDTO` JSON payloads.
* **Streamlined Collection Processing:** Replaced high-complexity nested loops (`O(N*M)`) with functional Java Streams and deterministic `LinkedHashSet` data structures for schedule analysis.

### 2. ⚡ Performance Optimization & Zero N+1 Query Guarantee
* **N+1 Query Elimination:** Converted `@ManyToMany` and `@OneToMany` lazy relationships to optimized `JOIN FETCH` queries (`UserRepository.findByUsernameWithRoles`) to fetch entity graphs in a single database round-trip.
* **High-Performance JPA Projections:** Designed interface projections (`GiangVienHeSoView`) to combine distinct academic degree (`HeSoHv`) and title (`HeSoCd`) lookups into a unified SQL query during salary calculations—cutting database hits by 50%.
* **B-Tree Indexing & Standard JPQL:** Built explicit B-Tree database indexes on critical foreign keys and filter fields (`user_id`, `ma_cd`, `ma_hv`, `(ma_gv, thang, nam)`). Replaced non-standard functions (`MONTH()`, `YEAR()`) with index-friendly `BETWEEN` date range comparisons.
* **Cryptographic Key Caching:** Pre-initialized and cached HMAC `SecretKey` instances at `@PostConstruct` inside `JwtTokenProvider` to avoid redundant Base64 decoding on every incoming request.

### 3. 🔒 Enterprise Security & Defense in Depth
* **Role-Based Access Control (RBAC):** Fine-grained method security using `@PreAuthorize("hasAnyRole('ROLE_GIANG_VIEN', 'ROLE_GIAO_VU', 'ROLE_ADMIN')")` ensuring strict boundary checks across academic schedules and payroll endpoints.
* **Production DDL Safeguards:** Enforced `DDL_AUTO: validate` in production container configurations to strictly prevent accidental schema modifications or table drops during container restarts.
* **Sanitized Client Diagnostics:** Stripped internal server topologies (`localhost:8080` or stack traces) from frontend error notifications while logging detailed diagnostics safely on the server side.

### 4. 📊 Enterprise Admin Dashboard & Visual Analytics (Recharts)
* **Real-time Payroll & Faculty Distribution:** Built a dynamic, responsive `AdminDashboard` module utilizing `Recharts` (`LineChart` & `PieChart` with custom hover tooltips and KPI overview cards) to visualize 6-month salary expenditure trends alongside real-time department-level budget allocation.
* **Optimized Aggregation Queries:** Designed custom native SQL & JPQL group-by queries (`MonthlySalaryTrendView` & `DepartmentSalaryView`) inside `BangLuongThangRepository` to compute complex multi-period aggregations directly within PostgreSQL, preventing out-of-memory processing on the JVM.
* **Vite & WebSocket Compatibility:** Natively configured `define: { global: 'window' }` in `vite.config.ts` ensuring seamless compatibility between modern Vite ES modules and Node-based WebSocket libraries (`@stomp/stompjs` & `sockjs-client`).

### 5. 🚀 DevOps, Multi-Stage Builds & Automated CI/CD
* **Multi-Stage Containerization:** Built highly compact Docker images using layered builds (`maven:3.9.9-eclipse-temurin-17-alpine` ➔ `eclipse-temurin:17-jre-alpine` running as a non-root `spring:spring` user; and `node:18-alpine` ➔ `nginx:alpine` with Gzip asset compression).
* **Automated TDD Quality Gate:** Integrated GitHub Actions (`.github/workflows/ci.yml`) spinning up ephemeral PostgreSQL containers to execute full `mvn clean verify` unit/integration test suites alongside React TypeScript compilation and `ESLint` style validations on every commit.

---

## 📂 Project Structure

```text
university-erp/
├── .github/
│   └── workflows/
│       └── ci.yml               # Automated CI pipeline (Maven verify & npm lint/build)
├── backend/                     # Spring Boot 3 REST API (Java 17)
│   ├── src/main/java/com/wiz/universityerpapi/
│   │   ├── controller/          # REST endpoints (DashboardController, LuongController, etc.)
│   │   ├── service/             # Transactional business logic & payroll calculation
│   │   ├── repository/          # Spring Data JPA repositories & Aggregation Projections
│   │   ├── entity/              # JPA domain models with explicit FetchTypes
│   │   ├── exception/           # Custom exception hierarchy & GlobalExceptionHandler
│   │   └── security/            # JWT Token Provider, Filters & EntryPoints
│   ├── src/test/                # Unit & Integration test suite (TDD gate)
│   └── Dockerfile               # Multi-stage JRE Alpine non-root build
├── frontend/                    # React 18 + Vite + TypeScript SPA
│   ├── src/
│   │   ├── api/                 # Axios client with interceptors & PROD base URL guard
│   │   ├── components/
│   │   │   └── dashboard/       # Recharts Line/Pie Analytics & KPI Cards (AdminDashboard)
│   │   ├── pages/               # Rich dynamic UI modules (Dashboard, Salary, Schedule, Admin)
│   │   └── store/               # Zustand global authentication & dashboard state
│   ├── vite.config.ts           # Vite build config with WebSocket global compatibility
│   ├── nginx.conf               # Nginx SPA routing, Gzip & Reverse Proxy configuration
│   └── Dockerfile               # Multi-stage Node Alpine -> Nginx Alpine build
├── sql/
│   └── init_database.sql        # Database initialization schema, indexes & mock data
├── docker-compose.yml           # One-command full stack orchestration
├── package.json                 # Root DX configuration (Concurrently dev server)
└── .env.example                 # Environment variables template
```

---

## 🚀 Quick Start & Developer Experience

### Option 1: One-Command Production Stack (Docker Compose)
The fastest way to experience the complete platform including the PostgreSQL database, Spring Boot API, and React Frontend:

```bash
# 1. Clone the repository
git clone https://github.com/Cuong1216/university-erp.git
cd university-erp

# 2. Copy the environment template
cp .env.example .env

# 3. Launch the full stack in detached mode
docker-compose up --build -d
```
🌐 **Access the Application:** Open your browser to **http://localhost** (Frontend SPA automatically routed via Nginx).  
🔌 **API Endpoint Debugging:** Direct API access is available at **http://localhost:8080/api/v1**.

---

### Option 2: One-Command Concurrent Local Development (DX)
For full-stack developers modifying both backend and frontend code simultaneously without managing two separate terminal tabs:

```bash
# 1. Install root dependencies (Concurrently)
npm install

# 2. Start both Spring Boot API and React Vite dev servers simultaneously!
npm run dev
```
* **Backend Dev Server:** Starts automatically via `mvnw spring-boot:run` at **http://localhost:8080**.
* **Frontend Dev Server:** Starts automatically via Vite at **http://localhost:5173** with Hot Module Replacement (HMR).

---

## 🔑 Demo Accounts

When the application boots, `DataInitializer.java` automatically seeds the database with the following accounts for comprehensive feature & RBAC testing:

| Role | Username | Password | Key Permissions & Accessible Modules |
| :--- | :--- | :--- | :--- |
| **👑 System Admin** | `admin` | `admin123` | **Full system access**: View **Admin Dashboard** (`/dashboard` with Recharts 6-month salary trends & faculty breakdown), salary config (`/admin/salary-config`), user & role management. |
| **📚 Academic Officer (Giáo vụ)** | `giaovu` | `giaovu123` | Manage academic salary calculations (`/academic/salary-management`), finalize monthly payrolls (`chot-luong`), export Excel reports. |
| **👨‍🏫 Lecturer (Giảng viên)** | `gv01` | `gv123` | View personal teaching schedule (`/schedule`), inspect monthly salary snapshots (`/teacher/salary`). |
| **🎓 Student (Sinh viên)** | `sv01` | `sv123` | View personal academic schedule (`/schedule`) and semester grades (`/student/grades`). |

---

## 🛠️ Tech Stack

| Category | Technologies |
| :--- | :--- |
| **Backend Core** | Java 17, Spring Boot 3.4.1, Spring Security 6, Spring Data JPA, Hibernate ORM |
| **Security & Auth** | JSON Web Tokens (JJWT 0.12.6 with HMAC-SHA512), BCrypt Password Hashing |
| **Database & Caching** | PostgreSQL 15, HikariCP Connection Pooling, B-Tree Indexing |
| **Frontend Core** | React 18, TypeScript 5.6, Vite 8, Recharts 3, Zustand State Management, React Router 7 |
| **HTTP & Reporting** | Axios with Auth Interceptors, Apache POI 5.4.0 (Excel Spreadsheet Export) |
| **DevOps & Tooling** | Docker, Docker Compose, Nginx Alpine, GitHub Actions CI/CD, Concurrently |

---

## 📋 Core API Reference

| HTTP Method | Endpoint | Secured Role | Description |
| :---: | :--- | :---: | :--- |
| `POST` | `/api/v1/auth/login` | *Public* | Authenticate user credentials and issue signed JWT Bearer token |
| `GET` | `/api/v1/dashboard/salary-stats` | `ROLE_ADMIN` | Retrieve aggregated 6-month salary historical trend and current-month faculty distribution for charts |
| `GET` | `/api/v1/luong/my-salary` | `ROLE_GIANG_VIEN` | Retrieve authenticated lecturer's current monthly salary snapshot & history |
| `POST` | `/api/v1/luong/chot-luong` | `ROLE_GIAO_VU`, `ROLE_ADMIN` | Finalize teaching log calculations & freeze salary snapshot for a given month |
| `GET` | `/api/v1/luong/export/excel` | `ROLE_GIAO_VU`, `ROLE_ADMIN` | Export comprehensive monthly payroll report as a formatted Excel (`.xlsx`) file |
| `GET` | `/api/v1/lich-hoc/my-schedule` | `ROLE_GIANG_VIEN` | Fetch detailed weekly teaching schedule and room assignments |

---

<div align="center">
  <b>Built with ❤️ using Clean Code & Enterprise Software Engineering Practices.</b>
</div>
