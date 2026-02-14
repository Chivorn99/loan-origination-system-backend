# Loan Origination System - Backend

A Spring Boot backend application for a loan origination system.

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose (optional)

### Run Locally

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd loan-origination-system-backend
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your database credentials
   ```

3. **Run with Maven**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Or run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

The API will be available at `http://localhost:8080`

## 🐳 Docker

### Using Docker Compose (Recommended)

```bash
# Start all services (app + PostgreSQL)
docker-compose up -d

# Start with PgAdmin (development)
docker-compose --profile dev up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Using Jib (No Docker daemon required)

```bash
# Build image locally
./mvnw compile jib:dockerBuild -Plocal

# Push to GitHub Container Registry
./mvnw compile jib:build -Pghcr

# Push to Docker Hub
./mvnw compile jib:build -Pdockerhub
```

### Using Dockerfile

```bash
# Build
docker build -t loan-origination-system .

# Run
docker run -p 8080:8080 --env-file .env loan-origination-system
```

## 📦 Build & Test

```bash
# Build
./mvnw clean package

# Run tests
./mvnw test

# Skip tests
./mvnw clean package -DskipTests
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Database JDBC URL | - |
| `DB_USERNAME` | Database username | - |
| `DB_PASSWORD` | Database password | - |
| `SERVER_PORT` | Application port | 8080 |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | default |

### Profiles

- `default` - Development configuration
- `test-h2` - Testing with H2 in-memory database
- `prod` - Production configuration

## 🏗️ CI/CD

This project uses GitHub Actions for CI/CD:

- **CI Pipeline** (`ci.yml`): Runs on every push/PR
  - Build & Test
  - Code Quality checks
  - Security scanning

- **CD Pipeline** (`cd.yml`): Runs on main branch
  - Build Docker image with Jib
  - Push to GitHub Container Registry
  - Deploy to staging/production

- **Release** (`release.yml`): Runs on version tags
  - Create GitHub release
  - Generate changelog

### Creating a Release

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## 📊 API Endpoints

### Health Check
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information

### Customers
- `GET /api/customers` - List all customers
- `POST /api/customers` - Create customer
- `GET /api/customers/{id}` - Get customer by ID

### Pawn Items
- `GET /api/pawn-items` - List all pawn items
- `POST /api/pawn-items` - Create pawn item

### Pawn Loans
- `GET /api/pawn-loans` - List all loans
- `POST /api/pawn-loans` - Create loan

## 🗂️ Project Structure

```
├── .github/workflows/     # CI/CD pipelines
├── init-scripts/          # Database initialization scripts
├── src/
│   ├── main/
│   │   ├── java/          # Application source code
│   │   └── resources/     # Configuration files
│   └── test/              # Test source code
├── docker-compose.yml     # Docker Compose configuration
├── Dockerfile             # Docker build configuration
├── Makefile               # Build automation
└── pom.xml                # Maven configuration
```

## 🔒 Security

- Non-root user in Docker containers
- Environment variables for sensitive data
- OWASP dependency checking in CI
- Health checks for container orchestration

## 📝 License

This project is licensed under the MIT License.

