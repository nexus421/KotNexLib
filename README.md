# KotNexLib

<p align="center">
  <img src="https://github.com/user-attachments/assets/dce41e84-cd28-49b8-91a7-6649e6890ad2" alt="KotNexLib Logo" width="150"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-blue.svg?style=flat-square&logo=kotlin" alt="Kotlin Version" />
  <img src="https://img.shields.io/badge/JDK-11%2B-orange.svg?style=flat-square&logo=openjdk" alt="JDK Version" />
</p>

## Overview

**KotNexLib** is a professional collection of Kotlin extensions and utilities designed to simplify JVM development. From
high-performance cryptography to advanced terminal UI components, it provides the building blocks for modern Kotlin
applications.

> [!IMPORTANT]
> This library targets **Java 11 or higher**. For Android-specific extensions,
> see [Kotlin-Extensions-Android](https://github.com/nexus421/Kotlin-Extensions-Android).

> [!WARNING]
> **Migration to 4.0.0**:
> - The project is moving to a new Maven repository: `https://maven.kickner.bayern/releases`.
> - Version 4.0.0 introduces **breaking changes** in the `crypto` module.
> - **Note**: `String.hash` has been moved to the `kotnexlib.crypto` package and may require updated imports.

---

## Documentation

Detailed documentation for each module can be found below:

### [Cryptography](docs/crypto/AesEncryptionHelper.md)

- [**AES Helper**](docs/crypto/AesEncryptionHelper.md): Secure GCM/CBC encryption, password-based key derivation.
- [**Argon2**](docs/crypto/Argon2Helper.md): Modern password hashing (Argon2id).
- [**Blowfish**](docs/crypto/BlowfishEncryptionHelper.md): Legacy support for Blowfish encryption.

### [Extensions](docs/extensions/StringExtensions.md)

- [**Strings**](docs/extensions/StringExtensions.md): Hashing, Base64, mask/cover, validation, and manipulation.
- [**ByteArrays**](docs/extensions/ByteArrayExtensions.md): Compression and encoding.
- [**Collections**](docs/extensions/Collections.md): Advanced Iterable, Map, and Set operations.
- [**Misc**](docs/extensions/MiscExtensions.md): Helper for Boolean, Numbers, and Generic types.

### [File Management](docs/file/FileManagement.md)

- Structured working directories, JSON configuration management, and automated logging with rotation.

### [Terminal Output](docs/terminal/ColoredPrinters.md)

- ANSI colors, styles, progress bars, spinners, and structured tables for professional CLI tools.

### [Utilities](docs/utils/CommonUtils.md)

- [**Math**](docs/utils/Math.md): Vector operations and similarity checks.
- [**IBAN**](docs/utils/IBAN.md): International bank account validation.
- [**Args**](docs/utils/ArgsInterpreter.md): Command-line argument parsing.
- [**Common**](docs/utils/CommonUtils.md): `ResultOf` patterns, permutations, and time measurement.

### [External Integrations](docs/external/ExternalIntegrations.md)

- Extensions for ObjectBox, Ktor, and QR Code generation.

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        name = "nexus421MavenReleases"
        url = uri("https://maven.kickner.bayern/releases")
    }
}

dependencies {
    implementation("com.github.nexus421:KotNexLib:4.1.0")
}
```

---

<p align="center">
  Developed with ❤️ for the Kotlin Community.
</p>

