plugins {
    java
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.5.12"
    id("checkstyle")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.5")
    }
}

repositories {
    mavenCentral()
}

val lombokVersion = "1.18.24"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    implementation("org.springdoc:springdoc-openapi-ui:1.6.7")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.6")
    implementation("commons-validator:commons-validator:1.7") {
        exclude(group = "commons-collections", module = "commons-collections")
    }

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "${JavaVersion.VERSION_11}"
        targetCompatibility = "${JavaVersion.VERSION_11}"
    }

    test {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showCauses = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    checkstyle {
        configFile = file("checkstyle.xml")
    }

    checkstyleMain {
        ignoreFailures = false
        maxWarnings = 0
        dependsOn(findByName("classes"))
    }

    checkstyleTest {
        enabled = false
    }
}
