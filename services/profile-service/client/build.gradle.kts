plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.jobcopilot"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.1")
    }
}

dependencies {
    api("org.springframework.cloud:spring-cloud-openfeign-core")
    api("org.springframework:spring-web")
}
