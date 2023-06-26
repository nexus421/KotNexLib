# KotNexLib

Some useful kotlin extensions and classes that I use in nearly all my projects. This can be used in all JVM-Applications.
For Android-specific extensions, have a look at my repository "Kotlin-Extensions-Android"

If you have any additional useful features or wishes, request them!

## Gradle Integration

To integrate KotNexLib into your project using Gradle, add the following dependency to your `build.gradle` file:

repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation ("com.github.nexus421:KotNexLib:1.0.3")
}

See releases for other versions.
