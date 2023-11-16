package kotnexlib

/**
 * Returns a map with all entries from the left/calling map, which keys are not contained in [rightMap].
 *
 * @return a new map that contains only the entries from the current map with keys that are not present in [rightMap]
 */
fun <K, V> Map<K, V>.keysOnlyLeft(rightMap: Map<K, V>): Map<K, V> {
    val mapWithKeysOnlyFromLeft = mutableMapOf<K, V>()

    forEach {
        if (rightMap.containsKey(it.key).not()) mapWithKeysOnlyFromLeft[it.key] = it.value
    }

    return mapWithKeysOnlyFromLeft
}

/**
 * Returns a map with all entries from [rightMap] map, which keys are not contained in the this/calling map
 *
 * @return a new map that contains only the entries from [rightMap] with keys that are not present in this map
 */
fun <K, V> Map<K, V>.keysOnlyRight(rightMap: Map<K, V>): Map<K, V> {
    val mapWithKeysOnlyFromRight = mutableMapOf<K, V>()

    rightMap.forEach {
        if (containsKey(it.key).not()) mapWithKeysOnlyFromRight[it.key] = it.value
    }

    return mapWithKeysOnlyFromRight
}

/**
 * Returns all entries, which keys are in both maps present but have different values.
 */
fun <K, V> Map<K, V>.entriesDiffering(rightMap: Map<K, V>): Map<K, DifferingValues<V?>> {
    val mapSameKeysDifferingValues = mutableMapOf<K, DifferingValues<V?>>()

    forEach {
        if (rightMap.containsKey(it.key) && rightMap[it.key] != it.value) mapSameKeysDifferingValues[it.key] =
            DifferingValues(it.value, rightMap[it.key])
    }

    return mapSameKeysDifferingValues
}

/**
 * Holds the data from two maps which have the same key with different values.
 * Used with [Map.entriesDiffering].
 */
data class DifferingValues<T>(val valueLeftMap: T, val valueRightMap: T)

/**
 * @return true if both maps are equal. Means: There is no difference between them.
 */
fun <K, V> Map<K, V>.areEqual(rightMap: Map<K, V>) =
    keysOnlyLeft(rightMap).isEmpty() && keysOnlyRight(rightMap).isEmpty() && entriesDiffering(rightMap).isEmpty()