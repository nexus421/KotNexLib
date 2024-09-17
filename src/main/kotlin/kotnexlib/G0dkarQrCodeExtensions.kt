package kotnexlib

import qrcode.QRCodeBuilder

/**
 * Sets the error correction for the QR-Code which results directly in different QR-Code sizes!
 * The lower the value, the lower is the error correcting possibilities for the QR-Code. But the QR-Code will be also smaller!
 * Try it by yourself and choose for your needs.
 *
 * The default used value is VeryHigh(6).
 * I would recommend Low or Medium.
 *
 * Needs at least implementation("io.github.g0dkar:qrcode-kotlin:4.2.0")!
 */
enum class QRCodeErrorCorrection(val value: Int) {
    LowestCorrectionAndSize(0),
    LowerCorrectionAndSize(1),
    LowCorrectionAndSize(2),
    MediumCorrectionAndSize(3),
    MediumHighCorrectionAndSize(4),
    HighCorrectionAndSize(5),
    VeryHighCorrectionAndSize(6),
    UltraCorrectionAndSize(10)
}

/**
 * Set the error correction for this QR-Code which directly impacts the QR-Code size.
 *
 * The built-in method for error correction does not work, because of the [QRCodeBuilder.minTypeNum].
 * So we have to change the minTypeNum by ourselves. But what number should you choose and what are the drawbacks?
 * This method will save you! Simply choose from [QRCodeErrorCorrection]
 *
 * You can set a custom value if you use the [QRCodeBuilder.withMinimumInformationDensity] directly. This method should only help you,
 * so you don't need to try numbers and avoid confusion.
 *
 * Needs at least implementation("io.github.g0dkar:qrcode-kotlin:4.2.0") !
 *
 * @param quality choose your desired quality and size from [QRCodeErrorCorrection]
 */
fun QRCodeBuilder.setErrorCorrectionAndSize(quality: QRCodeErrorCorrection) {
    withMinimumInformationDensity(quality.value)
}
