plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    `maven-publish`
}

java {
    withSourcesJar()
    withJavadocJar()
}

group = "com.github.nexus421"
version = "4.2.0"
val globalVersion = version.toString()

repositories {
    mavenCentral()
}

dependencies {
    //https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk18on
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")

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

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

publishing {
    repositories {
        maven {
            name = "nexus421Maven"
            url = uri("https://maven.kickner.bayern/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "bayern.kickner"
            artifactId = "KotNexLib"
            version = globalVersion
            from(components["java"])
        }
    }
}

