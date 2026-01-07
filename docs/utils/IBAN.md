### IBAN Utilities

Functions for validating and formatting International Bank Account Numbers (IBAN).

#### Features

- `isValidIban(iban)`: Validates checksum and length. Also available as `String.isValidIban()`.
- `formatIban(iban)`: Groups IBAN characters with spaces every 4 digits for readability.
- `getCountryCode(iban)`: Extracts the country code.
- `getCheckDigits(iban)`: Extracts the check digits.
