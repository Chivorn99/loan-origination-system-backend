# 🚀 DevOps Setup Guide - Loan Origination System

This guide walks you through setting up deployments, Docker, CI/CD, and environment management.

---

## 📋 Table of Contents
1. [Quick Start - Run Locally](#quick-start)
2. [Railway Deployment Setup](#railway-deployment)
3. [Docker Setup](#docker-setup)
4. [GitHub Actions CI/CD](#github-actions-cicd)
5. [Environment Variables](#environment-variables)
6. [Troubleshooting](#troubleshooting)

---

## 🏃 Quick Start - Run Locally {#quick-start}

### Option 1: Run with Maven (Recommended for Development)
```bash
# Windows PowerShell
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Option 2: Run the JAR file
```bash
# Build first
.\mvnw clean package -DskipTests

# Run the JAR
java -jar target/loan_origination_system-0.0.1-SNAPSHOT.jar
```

### Option 3: Run with Docker Compose
```bash
docker-compose up -d
```

**Access the app:** http://localhost:8080

**Health check:** http://localhost:8080/actuator/health

---

## 🚂 Railway Deployment Setup {#railway-deployment}

### Step 1: Create Railway Account & Project
1. Go to [railway.app](https://railway.app) and sign up
2. Click "New Project" → "Deploy from GitHub repo"
3. Select your repository: `loan-origination-system-backend`

### Step 2: Configure Environment Variables in Railway
In Railway dashboard → Your Project → Variables tab, add:

```
DB_USERNAME=your_supabase_username
DB_PASSWORD=your_supabase_password
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

### Step 3: Get Railway Token for CI/CD
1. Go to Railway dashboard → Account Settings → Tokens
2. Create a new token named `github-actions`
3. Copy the token

### Step 4: Add Token to GitHub Secrets
1. Go to your GitHub repo → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Name: `RAILWAY_TOKEN`
4. Value: paste your Railway token

### Step 5: Deploy!
- **Automatic:** Push to `main` branch triggers deployment
- **Manual:** Run `railway up` from terminal (with Railway CLI installed)

### Railway CLI Commands
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Link to project
railway link

# Deploy
railway up

# View logs
railway logs

# Open project in browser
railway open
```

---

## 🐳 Docker Setup {#docker-setup}

### Build Docker Image
```bash
# Build with Dockerfile
docker build -t loan-app .

# OR build with Jib (no Docker daemon needed)
.\mvnw compile jib:dockerBuild
```

### Run Docker Container
```bash
# Run with environment variables
docker run -d \
  -p 8080:8080 \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  -e SPRING_PROFILES_ACTIVE=default \
  --name loan-app \
  loan-app

# Or use docker-compose
docker-compose up -d
```

### Docker Compose for Local Development
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up -d --build
```

### Push to Docker Hub
```bash
# Tag the image
docker tag loan-app sys-thai69/loan-origination-system:latest

# Login to Docker Hub
docker login

# Push
docker push sys-thai69/loan-origination-system:latest
```

### Push to GitHub Container Registry (GHCR)
```bash
# Login to GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin

# Tag and push
docker tag loan-app ghcr.io/chivorn99/loan-origination-system:latest
docker push ghcr.io/chivorn99/loan-origination-system:latest
```

---

## ⚙️ GitHub Actions CI/CD {#github-actions-cicd}

### Workflows Overview

| Workflow | Trigger | What it does |
|----------|---------|--------------|
| `ci.yml` | Push/PR to main, develop | Build, test, deploy to Railway |
| `cd.yml` | Push to main, tags | Build & push Docker image to GHCR |
| `release.yml` | Tag `v*` | Create GitHub release with JAR |

### Required GitHub Secrets

Go to **Settings → Secrets and variables → Actions → New repository secret**

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `RAILWAY_TOKEN` | Railway deployment token | Railway Dashboard → Account → Tokens |
| `DOCKERHUB_USERNAME` | Docker Hub username | Your Docker Hub account |
| `DOCKERHUB_TOKEN` | Docker Hub access token | Docker Hub → Account Settings → Security |

### Manual Trigger
You can manually trigger the CD pipeline:
1. Go to Actions tab
2. Select "CD Pipeline"
3. Click "Run workflow"
4. Choose environment (staging/production)

---

## 🔐 Environment Variables {#environment-variables}

### Local Development (.env file)
Create a `.env` file in the project root:
```env
# Database
DB_USERNAME=postgres
DB_PASSWORD=your_local_password

# Application
SPRING_PROFILES_ACTIVE=default
SERVER_PORT=8080
```

### Railway Environment Variables
Set these in Railway Dashboard → Variables:
```
DB_USERNAME=your_supabase_user
DB_PASSWORD=your_supabase_password
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

### GitHub Secrets (for CI/CD)
```
RAILWAY_TOKEN=your_railway_token
```

### Environment Profiles

| Profile | Use Case | Configuration |
|---------|----------|---------------|
| `default` | Local development | Uses `.env` file |
| `prod` | Production (Railway) | Uses Railway env vars |
| `test-h2` | Testing | In-memory H2 database |

---

## 🔧 Troubleshooting {#troubleshooting}

### "Permission denied: ./mvnw" in GitHub Actions
**Solution:** Already fixed in workflows with `chmod +x ./mvnw`

### "Cannot connect to localhost:8080"
**Check:**
1. Is the app running? `docker ps` or check terminal
2. Is port 8080 available? `netstat -an | findstr 8080`
3. Check logs: `docker-compose logs -f app`

### Docker build fails with "openjdk:21-jdk-slim not found"
**Solution:** Already fixed - using `eclipse-temurin:21-jdk-alpine` instead

### Railway deployment fails
**Check:**
1. Is `RAILWAY_TOKEN` secret set in GitHub?
2. Is the Railway project linked? Run `railway link` locally
3. Check Railway logs in dashboard

### Database connection errors
**Check:**
1. Are `DB_USERNAME` and `DB_PASSWORD` set correctly?
2. Is the Supabase database accessible?
3. Check if SSL is required: `?sslmode=require` in URL

### Tests fail in CI
**Check:**
1. Tests require H2 profile: `-Dspring.profiles.active=test-h2`
2. Check test logs in GitHub Actions

---

## 📱 Quick Reference Commands

```bash
# === LOCAL DEVELOPMENT ===
.\mvnw spring-boot:run                    # Run locally
.\mvnw clean package -DskipTests          # Build JAR
.\mvnw test                               # Run tests

# === DOCKER ===
docker-compose up -d                      # Start with Docker
docker-compose down                       # Stop
docker-compose logs -f app                # View logs
docker build -t loan-app .                # Build image

# === RAILWAY ===
railway login                             # Login to Railway
railway link                              # Link project
railway up                                # Deploy
railway logs                              # View logs

# === JIB (Docker without daemon) ===
.\mvnw compile jib:dockerBuild            # Build local image
.\mvnw compile jib:build -Pghcr           # Push to GHCR
.\mvnw compile jib:build -Pdockerhub      # Push to Docker Hub
```

---

## 🎯 Deployment Flow

```
Developer pushes code to GitHub
         ↓
GitHub Actions CI runs (build & test)
         ↓
If main branch → Deploy to Railway
         ↓
Railway builds Docker image
         ↓
App goes live! 🚀
```

**Your app URL after Railway deployment:**
`https://loan-origination-system-backend-production.up.railway.app`
(Railway will give you the actual URL)

---

## 📞 Need Help?

1. Check Railway logs: Dashboard → Your Project → Deployments → Logs
2. Check GitHub Actions: Repository → Actions → Failed workflow → View logs
3. Run locally first to debug: `.\mvnw spring-boot:run`

