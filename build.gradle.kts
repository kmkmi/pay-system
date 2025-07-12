
plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.paysystem"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
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
    // Spring Boot Starters (using a bundle)
    implementation(libs.bundles.spring.boot.starters)

    // Database & Migration
    runtimeOnly(libs.mysql.connector)
    implementation(libs.bundles.flyway)

    // Lombok for boilerplate code reduction
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Kafka
    implementation(libs.spring.kafka)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.bundles.testcontainers)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:1.19.7")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
