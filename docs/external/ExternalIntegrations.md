### External Integrations

Optional extensions for 3rd-party libraries. Dependencies are not included by default.

#### ObjectBox

- **AES Encryption**: `CryptoStringEncryptionWithPassword` allows storing encrypted strings in ObjectBox databases using
  PBKDF2 and AES/CBC.

#### Ktor

- **API Key Check**: Simple middleware to validate API keys and ignore specific paths.
- **Server Self-Update**: A feature to allow a running server to update its own JAR file and restart via a script.

#### QR Code

- Extensions for `g0dkar/qrcode-kotlin` to simplify QR code generation.

#### Serialization

- Helpers for `kotlinx.serialization` to handle common cases and default values.
