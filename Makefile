.PHONY: \
	build-account build-fit build-analyzer build-plan build-profile build-auth-core build-all \
	bootrun-account bootrun-fit bootrun-analyzer bootrun-plan bootrun-profile bootrun-all \
	lint-fix-account lint-fix-profile lint-fix-analyzer lint-fix-fit lint-fix-plan lint-fix-all \
	nginx-up nginx-down run-all

build-account:
	@echo "Building Account Service..."
	./gradlew -p services/account-service build test
	@echo "Account service built successfully."

build-fit:
	@echo "Building Fit Score Service..."
	./gradlew -p services/fit-score-service build test
	@echo "Fit Score service built successfully."

build-analyzer:
	@echo "Building Job Analyzer Service..."
	./gradlew -p services/job-analyzer-service build test
	@echo "Job Analyzer service built successfully."

build-plan:
	@echo "Building Prep Plan Service..."
	./gradlew -p services/prep-plan-service build test
	@echo "Prep Plan service built successfully."

build-profile:
	@echo "Building Profile Service..."
	./gradlew -p services/profile-service build test
	@echo "Profile service built successfully."

build-auth-core:
	@echo "Building Auth Core Library..."
	./gradlew -p shared/libs/auth-core build test
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

lint-fix-all: lint-fix-account lint-fix-profile lint-fix-analyzer lint-fix-fit lint-fix-plan

nginx-up:
	nginx -c "$(HOME)/Projects/Job&InteviewCoPilot/project/Job-Interview-CoPilot/gateway/nginx.conf"
	@echo "Nginx server started."

nginx-down:
	nginx -c "$(HOME)/Projects/Job&InteviewCoPilot/project/Job-Interview-CoPilot/gateway/nginx.conf" -s stop

run-all: nginx-up run-all
	@echo "Nginx and all services are running."
