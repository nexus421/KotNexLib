plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
}

group = "com.github.nexus421"
version = "1.11.0"
val globalVersion = version.toString()

repositories {
    mavenCentral()
}

dependencies {

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

