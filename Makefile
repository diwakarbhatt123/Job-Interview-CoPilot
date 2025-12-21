.PHONY: \
	build-account build-fit build-analyzer build-plan build-profile build-auth-core build-all \
	bootrun-account bootrun-fit bootrun-analyzer bootrun-plan bootrun-profile bootrun-all \
	lint-fix-account lint-fix-profile lint-fix-analyzer lint-fix-fit lint-fix-plan lint-fix-auth-core lint-fix-all \
	nginx-up nginx-down run-all

build-account:
	@echo "Building Account Service..."
	./gradlew --configuration-cache -p services/account-service build
	@echo "Account service built successfully."

build-fit:
	@echo "Building Fit Score Service..."
	./gradlew --configuration-cache -p services/fit-score-service build
	@echo "Fit Score service built successfully."

build-analyzer:
	@echo "Building Job Analyzer Service..."
	./gradlew --configuration-cache -p services/job-analyzer-service build
	@echo "Job Analyzer service built successfully."

build-plan:
	@echo "Building Prep Plan Service..."
	./gradlew --configuration-cache -p services/prep-plan-service build
	@echo "Prep Plan service built successfully."

build-profile:
	@echo "Building Profile Service..."
	./gradlew --configuration-cache -p services/profile-service build
	@echo "Profile service built successfully."

build-auth-core:
	@echo "Building Auth Core Library..."
	./gradlew --configuration-cache -p shared/libs/auth-core build
	@echo "Auth Core library built successfully."

build-all: build-account build-fit build-analyzer build-plan build-profile build-auth-core
	@echo "All services built successfully."

bootrun-account:
	@echo "Running Account Service..."
	./gradlew -p services/account-service bootRun

bootrun-fit:
	@echo "Running Fit Score Service..."
	./gradlew -p services/fit-score-service bootRun

bootrun-analyzer:
	@echo "Running Job Analyzer Service..."
	./gradlew -p services/job-analyzer-service bootRun

bootrun-plan:
	@echo "Running Prep Plan Service..."
	./gradlew -p services/prep-plan-service bootRun

bootrun-profile:
	@echo "Running Profile Service..."
	./gradlew -p services/profile-service bootRun

bootrun-all: bootrun-account bootrun-fit bootrun-analyzer bootrun-plan bootrun-profile
	@echo "All services are running."

lint-fix-account:
	./gradlew -p services/account-service spotlessApply

lint-fix-profile:
	./gradlew -p services/profile-service spotlessApply

lint-fix-analyzer:
	./gradlew -p services/job-analyzer-service spotlessApply

lint-fix-fit:
	./gradlew -p services/fit-score-service spotlessApply

lint-fix-plan:
	./gradlew -p services/prep-plan-service spotlessApply

lint-fix-auth-core:
	./gradlew -p shared/libs/auth-core spotlessApply

lint-fix-all: lint-fix-account lint-fix-profile lint-fix-analyzer lint-fix-fit lint-fix-plan lint-fix-auth-core

nginx-up:
	nginx -c "$(CURDIR)/gateway/nginx.conf"
	@echo "Nginx server started."

nginx-down:
	nginx -c "$(CURDIR)/gateway/nginx.conf" -s stop

run-all: nginx-up bootrun-all
	@echo "Nginx and all services are running."

################ Frontend Services ################
fe-install:
	@echo "Installing frontend dependencies..."
	pnpm -C frontend/job-interview-copilot install
	@echo "Frontend dependencies installed."

fe-dev:
	@echo "Starting frontend development server..."
	pnpm -C frontend/job-interview-copilot dev
	@echo "Frontend development server is running."

fe-build:
	@echo "Building frontend application..."
	pnpm -C frontend/job-interview-copilot build
	@echo "Frontend application built successfully."

fe-lint:
	@echo "Linting frontend code..."
	pnpm -C frontend/job-interview-copilot lint
	@echo "Frontend code linted successfully."

fe-lint-fix:
	@echo "Fixing frontend lint issues..."
	pnpm -C frontend/job-interview-copilot lint --fix
	@echo "Frontend lint issues fixed."
