plugins {
    kotlin("jvm") version "1.9.10"
    //id("io.ktor.plugin") version "2.3.1"
    `maven-publish`
}

group = "com.github.nexus421"
version = "1.7.0"
val globalVersion = version.toString()

repositories {
    mavenCentral()
}

dependencies {

}

tasks.test {
    useJUnitPlatform()
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

