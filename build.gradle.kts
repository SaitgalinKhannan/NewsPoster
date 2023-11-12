val tgbotapi_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.khannan"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.inmo:tgbotapi:$tgbotapi_version")
    implementation("dev.inmo:tgbotapi.core:$tgbotapi_version")
    implementation("dev.inmo:tgbotapi.api:$tgbotapi_version")
    implementation("dev.inmo:tgbotapi.utils:$tgbotapi_version")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}