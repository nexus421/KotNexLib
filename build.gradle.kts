plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.dokka") version "2.1.0"
    `maven-publish`
}

group = "com.github.nexus421"
version = "3.2.1"
val globalVersion = version.toString()

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation(kotlin("reflect"))
    compileOnly("io.objectbox:objectbox-kotlin:5.0.1")
    compileOnly("io.objectbox:objectbox-java:5.0.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    compileOnly("io.github.g0dkar:qrcode-kotlin:4.2.0")
    compileOnly("io.ktor:ktor-server-core:3.0.2")

    //Ktor Client
    compileOnly("io.ktor:ktor-client-core:3.3.3")
    compileOnly("io.ktor:ktor-client-cio:3.3.3")
    compileOnly("io.ktor:ktor-client-content-negotiation:3.3.3")
    compileOnly("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
    compileOnly("io.ktor:ktor-client-auth:3.3.3")
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

