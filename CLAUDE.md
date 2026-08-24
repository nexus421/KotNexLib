# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

KotNexLib is a single-module Kotlin/JVM utility library (extensions, crypto, terminal UI, file/config management, optional 3rd-party integrations). It is consumed as a dependency, so every public declaration is API surface. Published to `https://maven.kickner.bayern/releases` as `bayern.kickner:KotNexLib`.

Kotlin 2.3.10, `jvmToolchain(11)`, JUnit 5, Gradle 8.10 wrapper. Gradle needs a JDK on `PATH`/`JAVA_HOME`.

**JDK 21 is the known-good JDK for building.** Gradle 8.10 can crash its daemon on newer JDKs (e.g. JDK 25) that happen
to be the system default — if `./gradlew build`/`test` fails inexplicably, retry with
`JAVA_HOME=/path/to/jdk-21 ./gradlew ...`. This is separate from the library's own `jvmToolchain(11)` target.

## Commands

```bash
./gradlew build
```

```bash
./gradlew test
```

Run a single test class or method:

```bash
./gradlew test --tests "kotnexlib.StringExtensionsTest"
```

```bash
./gradlew test --tests "kotnexlib.StringExtensionsTest.testHash"
```

Publishing requires `nexus421MavenUsername` / `nexus421MavenPassword` (Gradle properties or `ORG_GRADLE_PROJECT_*` env vars) — the repo name `nexus421Maven` in [build.gradle.kts](build.gradle.kts) determines those property names:

```bash
./gradlew publish
```

There is no CI, no linter config, and no formatter beyond `kotlin.code.style=official`.

## Architecture

### Package layout does not match one root package

Most code lives under `kotnexlib`, but `file`, `enums`, and `kpub` are **top-level packages** (`src/main/kotlin/file/…` → `package file`). Don't "fix" these into `kotnexlib.*` — it would break consumers' imports.

- `kotnexlib` — the extension core: String/ByteArray/Iterable/Map/Set/Number/Boolean/generic extensions, `ResultOf*`, `Cache`/`LocalCache`, `ColoredPrinters`, `ArgsInterpreter`, `IBAN`, `Math`, `Permutations`.
- `kotnexlib.crypto` — `AES` (CBC + GCM), `Argon2Helper`, `BlowfishEncryption` (legacy), `Hash` (`String.hash`, `hashBC`, `hashIter`).
- `kotnexlib.security` — `TOTP`.
- `kotnexlib.storage` — `SshStorage` (shells out to `ssh`/`scp`) and `RemoteFile`, which delegates back to `SshStorage`.
- `kotnexlib.external[.objectbox]` — integrations for ObjectBox, Ktor (server + client), kotlinx.serialization, qrcode-kotlin.
- `file` — `BaseFolder` → `LogFile` / `ConfigFile` → `BaseFolderWithLogAndConfig<T>` (composed working directory with rotating log + serializable config).
- `kpub` — `KPubClient`, a Ktor-client wrapper for the KPub mail/SMS service.

### Optional dependencies are `compileOnly`

ObjectBox, Ktor (server and client), kotlinx-serialization, kotlinx-coroutines, and qrcode-kotlin are declared `compileOnly` on purpose: consumers only pay for what they use, and the code compiles against those APIs without leaking them transitively. Only `bcprov-jdk18on` and `kotlin-reflect` are real `implementation` deps.

Consequences when adding code:
- Anything touching an optional library belongs in `kotnexlib.external` (or `kpub`) and must be reachable only from there — never reference Ktor/ObjectBox/coroutines types from `kotnexlib` core files. `Cache.kt`/`LocalCache` is the exception that already imports coroutines; keep such usage confined to the API that needs it.
- Tests cannot exercise `compileOnly` code unless the dependency is added as `testImplementation`. The existing test suite covers only the dependency-free core (`crypto`, string/byte/iterable extensions, `IBAN`, `Math`, `Permutations`, `ResultOf`, `ArgsInterpreter`).

### Error handling: `ResultOf`, not exceptions

[ResultOf.kt](src/main/kotlin/kotnexlib/ResultOf.kt) defines a family of sealed result types (`ResultOf`, `ResultOf2<T, V>`, `ResultOf3`, `ResultOfEmpty`, `ResultOfTripple` for success/warning/error). Public APIs return one of these, `kotlin.Result` (via `runCatching`), or nullable types via the `tryOrNull` / `ifNull` / `ifTrue` / `ifFalse` idioms in `GenericExtensions.kt` and `BooleanExtensions.kt`. Library code should not throw for expected failure paths.

### Opt-in annotations

[Annotations.kt](src/main/kotlin/kotnexlib/Annotations.kt) defines `@ExperimentalKotNexLibAPI` (warning) and `@CriticalAPI` (error, for unsafe/legacy APIs such as Blowfish). Mark new unstable or security-sensitive APIs with these rather than removing or silently changing them; prefer `@Deprecated` with a `ReplaceWith` migration for existing public API.

### AES data format

`AES.AESData` is the serialization contract for password-based encryption: `toString()` emits Base64 of `type`, `iv/nonce`, `iterations`, `salt`, `compressed`, `ciphertext` joined in that order by the `FILE_SEPERATOR` control char (`\u001C`), and `AESData.restore(String)` parses it positionally. That string is stored in consumers' databases — changing the field order, separator, or defaults (PBKDF2-HMAC-SHA256, 600k iterations, min 65 536) breaks decryption of existing data.

## Conventions

- KDoc on every public declaration, including `@param`/`@return`. This is a documentation-heavy library; matching that density matters.
- New/changed public API should be reflected in `docs/` (one Markdown page per topic) and linked from the README's Documentation section.
- Version lives in [build.gradle.kts](build.gradle.kts) (`version = "4.3.0"`); the README installation snippet quotes it and needs updating with it.
- Work happens on `develop`; `master` is the main branch.
