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

publishing {
    publications {
        create<MavenPublication>("release") {
            //from(components.release)

            groupId = "com.github.nexus421"
            artifactId = "KotNexLib"
            version = "1.0.0"
        }
    }
}

