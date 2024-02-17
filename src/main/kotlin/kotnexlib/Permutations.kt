package kotnexlib

import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.properties.Delegates


/**
 * Creates all permutations from the given [CharArray] with the specified [length].
 * This will create [CharArray.size] to the power of [length] Strings!
 *
 * You should not use the same character multiple times in the Array.
 *
 * @param length creates only permutations of exact this length. Defaults to the size of this [CharArray]
 * @param doNotFillResultList if true, the result list will be always empty. You may only want to check the results through [onEachGeneration] and don't care about all possibilities.
 * @param onEachGeneration if not null, this will be called on each generated permutation. If the returned boolean is true, the execution will be canceled and the current results will be returned.
 *
 * @return an object with the time it took to calculate all possibilities and the possibilities as a List<String>
 */
fun CharArray.createPermutations(
    length: Int = size,
    doNotFillResultList: Boolean = false,
    onEachGeneration: ((String) -> Boolean)? = null
): ResultTimeMeasure<List<String>> {
    val possibilities = size.toDouble().pow(length)
    //Creates a Map, where each char as its own int to work with
    val charsToUseMap = mapIndexed { index, c -> Pair(index + 1, c) }.toMap()
    val maxIndexValue = size

    val results = mutableListOf<String>()
    //We only store Int here, so we can easily increment the Int by one to represent the next char.
    //Why? Because on 'a' my not follow 'b'.
    val cache = IntArray(length) { 1 }

    return measureTime {
        //Store the first initial result. Convert the Int with its char counterpart
        cache.joinToString("") { charsToUseMap[it].toString() }.let {
            doNotFillResultList.ifFalse { results.add(it) }
            onEachGeneration?.invoke(it)?.ifTrue { return@measureTime results }
        }

        //We will start at the right
        var indexToIncrement = length - 1
        while (true) {
            //If the indexToIncrement is smaller than 0, we incremented all entries to max. End!
            if (indexToIncrement < 0) break

            //Check if we reached the last char from the Map.
            //If true, we decrement the index by 1 to increment the char before the current one as we reached the last one there.
            //Else we increment the Int at the current indexToIncrement by one.
            if (cache[indexToIncrement] + 1 > maxIndexValue) {
                cache[indexToIncrement] = 1
                indexToIncrement--
            } else {
                cache[indexToIncrement] = cache[indexToIncrement] + 1
                //Store the first initial result. Convert the Int with its char counterpart
                cache.joinToString("") { charsToUseMap[it].toString() }.let {
                    doNotFillResultList.ifFalse { results.add(it) }
                    onEachGeneration?.invoke(it)?.ifTrue { return@measureTime results }
                }
                //Go back to the most right. Important to not miss any combination!
                indexToIncrement = length - 1
            }
        }
        if (results.size != possibilities.toInt()) System.err.println("There might be an error, as the result size and the calculatet size are not the same!")

        results
    }
}

/**
 * Does the same as [CharArray.createPermutations] but multi-threaded for each length between [from] and [until].
 * See [CharArray.createPermutations] for all information.
 *
 * @param onEachGeneration if not null, this will be called on each generated permutation. If the returned boolean is true, the execution will be canceled and the current results will be returned. This method is not synchronised!
 * @return a map with all permutations. The key is the length for the entries.
 */
fun CharArray.createPermutationsMulti(
    from: Int = 1,
    until: Int = size,
    doNotFillResultList: Boolean = false,
    onEachGeneration: ((String) -> Boolean)? = null
): Map<Int, ResultTimeMeasure<List<String>>> {
    if (from < 1) throw IllegalArgumentException("from is not allowed to be lower than 1.")

    val results = mutableMapOf<Int, ResultTimeMeasure<List<String>>>()
    var stopAll by Delegates.once(throwOnChangeTry = false, initialValue = false)

    val threads = mutableListOf<Thread>()
    until.downTo(from).forEach {
        threads.add(thread {
            results[it] = createPermutations(it, doNotFillResultList, if (onEachGeneration != null) { string ->
                val result = onEachGeneration(string)
                stopAll = result
                stopAll
            } else null)
        })
    }

    threads.forEach { it.join() }

    return results
}

/**
 * Shortcut for String. Uses [CharArray.createPermutations] under the hood. Look there for docs.
 */
fun String.createPermutations(
    length: Int = this.length,
    doNotFillResultList: Boolean = false,
    onEachGeneration: ((String) -> Boolean)? = null
) = toCharArray().createPermutations(length, doNotFillResultList, onEachGeneration)