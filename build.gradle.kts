plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
}

group = "com.github.nexus421"
version = "1.14.0"
val globalVersion = version.toString()

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
//    implementation("com.google.crypto.tink:tink:1.11.0")
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

