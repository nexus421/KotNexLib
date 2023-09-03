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
