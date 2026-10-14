# CineWave Backend API 🎬

An enterprise-grade, digital cinema auditorium and movie ticket booking platform built with **Spring Boot 3.5.0**, **Java 21**, **Spring Security 6**, and **MySQL**.

Inspired by BookMyShow, CineWave powers real-time seat reservations, automated TMDB poster synchronization, theater discovery, role-based administration, OTP authentication, and Razorpay payment integration.

---

## 🌟 Key Capabilities

1. **Exact TMDB Poster Synchronization (`/api/tmdb/**`, `MovieSyncService`)**:
   - Searches TMDB API for Indian and global releases (Telugu, Hindi, Tamil, Malayalam, Kannada, English).
   - Admin search & selection modal stores exact TMDB IDs, backdrop paths, and high-resolution posters.
   - Built-in curated catalog fallback prevents fake, missing, or mismatched posters.

2. **Automated Theater Directory Discovery (`TheaterDirectoryService`)**:
   - Discovers and caches multiplexes with geographic coordinates (Hyderabad, Telangana prioritized).
   - Strict separation between discovered directory theaters and verified partner inventory.
   - Bulk CSV import support for new multiplex chains.

3. **3 Separate Roles with Backend Enforcement**:
   - **ADMIN**: System-wide dashboard, global metrics, CRUD over movies, theaters, screens, seats, shows, users, events, streams, offers, and booking audits.
   - **THEATER_OWNER**: Dedicated portal (`/owner/*`) restricted exclusively to assigned theaters. Screen configuration, seat layout generation, showtime scheduling, and theater-specific revenue analytics.
   - **USER**: Public catalog browsing, seat reservation, M-ticket generation, OTP verification, profile management, and booking cancellations.

4. **Concurrency-Safe Seating & Booking Engine (`BookingService`)**:
   - Real-time seat layout generation with 3 tiers: 🛋️ Recliner, 🌟 Premium, 🎟️ Regular.
   - Transactional isolation prevents seat double booking during concurrent checkouts.
   - 10-minute temporary seat hold with automatic release on expiration or cancellation.

5. **Dual-Mode OTP Verification (`OtpService`)**:
   - Email OTP via SMTP (Gmail, SendGrid, Amazon SES) with masked recipient delivery.
   - SMS OTP via Twilio / Fast2SMS with normalized E.164 phone numbers.
   - Secure fallback to dev/sandbox console mode when provider credentials are not yet configured.

6. **Razorpay Payment Integration (`PaymentService`)**:
   - Order creation, server-side amount calculation, and HMAC-SHA256 signature verification.
   - Transparent sandbox simulation mode for local development.

7. **Events, Streams & Promotional Offers**:
   - Live events ticketing with venue, date, and pass booking (`/api/events`).
   - Digital stream catalog with legal playback actions and fallback states (`/api/streams`).
   - Promotional promo-code engine with minimum order value, validity checks, and instant discounts (`/api/offers`).

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.0
- **Language**: Java 21 (JDK 21 LTS)
- **Security**: Spring Security 6 (Stateless JWT token filter + BCrypt password hashing)
- **Persistence**: Spring Data JPA / Hibernate 6
- **Connection Pool**: HikariCP Enterprise
- **Database**: MySQL 9.7 (Supports local MySQL & Cloud MySQL like Aiven, AWS RDS, Railway)
- **Build Tool**: Maven 3.9+ (`mvnw`)
- **External Integrations**: TMDB API v3, Razorpay API, JavaMailSender SMTP, Twilio SMS

---

## 📂 Project Structure

```
backend/
├── src/main/java/com/cinewave/
│   ├── config/              # SecurityConfig, DataSeeder, AdminInitializer
│   ├── controller/          # REST Controllers (Auth, Movie, Theater, Show, Booking, Admin, Owner, etc.)
│   ├── dto/                 # Request/Response DTOs
│   ├── entity/              # JPA Entities (User, Movie, Theater, Screen, Seat, Show, Booking, Payment, Otp, Event, Offer, Stream)
│   ├── exception/           # Global exception handlers
│   ├── repository/          # Spring Data JPA Repositories
│   ├── security/            # JwtAuthenticationFilter, JwtTokenProvider, CustomUserDetailsService
│   └── service/             # Business Logic Layer
├── src/main/resources/
│   ├── application.properties
│   └── schema_update.sql    # Incremental database DDL schemas
├── pom.xml
└── .env.example
```

---

## ⚡ Getting Started

### Prerequisites
- **Java 21 JDK** installed (`java -version`)
- **Maven 3.8+** (or use included `./mvnw`)
- **MySQL 8.0+ / 9.x** running on `localhost:3306`

### 1. Database Setup
Ensure the MySQL database `cinewave` exists:
```sql
CREATE DATABASE IF NOT EXISTS cinewave CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configuration (`.env`)
Copy the environment template:
```bash
cp .env.example .env
```
Fill in your database credentials:
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=cinewave
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_super_secret_jwt_key_at_least_32_characters_long
```

### 3. Build & Run
```bash
# Run all unit and integration tests (44 tests)
./mvnw clean test

# Run application
./mvnw spring-boot:run
```

Backend will start on: **`http://localhost:8080`**

Default Administrator:
- **Email**: `admin@cinewave.com`
- **Password**: `Admin@123`

Default Theater Owner:
- **Email**: `owner@cinewave.com`
- **Password**: `Admin@123`

---

## 🧪 Testing

```bash
# Run full automated test suite
mvn test
```
The test suite validates:
- Database connectivity & HikariCP initialization
- JWT authentication & BCrypt credential verification
- Role-based authorization (`ADMIN`, `THEATER_OWNER`, `USER`)
- Owner isolation (preventing access to unassigned theaters)
- Concurrency-safe seat reservations & double-booking prevention
- TMDB synchronization & catalog persistence
- Theater directory discovery & CSV bulk import

---

## 🚀 Production Deployment

### Option A: Railway / Render / AWS Elastic Beanstalk
1. Package the executable fat JAR:
   ```bash
   ./mvnw clean package -DskipTests
   ```
2. Set environment variables on your cloud provider:
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
   - `DB_SSL_MODE=REQUIRED`
   - `JWT_SECRET`
   - `CORS_ORIGINS=https://your-frontend-domain.vercel.app`
   - `TMDB_API_KEY`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`
3. Launch with:
   ```bash
   java -jar target/cinewave-backend-1.0.0.jar
   ```

### Option B: Docker Container
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/cinewave-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 📦 GitHub Repository Setup

To push this backend codebase as a standalone repository (`CineWave-Backend`):
```bash
cd backend
git remote set-url origin https://github.com/<YOUR_GITHUB_USERNAME>/CineWave-Backend.git
git add .
git commit -m "feat: complete CineWave Spring Boot backend with full BookMyShow functionality"
git push -u origin main
```
