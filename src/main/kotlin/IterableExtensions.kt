
typealias LastElement = Boolean

/**
 * Iterates this Iterable and calls action.
 * If the current iteration is the last one, the Boolean will be true, false otherwise.
 *
 * @param action Callback for each iteration. T == Element, Boolean == IsLastElement
 */
inline fun <T> Iterable<T>.forEachDoLast(action: (T, LastElement) -> Unit) {
    val end = count()
    forEachIndexed { index, t ->
        action(t, index == end - 1)
    }
}

inline fun <T> Iterable<T>.contains(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

/**
 * Splits this list into two.
 *
 * @param predicate condition. T is the current element. On true fills trueList, on false fills falseList
 *
 * @return SplitList containing splitted list
 */
inline fun <T> Iterable<T>.splitFilter(predicate: (T) -> Boolean): SplitList<T> {
    val trueList = mutableListOf<T>()
    val falseList = mutableListOf<T>()

    forEach {
        if (predicate(it)) trueList.add(it) else falseList.add(it)
    }

    return SplitList(trueList, falseList)
}

/**
 * If the size of this collection is 1, than [sizeIsOne] will be called with this value.
 */
inline fun <T> Collection<T>.ifSizeIsOne(sizeIsOne: (T) -> Unit) {
    if (size == 1) sizeIsOne(first())
}

/**
 * @return true, if [size] is equal to [Collection.size]
 */
fun <T> Collection<T>.sizeIs(size: Int) = this.size == size

/**
 * Checks if the collection has the size of [size]. If so, then [sizeIs] will be called with all values.
 * If [size] is 1, you may use [ifSizeIsOne].
 */
inline fun <T> Collection<T>.ifSizeIs(size: Int, sizeIs: (Collection<T>) -> Unit) {
    if (this.size == size) sizeIs(this)
}

/**
 * Checks if the collection has not the size of [size]. If so, then [sizeIsNot] will be called with all values.
 */
inline fun <T> Collection<T>.ifSizeIsNot(size: Int, sizeIsNot: (Collection<T>) -> Unit) {
    if (this.size != size) sizeIsNot(this)
}


data class SplitList<T>(val trueList: List<T>, val falseList: List<T>)