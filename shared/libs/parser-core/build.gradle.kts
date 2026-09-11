import com.diffplug.gradle.spotless.SpotlessTask
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.kotlin.dsl.withType

plugins {
    id("java")
    id("com.diffplug.spotless") version "8.2.1"
    id("com.github.spotbugs") version "6.4.8"
}

group = "org.jobcopilot"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks.test {
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
        create("html") {
            required.set(true)
        }
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
