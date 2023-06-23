plugins {
    kotlin("jvm") version "1.8.21"
    `maven-publish`
}

group = "com.github.nexus421"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
