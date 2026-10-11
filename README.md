# CineWave Backend API 🎬

The backend REST API for **CineWave**, an enterprise-grade movie ticket and cinema auditorium booking platform built with **Spring Boot 3.5**, **Java 17/21**, and **Spring Security 6**.

---

## 🚀 Overview

CineWave Backend delivers high-performance RESTful APIs managing real-time cinema operations:
- **Authentication & Security**: Stateless JWT token authentication with role-based access control (`ROLE_CUSTOMER`, `ROLE_ADMIN`).
- **Catalog Management**: Full CRUD operations for movies, genres, languages, multiplexes, auditoriums, and showtimes.
- **Auditorium Seating Engine**: Dynamic tiered seat layout generation (Recliner, Premium, Regular) with seat availability resolution.
- **Concurrency-Safe Booking**: Transactional reservations with optimistic/pessimistic lock guards against double bookings.
- **Simulated Payment & QR Pass**: Payment verification and digital boarding pass generation.
- **Admin Control Center**: Analytics aggregations, revenue tracking, and schedule collision detection.

---

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.0
- **Language**: Java 17 / 21
- **Security**: Spring Security 6 + JJWT (`0.12.6`)
- **Persistence**: Spring Data JPA / Hibernate ORM 6
- **Database**:
  - **Local Development**: H2 In-Memory Database (auto-seeded)
  - **Production**: MySQL (Aiven Cloud Managed MySQL with SSL)
- **Build Tool**: Maven (`mvnw`)

---

## 📂 Project Structure

```
backend/
├── src/main/java/com/cinewave/
│   ├── config/          # Security, Web MVC, and CORS configuration
│   ├── controller/      # REST API Controllers (Auth, Movie, Booking, Theater, Admin, etc.)
│   ├── dto/             # Request & Response Data Transfer Objects
│   ├── entity/          # JPA Entities (User, Movie, Theater, Screen, Seat, Showtime, Booking)
│   ├── exception/       # Global exception handlers & custom exceptions
│   ├── repository/      # Spring Data JPA Repositories
│   ├── security/        # JWT Authentication Filter, Token Provider & UserDetails
│   └── service/         # Business logic layer
└── src/main/resources/
    ├── application.properties
    └── application-local.properties
```

---

## ⚡ Getting Started

### Prerequisites
- **JDK 17** or **JDK 21**
- **Maven 3.8+** (or use included `./mvnw`)

### Running Locally (Zero-Config In-Memory Database)

Runs with an auto-seeded catalog (sample movies, theaters, screens, and showtimes) without needing an external MySQL instance:

```bash
# On Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# On Windows PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

- API Base URL: `http://localhost:8080/api`
- H2 Web Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:cinewavedb`
  - User: `sa` | Password: *(blank)*

### Running with Production / Cloud MySQL

Set the environment variables or update `application.properties`:

```bash
export DB_URL="jdbc:mysql://<host>:<port>/cinewavedb?sslMode=REQUIRED"
export DB_USERNAME="<username>"
export DB_PASSWORD="<password>"
export JWT_SECRET="<your-256-bit-secret-key>"

./mvnw spring-boot:run
```

---

## 🔑 Key API Endpoints

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new customer | Public |
| `POST` | `/api/auth/login` | Login and obtain JWT token | Public |
| `GET` | `/api/movies` | Fetch all movies / now showing | Public |
| `GET` | `/api/movies/{id}` | Movie details and metadata | Public |
| `GET` | `/api/theaters` | List multiplexes and locations | Public |
| `GET` | `/api/showtimes` | Query showtimes by movie & date | Public |
| `GET` | `/api/showtimes/{id}/seats` | Seat layout and availability | Public |
| `POST` | `/api/bookings` | Create ticket reservation | Authenticated |
| `GET` | `/api/bookings/my` | Retrieve user booking history | Authenticated |
| `GET` | `/api/admin/metrics` | Platform overview & metrics | Admin Only |
| `POST` | `/api/admin/movies` | Add or update movie | Admin Only |

---

## 🧪 Testing & Build

```bash
# Run unit and integration tests
./mvnw clean test

# Build executable JAR
./mvnw clean package -DskipTests
```
The output JAR will be generated in `target/cinewave-backend-1.0.0.jar`.

---

## 👤 Author
- **GitHub**: [@saiteja00007](https://github.com/saiteja00007)
