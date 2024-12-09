package kotnexlib
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

/**
 * Use this to easily handle the three most relevant (in my case) sizes to handle.
 *
 * Call this within a when expression to easily handle an empty collection and a collection with one element and a collection with more than one element.
 *
 * @return [IterableSize]. See those docs for more.
 */
fun <T : Any> Collection<T>.handleSizes(): IterableSize<T> {
    return if (isEmpty()) IterableSize.IsEmpty()
    else if (size == 1) IterableSize.IsOne(first())
    else IterableSize.HasMany(this)
}

/**
 * Iterates over a list and searches with [find] for an inner object inside this list.
 * You may use this to find an object inside a list inside a list: List<List<Any>> --> Would return Any?
 *
 * @return the first match [find] found or null if no mach was found.
 */
inline fun <T, K> Iterable<T>.findInner(find: (T) -> K?): K? {
    forEach {
        val result = find(it)
        if (result != null) return result
    }
    return null
}


/**
 * Manage the size-states from any collection through [handleSizes]
 */
sealed interface IterableSize<I> {

    /**
     * Will be used if the [Collection] is empty.
     */
    class IsEmpty<T> : IterableSize<T>

    /**
     * Will be used, if the [Collection] has exact one entry. This entry will be guaranteed accessible through [entry]
     */
    data class IsOne<T>(val entry: T) : IterableSize<T>

    /**
     * Will be used, if the [Collection] has more than one entry. All entries are accessible through [entries]
     */
    data class HasMany<T>(val entries: Collection<T>) : IterableSize<T>
}

/**
 * Removes [thisEntry] and inserts it at [toIndex].
 * If this list does not contain [thisEntry] or [toIndex] is out of bound, nothing will happen.
 *
 * @param thisEntry element to move inside this list.
 * @param toIndex new index to insert this element
 */
fun <T> MutableList<T>.move(thisEntry: T, toIndex: Int) {
    if (contains(thisEntry).not() || toIndex >= size) return
    remove(thisEntry)
    add(toIndex, thisEntry)
}

/**
 * Removes [thisEntry] and inserts it at [toIndex].
 * If [toIndex] is out of bound, nothing will happen.
 * If this list does not contain [thisEntry] it will be inserted at [toIndex].
 *
 * @param thisEntry element to move inside this list.
 * @param toIndex new index to insert this element
 */
fun <T> MutableList<T>.moveOrAdd(thisEntry: T, toIndex: Int) {
    if (toIndex >= size) return
    if (contains(thisEntry)) remove(thisEntry)
    add(toIndex, thisEntry)
}

/**
 * Checks if a given element comes before another element in the iteration.
 *
 * @param thisT The element to check if it comes before another element.
 * @param before A function that returns a boolean and determines which element is considered the "other" element.
 * @return `true` if the element `thisT` comes before the element determined by the `before` function, otherwise `false`.
 */
fun <T> Iterable<T>.isBefore(thisT: T, before: (T) -> Boolean): Boolean {
    val destination = indexOfFirst(before)
    return indexOf(thisT) < destination
}


/**
 * Checks if a given element comes after another element in the iteration.
 *
 * @param thisT The element to check if it comes after another element.
 * @param after A function that returns a boolean and determines which element is considered the "other" element.
 * @return `true` if the element `thisT` comes after the element determined by the `after` function, otherwise `false`.
 */
fun <T> Iterable<T>.isAfter(thisT: T, after: (T) -> Boolean): Boolean {
    val destination = indexOfFirst(after)
    return indexOf(thisT) > destination
}

