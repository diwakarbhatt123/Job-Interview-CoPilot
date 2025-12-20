import com.github.spotbugs.snom.SpotBugsTask
import com.diffplug.gradle.spotless.SpotlessTask

plugins {
	java
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.github.spotbugs") version "6.0.26"
}

group = "com.jobcopilot"
version = "0.0.1-SNAPSHOT"
description = "Account Service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jobcopilot:auth-core:0.0.1-SNAPSHOT")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
//	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat("1.29.0")
        target("src/**/*.java")
    }

    kotlinGradle {
        target("*.gradle.kts")
    }

    format("misc") {
        target("*.md", "*.yml", "*.yaml", ".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

spotbugs {
    toolVersion.set("4.9.8")
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
}

tasks.build {
    dependsOn(tasks.withType<SpotlessTask>())
    dependsOn(tasks.withType<SpotBugsTask>())
    dependsOn(tasks.test)
}

tasks.withType<SpotBugsTask>().configureEach {
    reports {
        // enable HTML
        create("html") {
            required.set(true)
        }
        // disable XML
        create("xml") {
            required.set(false)
        }
    }
}
