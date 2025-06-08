plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
}

group = "com.github.nexus421"
version = "3.2.1"
val globalVersion = version.toString()

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation(kotlin("reflect"))
    compileOnly("io.objectbox:objectbox-kotlin:4.3.0")
    compileOnly("io.objectbox:objectbox-java:4.3.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    compileOnly("io.github.g0dkar:qrcode-kotlin:4.2.0")
    compileOnly("io.ktor:ktor-server-core:3.0.2")

    //Ktor Client
    compileOnly("io.ktor:ktor-client-core:3.1.3")
    compileOnly("io.ktor:ktor-client-cio:3.1.3")
    compileOnly("io.ktor:ktor-client-content-negotiation:3.1.3")
    compileOnly("io.ktor:ktor-serialization-kotlinx-json:3.1.3")
    compileOnly("io.ktor:ktor-client-auth:3.1.3")
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

