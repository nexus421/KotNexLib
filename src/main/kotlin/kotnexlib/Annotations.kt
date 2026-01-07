package kotnexlib

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This API is experimental and may change in the future. Use at your own risk."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalKotNexLibAPI(val reason: String = "")

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is experimental or not save to use (anymore). Use at your own risk."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CriticalAPI(val reason: String)
