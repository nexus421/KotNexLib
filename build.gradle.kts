plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.dokka") version "1.9.10"
    `maven-publish`
}

group = "com.github.nexus421"
version = "2.4.0"
val globalVersion = version.toString()

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation(kotlin("reflect"))
    compileOnly("io.objectbox:objectbox-kotlin:3.8.0")
    compileOnly("io.objectbox:objectbox-java:3.8.0")
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

