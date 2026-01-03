import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm") version "2.3.0"
}

application {
    mainClass = "com.kylemayes.generator.MainKt"
}

group = "com.kylemayes"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.ajalt.clikt:clikt:4.3.0")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.apache.commons:commons-text:1.14.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:6.9.0.202403050737-r")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.kohsuke:github-api:1.321")
    implementation("org.slf4j:slf4j-simple:2.0.12")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}
