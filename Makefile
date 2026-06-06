.PHONY: help start stop clean build test run dev prod docker native logs

# Variables
JAR_FILE = target/messageforge-1.0.0.jar
DOCKER_IMAGE = messageforge:latest
PORT = 8080

help:
	@echo "MessageForge Development Commands"
	@echo "===================================="
	@echo ""
	@echo "Startup:"
	@echo "  make dev              Start development environment"
	@echo "  make start            Start Docker services (PostgreSQL, Redis, RabbitMQ)"
	@echo "  make stop             Stop all Docker services"
	@echo ""
	@echo "Building:"
	@echo "  make build            Build Spring Boot JAR"
	@echo "  make native           Build native image (requires GraalVM)"
	@echo "  make docker           Build Docker image"
	@echo ""
	@echo "Running:"
	@echo "  make run              Run Spring Boot application"
	@echo "  make run-native       Run native executable"
	@echo ""
	@echo "Testing:"
	@echo "  make test             Run all unit tests"
	@echo "  make test-integration Run integration tests"
	@echo "  make coverage         Generate test coverage report"
	@echo ""
	@echo "Database:"
	@echo "  make db-reset         Reset database (drops all data)"
	@echo "  make db-migrate       Run Flyway migrations"
	@echo ""
	@echo "Cleaning:"
	@echo "  make clean            Clean build artifacts"
	@echo "  make clean-all        Full cleanup (including Docker volumes)"
	@echo ""
	@echo "Logs:"
	@echo "  make logs             View application logs"
	@echo "  make logs-db          View PostgreSQL logs"
	@echo "  make logs-redis       View Redis logs"
	@echo "  make logs-rabbitmq    View RabbitMQ logs"

# ==================== Development ====================

dev: clean start build
	@echo "✅ Development environment ready"
	@echo "📝 Next: run 'make run' to start the application"

# ==================== Infrastructure ====================

start:
	@echo "🐳 Starting Docker services..."
	docker-compose up -d
	@echo "⏳ Waiting for services to be ready..."
	sleep 10
	@echo "✅ Services started"
	@echo "📊 Services:"
	@echo "   - PostgreSQL: localhost:5432"
	@echo "   - Redis: localhost:6379"
	@echo "   - RabbitMQ: localhost:5672 (Admin: http://localhost:15672)"
	@echo "   - PgAdmin: http://localhost:5050"

stop:
	@echo "🛑 Stopping Docker services..."
	docker-compose down
	@echo "✅ Services stopped"

# ==================== Building ====================

build:
	@echo "🔨 Building Spring Boot JAR..."
	mvn clean package -DskipTests
	@echo "✅ Build complete: $(JAR_FILE)"

native:
	@echo "⚙️ Building native image (this may take 2-3 minutes)..."
	mvn -Pnative clean package -DskipTests
	@echo "✅ Native image built"

docker:
	@echo "🐳 Building Docker image..."
	docker build -t $(DOCKER_IMAGE) .
	@echo "✅ Docker image built: $(DOCKER_IMAGE)"
	@echo "📝 Run with: docker run -p 8080:8080 $(DOCKER_IMAGE)"

# ==================== Running ====================

run: build
	@echo "🚀 Starting Spring Boot application..."
	java -jar $(JAR_FILE)

run-native:
	@echo "🚀 Starting native image..."
	./target/messageforge -Dspring.profiles.active=prod

run-docker:
	@echo "🐳 Starting Docker container..."
	docker run -d \
		--name messageforge \
		-p $(PORT):8080 \
		--network messageforge-network \
		-e DB_URL=jdbc:postgresql://postgres:5432/messageforge \
		-e REDIS_HOST=redis \
		-e RABBITMQ_HOST=rabbitmq \
		$(DOCKER_IMAGE)
	@echo "✅ Container started"
	@echo "📝 Access at: http://localhost:$(PORT)/api/actuator/health"

# ==================== Testing ====================

test:
	@echo "🧪 Running unit tests..."
	mvn test

test-integration:
	@echo "🧪 Running integration tests..."
	mvn verify

coverage:
	@echo "📊 Generating test coverage report..."
	mvn jacoco:report
	@echo "✅ Report generated: target/site/jacoco/index.html"

# ==================== Database ====================

db-migrate:
	@echo "📦 Running database migrations..."
	mvn flyway:migrate
	@echo "✅ Migrations complete"

db-reset:
	@echo "⚠️  Resetting database (all data will be lost)..."
	@read -p "Are you sure? (y/n) " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker-compose down -v postgres; \
		docker-compose up -d postgres; \
		sleep 10; \
		mvn flyway:clean; \
		mvn flyway:migrate; \
		echo "✅ Database reset complete"; \
	fi

# ==================== Cleaning ====================

clean:
	@echo "🧹 Cleaning build artifacts..."
	mvn clean
	rm -rf target/

clean-all: clean
	@echo "♻️ Full cleanup (including Docker volumes)..."
	docker-compose down -v
	rm -rf .m2/ logs/
	@echo "✅ Full cleanup complete"

# ==================== Logging ====================

logs:
	@echo "📋 Application logs:"
	tail -f logs/messageforge.log

logs-db:
	@echo "📋 PostgreSQL logs:"
	docker-compose logs -f postgres

logs-redis:
	@echo "📋 Redis logs:"
	docker-compose logs -f redis

logs-rabbitmq:
	@echo "📋 RabbitMQ logs:"
	docker-compose logs -f rabbitmq

# ==================== Health Check ====================

health:
	@echo "🏥 Checking application health..."
	@curl -s http://localhost:$(PORT)/api/actuator/health | python3 -m json.tool
	@echo ""

# ==================== Code Quality ====================

lint:
	@echo "🔍 Running code quality checks..."
	mvn checkstyle:check spotbugs:check
	@echo "✅ Quality checks passed"

format:
	@echo "💅 Formatting code..."
	mvn google-java-format:format
	@echo "✅ Code formatted"

# ==================== Documentation ====================

docs:
	@echo "📚 Generating documentation..."
	mvn javadoc:javadoc
	@echo "✅ Javadoc generated: target/site/apidocs/index.html"

swagger:
	@echo "📖 Swagger UI available at: http://localhost:$(PORT)/api/swagger-ui.html"
	@echo "📖 API Docs: http://localhost:$(PORT)/api/v3/api-docs"

# ==================== Utilities ====================

env-setup:
	@echo "🔧 Setting up environment..."
	cp .env.example .env
	@echo "✅ .env file created (review and customize if needed)"

version:
	@echo "MessageForge v1.0.0"
	@echo "Java: $$(java -version 2>&1 | head -1)"
	@echo "Maven: $$(mvn --version | head -1)"
	@echo "Docker: $$(docker --version)"

# ==================== Database Info ====================

db-info:
	@echo "📊 Database Information:"
	@echo "Host: localhost:5432"
	@echo "Database: messageforge"
	@echo "User: postgres"
	@echo "Password: postgres"
	@echo ""
	@echo "📊 Access via:"
	@echo "  - PgAdmin: http://localhost:5050 (admin@example.com/admin)"
	@echo "  - psql: psql -h localhost -U postgres -d messageforge"

.DEFAULT_GOAL := help
