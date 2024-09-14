plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
}

group = "com.github.nexus421"
version = "3.0.0"
val globalVersion = version.toString()

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation(kotlin("reflect"))
    compileOnly("io.objectbox:objectbox-kotlin:4.0.0")
    compileOnly("io.objectbox:objectbox-java:4.0.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}

kotlin {
    jvmToolchain(11)
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = "com.github.nexus421"
            artifactId = "KotNexLib"
            version = globalVersion
            from(components["java"])
        }
    }
}

