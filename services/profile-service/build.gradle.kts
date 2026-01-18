import com.diffplug.gradle.spotless.SpotlessTask
import com.github.spotbugs.snom.SpotBugsTask

plugins {
	java
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.github.spotbugs") version "6.0.26"
}

group = "com.jobcopilot"
version = "0.0.1-SNAPSHOT"
description = "Profile Service"

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
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:4.0.1")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.apache.pdfbox:pdfbox:3.0.6")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
//	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.23.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    constraints {
        implementation("org.apache.logging.log4j:log4j-core:2.25.3") {
            because("CVE-2025-68161: fix TLS hostname verification in SocketAppender")
        }
        implementation("org.eclipse.jgit:org.eclipse.jgit:7.2.1.202505142326-r") {
            because("CVE-2025-4949: XXE in ManifestParser/AmazonS3 transport")
        }
        implementation("com.fasterxml.jackson.core:jackson-core:2.15.0") {
            because("CVE-2025-52999: avoid StackoverflowError on deeply nested input")
        }
    }
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

tasks.build {
    dependsOn(tasks.withType<SpotlessTask>())
    dependsOn(tasks.withType<SpotBugsTask>())
    dependsOn(tasks.test)
}
