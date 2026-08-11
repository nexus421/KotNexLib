# LLM.md

Referenz für KI-Assistenten (Claude Code o.ä.), die in einem Projekt arbeiten, das KotNexLib einbindet oder einbinden könnte.
Ziel: schnell wissen, was die Bibliothek bereits löst, bevor man es neu implementiert. Für Details (Parameter, Edge Cases)
immer die KDoc-Kommentare in der genannten Quelldatei lesen — hier steht nur die Kurzfassung.

## Einbindung

```kotlin
repositories {
    maven {
        url = uri("https://maven.kickner.bayern/releases")
    }
}

dependencies {
    implementation("bayern.kickner:KotNexLib:4.3.0")
}
```

Kotlin 2.3.10, JVM 11+. ObjectBox-, Ktor-, kotlinx-serialization-, kotlinx-coroutines- und qrcode-kotlin-Integrationen sind
`compileOnly` in KotNexLib selbst — die jeweilige Abhängigkeit muss im eigenen Projekt zusätzlich vorhanden sein, um die
entsprechenden Erweiterungen (Pakete `kotnexlib.external*`) nutzen zu können.

---

## kotnexlib — Kern-Paket

### Control-Flow & Generics
`GenericExtensions.kt`, `Delegates.kt`, `GlobalFunctions.kt`, `Flow.kt`

- `T?.ifNull(isNull, notNull)` — if/else-Ersatz für Nullable-Werte, beide Zweige liefern ein Ergebnis.
- `T?.ifNull { }` / `T?.ifNotNull { }` — führt Block nur bei null bzw. nicht-null aus.
- `T?.isNull()` / `T?.notNull()` — Null-Check als Funktion, für Ketten/`when`.
- `T?.default(value)` — Ersatz für `?:` als Funktionsaufruf.
- `T?.onNull { }` — führt Block bei null aus, gibt den Empfänger zurück.
- `tryOrNull { }` / `K?.tryOrNull { }` — führt Block aus, fängt Exceptions, gibt `null` zurück (optionaler `onError`-Callback).
- `tryOrDefault(default) { }` — wie `tryOrNull`, aber mit Default-Wert statt `null`.
- `getAllSealedSubclassesFrom(KClass)` — liefert alle Subklassen einer sealed class per Reflection.
- `Delegates.once()` / `Delegates.onceOrNull()` — Property-Delegate: Wert darf nur einmal gesetzt werden (wie `lateinit`, aber danach schreibgeschützt).
- `measureTimeMillisAndReturn { }` / `measureTime { }` — misst Ausführungszeit **und** liefert das Blockergebnis (`ResultTimeMeasure<T>`).
- `measureTimeSilent { }` — misst Zeit, übergibt sie an einen Callback statt sie zu drucken.
- `doUntilNotNull { }` — wiederholt `action`, bis ein Ergebnis `!= null` kommt.
- `timeAndEmit(emissionsPerSecond)` — Coroutines-`Flow<Duration>`, der die verstrichene Zeit periodisch emittiert.

### Boolean
`BooleanExtensions.kt`

- `Boolean.ifTrue { }` / `ifFalse { }` — führt Block aus, gibt `this` zurück (verkettbar).
- `Boolean.ifBooleanIs(that) { }` — führt Block aus, wenn `this == that`.
- `Boolean?.orTrue` / `orFalse` — Default-Werte für nullable Boolean.
- `Boolean.toGerman()` / `toEnglish()` — "Ja"/"Nein" bzw. "Yes"/"No" statt `true`/`false`.
- `Boolean.switchText(onTrue, onFalse)` — Ausdrucks-Ersatz für `if (bool) a else b`.
- `Boolean.switchOrder(run1, run2)` — führt zwei Blöcke in von diesem Boolean abhängiger Reihenfolge aus.

### String
`StringExtensions.kt`

- `String.coverString(start, end, coverChar)` — maskiert Teile eines Strings, z. B. `"12345678"` → `"1******8"` (Kontonummern, Secrets in Logs).
- `String?.ifNullOrBlankDo { }` / `ifNullOrBlank { }` — Aktion bzw. Fallback-Wert bei null/blank.
- `String?.isNotNullOrBlank()` / `ifNotNullOrBlank { }` — Null-/Blank-Check inkl. Smart-Cast im Block.
- `String.replaceAllMatchingStart(char)` — entfernt führende Wiederholungen eines Zeichens (z. B. führende Nullen).
- `String?.embedIfNotNull(before, after, fallback)` — umschließt String mit Prefix/Suffix, wenn nicht null.
- `String?.compress()` / `decompress()` — GZIP+Base64-Kompression für (lange) Strings.
- `String.crossContains(that)` — `true`, wenn einer der beiden Strings im anderen enthalten ist.
- `String.isDigitOnly` / `isAlphabeticOnly` / `isAlphanumericOnly` — Zeichenklassen-Checks.
- `String.toDate(pattern, fallback)` / `toDateOrNull(pattern)` — Parsing zu `java.util.Date`.
- `String.hexToInt()` — Hex-String zu `Int`.
- `String.containsAll(list)` / `containsOneOf(list)` / `containsOneOfAndGet(list)` — Mengen-Checks gegen eine Liste von Substrings.
- `String?.equalsOneOf(list)` / `equalsOneOfAndGet(list)` — Vergleich gegen eine Liste möglicher Werte.
- `String.copyToClipboard()` — kopiert den String in die System-Zwischenablage.
- `String.toBase64()` / `fromBase64()` / `fromBase64ToByteArray()` / `fromBase64OrNull()` / `fromBase64ToByteArrayOrNull()` — Base64 hin/zurück.
- `String.splitEachCharBy(separator)` — fügt Trenner zwischen jedes Zeichen ein.
- `String.replaceOrAppend(search, replaceBy)` — ersetzt `search` falls vorhanden, hängt sonst `replaceBy` an.

### ByteArray & CharArray
`ByteArrayExtensions.kt`, `CharArrayExtensions.kt`

- `ByteArray.toBase64()` — Base64-Kodierung.
- `ByteArray?.compress()` / `decompress()` — GZIP-Kompression inkl. Base64.
- `CharArray.useAndWipe { byteArray -> }` — konvertiert sicher zu `ByteArray` (für Passwörter) und löscht den Speicher danach.

### Zahlen & Math
`NumberExtensions.kt`, `Math.kt`

- `Int.forEach(startAt) { }` — Schleife als Extension.
- `Int/Double/Float/Long.negative()` — erzwingt einen negativen Wert.
- `Int/Double.isBetween(lower, higher)` — Bereichs-Check.
- `Long.toDate()` / `toLocalDateTime()` — Millis zu `Date`/`LocalDateTime`.
- `Int/Long.toMillisFrom(TimeUnit)` — eigenes `TimeUnit`-Enum inkl. `WEEK`/`MONTH`/`YEAR`, Umrechnung in Millisekunden.
- `Long/Int.convert(from, to: ConvertType)` — Größenumrechnung Byte/KB/MB/GB/TB.
- `Math.dotProduct(a, b)` / `cosineSimilarity(a, b)` — Vektor-Ähnlichkeit (z. B. für Embeddings).
- `List<Double>.normalizeVector()` / `isNormalizedVector()` — Vektor-Normalisierung/-Check.
- `Double/Int/Float/Long.powOfTwo()` — Quadrat.

### Datum & Zeit
`CalendarFunctions.kt`

- Gegenseitige Umwandlung zwischen `Calendar`/`Date`/`LocalDate`/`LocalTime`/`LocalDateTime` (`toCalendar()`, `toLocalDate()`, `toLocalDateTime()`, `toDate()`, `getAsDate()`, …) sowie `format(pattern)` auf jedem Typ.
- `Calendar.isToday()` / `isSameDay(other)` — Datumsvergleiche.
- `Calendar.year()` / `month()` / `dayOfMonth()` / `dayOfYear()` / `hourOfDay()` / `minute()` / `seconds()` — Kurzzugriffe statt `get(Calendar.X)`.
- `Pair<Calendar, Calendar>.calculateTimeDiffInMinutes()` — Differenz zwischen zwei Kalendern in Minuten.
- `Date.addDays(amount)` — Datum verschieben (auch negativ).

### Collections
`IterableExtensions.kt`, `MapExtensions.kt`, `SetExtensions.kt`

- `Iterable<T>.forEachDoLast { element, isLast -> }` — wie `forEach`, weiß zusätzlich ob es das letzte Element ist.
- `Iterable<T>.splitFilter(predicate)` — teilt eine Liste anhand eines Prädikats in zwei Listen (`SplitList`).
- `Collection<T>.handleSizes()` — liefert `IsEmpty` / `IsOne(entry)` / `HasMany(entries)` für ein exhaustives `when`.
- `Collection<T>.ifSizeIs(size) { }` / `ifSizeIsOne { }` / `ifSizeIsNot(size) { }` — bedingte Ausführung je nach Collection-Größe.
- `Iterable<T>.findInner { }` — sucht ein verschachteltes Objekt innerhalb der Elemente.
- `MutableList<T>.move(entry, toIndex)` / `moveOrAdd(entry, toIndex)` — Element an neue Position verschieben (bzw. einfügen, falls nicht vorhanden).
- `MutableList<T>.addIfAbsent(element)` — fügt nur hinzu, wenn noch nicht enthalten.
- `Iterable<T>.isBefore(t, predicate)` / `isAfter(t, predicate)` — Reihenfolge-Check zwischen Elementen.
- `Map<K,V>.keysOnlyLeft(right)` / `keysOnlyRight(right)` — Mengendifferenz zwischen zwei Maps nach Keys.
- `Map<K,V>.entriesDiffering(right)` — Einträge mit gleichem Key, aber unterschiedlichem Value in beiden Maps.
- `Map<K,V>.areEqual(right)` — Inhaltsvergleich zweier Maps.
- `Set<T>.difference(other)` — Mengendifferenz.

### Cache
`Cache.kt`

- `Cache<K, T>(maxAge, onEvict)` — generischer, thread-sicherer Cache mit optionaler TTL und Auto-Cleanup (`startAutoCleanup()`/`stopAutoCleanup()`), `get`/`set`/`remove`/`clear`.
- `LocalCache` — globaler, **typbasierter** In-Memory-Cache: jeder Typ bekommt automatisch seinen eigenen `Cache<String, T>`-Bucket über `getData<T>(key)` / `setData<T>(key, value)`; `initCache<T>(cache)` zum Vorkonfigurieren (z. B. TTL) vor der ersten Nutzung.

### Terminal-Ausgabe
`ColoredPrinters.kt`

- `Terminal.isLinux()` / `isMacOS()` / `isWindows()` / `supportsAnsiColors()` — Plattform-Checks.
- `printlnColored` / `printColored` / `printlnWithBackground` / `printWithBackground` / `printlnWithStyle` / `printWithStyle` / `printlnFormatted` / `printFormatted` — farbige/gestylte Konsolenausgabe (`CommandLineColors`/`CommandLineBackgroundColors`/`CommandLineStyles`).
- `Cursor.up/down/left/right/moveToStartOfLine/clearLine/clearScreen/savePosition/restorePosition/hide/show` — ANSI-Cursorsteuerung.
- `ProgressBar(...)` / `createLoadingIndicator(message)` — Fortschrittsbalken in der Konsole.
- `Spinner(...)` / `createSpinner(message)` — Ladeanimation für unbestimmte Dauer.
- `Table(...)` mit `addRow(...)` / `print()` — formatierte Tabellenausgabe.
- `updateLine(text)` — überschreibt die aktuelle Zeile (für dynamische Anzeigen).
- `getTerminalWidth()` — ermittelt die Terminalbreite.
- `createRainbowText` / `printRainbow` / `printlnRainbow` — regenbogenfarbiger Text.
- `createTextBox` / `printTextBox` — Text mit Rahmen darstellen.

### CLI-Argumente
`ArgsInterpreter.kt`

- `ArgsInterpreter(args).getValue(key)` / `getValueAsInt` / `getValueAsDouble` / `getValueAsBoolean` — liest `key=value`-Argumente aus `args` (Anführungszeichen für Werte mit Leerzeichen).
- `containsParam(name)` — prüft auf Flag-Parameter (`-name`) ohne Wert.

### Reflektion, System & Sonstiges
`OtherExtensions.kt`, `StaticFunctions.kt`, `StaticAndOther.kt`, `SystemProperties.kt`

- `Any.TAG` — Klassenname (für Logging-Tags).
- `Any.cast<C>()` / `safeCast<C>()` / `letCast<C, R> { }` — Cast-Hilfsfunktionen.
- `ByteArray.toHexString()` — Hex-Darstellung.
- `getHeapInfo()` — aktuelle Heap/RAM-Nutzung der JVM (`HeapInfo`).
- `getRandomString(length, allowedChars)` — Zufalls-String.
- `String.calcBcc(ignoreFirstCharacter)` — Block Check Character (Prüfsumme) berechnen.
- `getCurrentMethodName()` / `Any.getCurrentClassAndMethodName()` — aktuelle Methode/Klasse per `StackWalker` (für Logging).
- `runsAsJar(any)` — erkennt, ob die App aus einem Jar heraus läuft.
- `SystemProperties` — Kurzzugriffe auf System-Properties: `getUserHome()`, `userWorkingDir()`, `osName()`, `osVersion()`, `osArch()`, `javaVersion()`, `javaHome()`, `fileSeparator()`, `pathSeparator()`, …

### Weitere
`Permutations.kt`, `IBAN.kt`, `Image.kt`, `Languages.kt`, `Serialization.kt`, `ResultOf.kt`, `enums/AsciiNP.kt`

- `CharArray.createPermutations(length)` / `createPermutationsMulti(from, until)` / `String.createPermutations(...)` — alle Permutationen einer Zeichenmenge erzeugen; Multi-Thread-Variante für mehrere Längen gleichzeitig (z. B. für Brute-Force-Tests eigener Passwortstärke).
- `IBAN.isValidIban(iban)` / `formatIban(iban)` / `getCountryCode(iban)` / `getCheckDigits(iban)` — IBAN-Validierung (Mod-97) und -Formatierung; auch als `String.isValidIban()`.
- `scaleImage(...)` / `BufferedImage.scaleImage(...)` — Bilder skalieren.
- `ByteArray.asBufferedImageOrNull()` / `BufferedImage.toByteArray(format)` — Konvertierung zwischen `ByteArray` und `BufferedImage`.
- `Languages.BCP47` — Enum bekannter Sprach-/Regions-Codes nach BCP 47.
- `T.serializeToString()` / `serializeToByteArray()` / `String.deserializeFromStringOrNull<T>()` / `ByteArray.deserializeFromByteArrayOrNull<T>()` — Java-Serialization (für `Serializable`-Objekte) als Base64-String oder ByteArray.
- `ResultOf<T>` / `ResultOf2<T, V>` / `ResultOf3<T, V, X>` / `ResultOfEmpty<T>` / `ResultOfTripple<T, V, M>` — sealed-class-Familie für Erfolg/Fehler (bzw. Erfolg/Warnung/Fehler) statt Exceptions als Rückgabewert. **Durchgängiges Fehlerbehandlungs-Idiom der Bibliothek** — beim Konsumieren von KotNexLib-APIs meist einer dieser Typen statt eines Throws.
- `Result<T>.finally { }` — try/catch/finally-ähnliches Verhalten für `runCatching`-Ergebnisse, verkettbar.
- `AsciiNP` — Enum aller nicht druckbaren ASCII-Zeichen (z. B. `ESC`, für Terminal-Steuerzeichen).

---

## kotnexlib.crypto

- `AES.CBC` / `AES.GCM` / `AES.ECB` — Ver-/Entschlüsselung mit eigenem Key (`AES.Common.generateAESKey()`); GCM empfohlen (authentifiziert).
- `AES.CBC.encryptWithPassword(text, password)` / `AES.GCM.encryptWithPassword(...)` — passwortbasierte Verschlüsselung (PBKDF2-HMAC-SHA256, 600 000 Iterationen), Ergebnis ist `AESData`.
- `AES.AESData` — Container für Chiffretext + Metadaten (IV, Salt, Modus, Iterationen). `toString()` liefert **einen** speicherbaren Base64-String, `AESData.restore(string)` rekonstruiert ihn wieder; `decryptAsString(password)` / `decryptAsByteArray(password)` direkt am Objekt. Format ist stabil — nicht selbst nachbauen, sondern immer über diese Klasse.
- `Argon2Helper.hash(password, iterations, memoryKb, parallelism)` / `hashForMobileDevices(password)` — Argon2id-Passwort-Hashing (PHC-Format, `CharArray` statt `String` fürs Passwort).
- `Argon2Helper.verify(password, encodedHash)` — prüft ein Passwort gegen einen Argon2-Hash.
- `BlowfishEncryption.encrypt(...)` / `decrypt(...)` — Blowfish, nur für Altsysteme (Annotation `@CriticalAPI`, bewusst nicht für neuen Code).
- `String.hash(HashAlgorithm)` — MD5/SHA-1/SHA-256/… Hash eines Strings.
- `String.hashBC(HashAlgorithmBC)` — Hash über BouncyCastle-Algorithmen (z. B. SHA3).
- `String.hashIter(algorithm, iterations)` — wiederholtes Hashing (Iterationskette).

## kotnexlib.security

- `TOTP.Registration.generateSecret(length)` — erzeugt ein Base32-Secret für 2FA-Apps (Google Authenticator, 1Password, …).
- `TOTP.Verification.serverVerifyCode(secret, code, window, timeMillis)` — prüft einen 6-stelligen TOTP-Code serverseitig (Zeitfenster-Toleranz, timing-attack-geschützter Vergleich).
- `TOTP.Verification.clientGenerateCode(...)` — erzeugt selbst einen Code (z. B. zu Testzwecken).

## kotnexlib.storage

- `SshStorage.init(sshKey, user, host, port)` — einmalige Konfiguration für SSH/SCP-Zugriff (benötigt `ssh`/`scp` im PATH und einen SSH-Key; kein Passwort-Login unterstützt).
- `SshStorage.upload(byteArray|text|File, destinationPath)` — Datei/Text/ByteArray auf den Remote-Host hochladen.
- `SshStorage.downloadFile(remotePath, localDestination)` / `downloadFolder(...)` — Herunterladen per SCP.
- `SshStorage.deleteFile(remotePath)` / `deleteFolder(remotePath)` — Löschen auf dem Remote-Host.
- `SshStorage.listFiles(directoryPath)` — listet Remote-Verzeichnisinhalt als `RemoteFile`-Liste.
- `SshStorage.Encryption` — verschlüsseltes Hoch-/Herunterladen (Kombination aus Upload + AES).
- `RemoteFile` — Datenklasse für eine Remote-Datei/-Ordner: `delete()`, `download(localDestination)`, `downloadAndDecrypt(password)`. Unterstützt auch Hetzner Storage Box.

## kotnexlib.external (Ktor & sonstige Integrationen)

- `Application.checkApiKey(apiKeys, ignorePaths)` — Middleware, prüft eingehende Requests gegen erlaubte API-Keys (mit Pfad-Ausnahmen und Validierungsart `EXACT`/`PREFIX`).
- `Application.serverSelfUpdate(password, serverJarPath, restartScriptPath, updateEndpoint, ...)` — Endpoint, über den ein laufender Server sein eigenes JAR per Upload ersetzt (Backup der Vorversion, Größenvalidierung gegen Fehl-Uploads, optionales Neustart-Skript, fertige Status-HTML-Seiten).
- `UniversalServerStorage.init(uniqueId, baseUrl, apiKey, basicUsername, basicPassword)` — konfiguriert einen generischen Client für eine ObjectBox-gestützte Server-Speicherschicht (CRUD, liefert `ResultOf2<Data, Error>` bzw. `ResultOfEmpty<Error>`).
- `EnumAsIntSerializer<T>` / `EnumAsStringSerializer<T>` — kotlinx.serialization-Serializer, um Enums als `Int` bzw. `String` zu (de)serialisieren statt als Objekt.
- `QRCodeBuilder.setErrorCorrectionAndSize(QRCodeErrorCorrection)` — steuert Fehlerkorrektur/Größe eines QR-Codes (`g0dkar/qrcode-kotlin`) über sprechende Stufen statt Zahlenwerte zu raten. **Deprecated seit 4.3.0** — nicht mehr für neuen Code verwenden.
- `KtorACME.kt` enthält ein experimentelles **privates** (nicht öffentlich zugängliches) Let's-Encrypt-Plugin, laut Code-Kommentar "AI generated, not checked or validated yet" — nicht Teil der nutzbaren API, ignorieren.

## kotnexlib.external.objectbox

- `CryptoStringEncryptionWithPassword` — `PropertyConverter` für ObjectBox: Feld wird beim Speichern automatisch AES-verschlüsselt/entschlüsselt (`password` und optional `compress` vorher setzen). Nutzung: `@Convert(converter = CryptoStringEncryptionWithPassword::class, dbType = String::class)` auf einem Entity-Feld.
- `ObjectBoxHashStringConverter` / `ObjectBoxArgon2HashStringConverter` — `PropertyConverter`: Feld wird beim Speichern gehasht statt im Klartext abgelegt (SHA-256 bzw. Argon2id); `toHash(input)` erzeugt denselben Hash zum Vergleich (z. B. Login-Query).
- `Query<T>.findAndClose(maxLogs)` / `findFirstAndClose()` / `removeAndClose()` — ObjectBox-Query ausführen und automatisch schließen.
- `Query<T>.doForEachPages(pageSize, maxLimit) { page -> }` — verarbeitet große Ergebnismengen seitenweise statt alles auf einmal zu laden.
- `Box<T>.contains(condition)` / `findOrNull(condition)` / `findAll(condition)` — Kurzformen für häufige ObjectBox-Queries.

## file (eigenes Package, **nicht** unter `kotnexlib`)

- `BaseFolder(path, name)` — legt ein Arbeitsverzeichnis an/prüft es; Basis für die übrigen Klassen in diesem Package.
- `LogFile` — rotierendes Logfile (Größenlimit via `LogSizeSettings`, alte Logs werden verschoben); `writeLog(msg, throwable)` / `plainLog(msg)`.
- `ConfigFile<T>(default, loadConfig, storeConfig)` — Config-Objekt wird aus/in eine Datei geladen/gespeichert, eigene Lade-/Speicherfunktion frei wählbar (z. B. kotlinx.serialization); `reloadConfig()` / `storeNewConfig(new)`.
- `KeyValueConfigFile` — fertige `ConfigFile`-Variante für einfache `key=value`-Dateien ohne eigenes Serialisierungs-Setup.
- `BaseFolderWithLogAndConfig<T>` — kombiniert `BaseFolder` + `LogFile` + `ConfigFile<T>` in einer Klasse (`log`, `configFile`, `getConfig()`) — Standardwahl für ein "Arbeitsverzeichnis mit Logging und Config" in einer Zeile.
- `File.existsDir(createIfNotExist)` / `existsFile(createIfNotExist)` — Existenz-Check mit optionalem Anlegen.
- `File.zipFiles(...)` / `File.unzipFile(...)` — ZIP-Erstellung/-Extraktion.
- `File.isZipFile()` — prüft Magic Bytes, ob es sich um ein ZIP handelt.
- `File.useAndDelete { }` — nutzt die Datei innerhalb des Blocks und löscht sie danach garantiert.

## kpub (eigenes Package)

- `KPubClient(serverUrl, token, port)` — Client für den KPub-Dienst; sendet Mail/SMS/beides über einen Aufruf (`SendType`, `SendRequest`), kapselt den Ktor-HTTP-Client (`AutoCloseable`, also mit `use { }` verwenden oder `close()` aufrufen).
