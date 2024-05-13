val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version = "3.3.3"
val kgraphql_version = "0.19.0"

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
}

group = "at.gleb"
version = "0.0.1"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

application {
    mainClass.set("at.gleb.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("io.ktor:ktor-server-swagger-jvm")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-serialization-gson:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    implementation("com.apurebase:kgraphql:$kgraphql_version")
    implementation("com.apurebase:kgraphql-ktor:$kgraphql_version")

    // Koin for Ktor
    implementation("io.insert-koin:koin-ktor:3.3.1")

    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.11.0")


    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.testng:testng:7.7.0")
    // Koin testing tools
    testImplementation("io.insert-koin:koin-test:$koin_version")
    // Needed JUnit version
    testImplementation("io.insert-koin:koin-test-junit4:$koin_version")
    testImplementation("io.mockk:mockk:1.13.8")
}
