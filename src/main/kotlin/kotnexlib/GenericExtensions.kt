package kotnexlib

/**
 * Replacement for if/else.
 * Use this to check if a variable is null or not and return something.
 *
 * @param isNull Will be called when this is null. Return Anything.
 * @param notNull Will be called when this is not null with this as parameter. Return Anything.
 */
inline fun <T, R> T?.ifNull(isNull: () -> R, notNull: T.() -> R): R = if (this == null) isNull() else notNull()

inline fun <T> T?.ifNotNull(thisObj: T.() -> Unit) {
    if (this != null) thisObj()
}

inline fun <T> T?.ifNull(isNull: () -> Unit) {
    if (this == null) isNull()
}

fun <T> T?.isNull() = this == null
fun <T> T?.isNotNull() = this != null

/**
 * Default-Wert angeben. Wenn ein Objekt null sein kann, kann damit ein default-Wert angegeben werden.
 * @return Default-Wert, wenn this == null
 */
fun <T> T?.default(value: T) = this ?: value

/**
 * Simple try-catch execution.
 * Tries to execute [tryThis] inside a try-catch block.
 *
 * @param onError if not null, will be called if the execution throws a [Throwable]
 * @param tryThis Block will be executed inside try-catch
 *
 * @return If it fails, it will only return null. Otherwise, the [tryThis] result
 */
inline fun <T> tryOrNull(noinline onError: ((Throwable) -> Unit)? = null, tryThis: () -> T?): T? {
    return try {
        tryThis()
    } catch (e: Throwable) {
        onError?.let { it(e) }
        null
    }
}

/**
 * Simple try-catch execution.
 * Tries to execute [tryThis] inside a try-catch block.
 *
 * @param onError if not null, will be called if the execution throws a [Throwable]
 * @param tryThis Block will be executed inside try-catch
 * @param default on any exception, this will be returned
 *
 * @return If it fails, it will return [default]. Otherwise, the [tryThis] result
 */
inline fun <T> tryOrDefault(noinline onError: ((Throwable) -> Unit)? = null, default: T, tryThis: () -> T): T {
    return try {
        tryThis()
    } catch (e: Throwable) {
        onError?.let { it(e) }
        default
    }
}

/**
 * See [tryOrNull].
 * @param tryThis Block will be executed inside try-catch with this.
 *
 * @return If it fails or this is null, it will return [default]. Otherwise, the [tryThis] result
 */
inline fun <T, K> K?.tryOrDefault(noinline onError: ((Throwable) -> Unit)? = null, default: T, tryThis: K.() -> T): T {
    return try {
        if (this == null) default else tryThis()
    } catch (e: Throwable) {
        onError?.let { it(e) }
        default
    }
}

/**
 * See [tryOrNull].
 * @param tryThis Block will be executed inside try-catch with this.
 *
 * If it fails or this is null, it will return null. Otherwise, the [tryThis] result
 */
inline fun <T, K> K?.tryOrNull(noinline onError: ((Throwable) -> Unit)? = null, tryThis: K.() -> T): T? {
    return try {
        if (this == null) null else tryThis()
    } catch (e: Throwable) {
        onError?.let { it(e) }
        null
    }
}