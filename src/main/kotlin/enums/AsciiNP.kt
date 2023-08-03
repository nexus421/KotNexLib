package enums

/**
 * This enum represents all non-printable ASCII-Characters
 *
 * @param asText Non printable.. But some Systems print them as squares with the name in it. Or you need this within a string.
 * @param asHex Ascii as a String-Hex-Value
 * @param asUInt Ascii as a simple unsigned Int value
 */
enum class AsciiNP(val asText: String, val asHex: String, val asUInt: Int) {
    NUL("\u0000", "00", 0),
    SOH("\u0001", "01", 1),
    STX("\u0002", "02", 2),
    ETX("\u0003", "03", 3),
    EOT("\u0004", "04", 4),
    ENQ("\u0005", "05", 5),
    ACK("\u0006", "06", 6),
    BEL("\u0007", "07", 7),
    BS("\u0008", "08", 8),
    HT("\u0009", "09", 9),
    LF("\u000A", "0A", 10),
    VT("\u000B", "0B", 11),
    FF("\u000C", "0C", 12),
    CR("\u000D", "0D", 13),
    SO("\u000E", "0E", 14),
    SI("\u000F", "0F", 15),
    DLE("\u0010", "10", 16),
    DC1("\u0011", "11", 17),
    DC2("\u0012", "12", 18),
    DC3("\u0013", "13", 19),
    DC4("\u0014", "14", 20),
    NAK("\u0015", "15", 21),
    SYN("\u0016", "16", 22),
    ETB("\u0017", "17", 23),
    CAN("\u0018", "18", 24),
    EM("\u0019", "19", 25),
    SUB("\u001A", "1A", 26),
    ESC("\u001B", "1B", 27),
    FS("\u001C", "1C", 28),
    GS("\u001D", "1D", 29),
    RS("\u001E", "1E", 30),
    US("\u001F", "1F", 31);

    val asChar = asUInt.toChar()

    /**
     * Only returns [Enum.name]
     */
    val printableName = name

}
