import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com." + project.name
version = "0.0.1"

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

plugins {
  id("idea")
  id("base")

  //Spring
  id("org.springframework.boot") version "3.0.6"
  id("io.spring.dependency-management") version "1.1.0"

  //Kotlin
  kotlin("jvm") version "1.7.20"
  kotlin("plugin.spring") version "1.7.20"
  kotlin("plugin.allopen") version "1.7.20"
  kotlin("plugin.noarg") version "1.7.20"

  // Compose
  id("org.jetbrains.compose") version "1.4.0"
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.springframework.boot:spring-boot-starter")

  implementation(compose.desktop.currentOs)
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "17"
  }
}

buildscript {
  repositories {
    mavenCentral()
  }
}

repositories {
  mavenCentral()
}