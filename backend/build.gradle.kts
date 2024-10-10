plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.jpa") version "1.9.25"
    id("com.google.cloud.tools.jib") version "3.4.3"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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
    // https://mvnrepository.com/artifact/org.locationtech.jts/jts-core
    implementation("org.locationtech.jts:jts-core:1.20.0")

    // https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-spatial
    implementation("org.hibernate.orm:hibernate-spatial")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jib {
    from {
        image = "public.ecr.aws/docker/library/amazoncorretto:21-alpine"
//        platforms {
//            platform {
//                architecture = "arm64"
//                os = "linux"
//            }
//        }
    }
    to {
        image = "registry.musma.net/dandi/territory-service"
        tags = setOf("${project.version}", "latest")
        auth {
            username = "musma"
            password = "Musma0812@!"
        }
    }
    container {
        creationTime = "USE_CURRENT_TIMESTAMP"
        jvmFlags =
            listOf(
                "-Xms512m",
                "-Xmx512m",
            )
        setAllowInsecureRegistries(true)
    }
}