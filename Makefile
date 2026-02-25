# ===========================================
# Loan Origination System - Makefile
# ===========================================

.PHONY: help build test run clean docker-build docker-up docker-down docker-logs jib-local jib-push

# Default target
help:
	@echo "Available commands:"
	@echo "  make build        - Build the application"
	@echo "  make test         - Run tests"
	@echo "  make run          - Run the application locally"
	@echo "  make clean        - Clean build artifacts"
	@echo "  make docker-build - Build Docker image using Dockerfile"
	@echo "  make docker-up    - Start all services with docker-compose"
	@echo "  make docker-down  - Stop all services"
	@echo "  make docker-logs  - View logs from all services"
	@echo "  make jib-local    - Build Docker image locally using Jib"
	@echo "  make jib-push     - Build and push Docker image using Jib"

# Maven commands
build:
	./mvnw clean package -DskipTests

test:
	./mvnw test

run:
	./mvnw spring-boot:run

clean:
	./mvnw clean

# Docker commands
docker-build:
	docker build -t loan-origination-system .

docker-up:
	docker-compose up -d

docker-up-dev:
	docker-compose --profile dev up -d

docker-down:
	docker-compose down

docker-logs:
	docker-compose logs -f

docker-restart:
	docker-compose down && docker-compose up -d

# Jib commands (no Docker daemon required)
jib-local:
	./mvnw compile jib:dockerBuild -Plocal

jib-push-ghcr:
	./mvnw compile jib:build -Pghcr

jib-push-dockerhub:
	./mvnw compile jib:build -Pdockerhub

# Database commands
db-up:
	docker-compose up -d postgres

db-down:
	docker-compose stop postgres

db-reset:
	docker-compose down -v postgres && docker-compose up -d postgres

# Full stack commands
start: docker-up
	@echo "All services started!"

stop: docker-down
	@echo "All services stopped!"

restart: docker-restart
	@echo "All services restarted!"

