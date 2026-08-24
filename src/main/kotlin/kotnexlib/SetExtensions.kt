package kotnexlib

/**
 * This method returns a set containing all elements that are contained by this and not contained by [otherSet].
 * Note: [otherSet] may also contain elements not present in this, these are simply ignored.
 */
@Deprecated(
    message = "Use Kotlin standard library subtract() or '-' operator instead",
    replaceWith = ReplaceWith("this.subtract(otherSet)")
)
fun <T> Set<T>.difference(otherSet: Set<T>) = mapNotNull { if (otherSet.contains(it)) null else it }.toSet()