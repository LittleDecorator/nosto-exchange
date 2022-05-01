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

            val ansiReset = "\u001B[0m"
            val ansiGreen = "\u001B[32m"
            val ansiRed = "\u001B[31m"
            val ansiYellow = "\u001B[33m"

            fun getColoredResultType(resultType: TestResult.ResultType): String {
                return when (resultType) {
                    TestResult.ResultType.SUCCESS -> "$ansiGreen $resultType $ansiReset"
                    TestResult.ResultType.FAILURE -> "$ansiRed $resultType $ansiReset"
                    TestResult.ResultType.SKIPPED -> "$ansiYellow $resultType $ansiReset"
                }
            }

            afterTest(
                KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
                    println("${desc.className} | ${desc.displayName} : ${getColoredResultType(result.resultType)}")
                })
            )

            afterSuite(
                KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
                    if (desc.parent == null) {
                        println("Result: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)")
                    }
                })
            )
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
