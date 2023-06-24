plugins {
    kotlin("jvm") version "1.8.21"
    id("io.ktor.plugin") version "2.3.1"
    `maven-publish`
}

group = "com.github.nexus421"
version = "1.0.1"

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

application {
    mainClass.set("")
}

ktor {
    fatJar {
        archiveFileName.set("kotNexLib_$version.jar")
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            //from(components.release)

            groupId = "com.github.nexus421"
            artifactId = "KotNexLib"
            version = "1.0.1"
        }
    }
}

