package kotnexlib

import kotlin.system.measureTimeMillis

/**
 * Measures the time like [measureTimeMillis]. But this method can return anything including the execution time.
 */
inline fun <T> measureTimeMillisAndReturn(block: () -> T): ResultTimeMeasure<T> {
    val result: T
    val time = measureTimeMillis {
        result = block()
    }
    return ResultTimeMeasure(result, time)
}

/**
 * Measures the time like [measureTimeMillis]. But this method can return anything including the execution time through [ResultTimeMeasure].
 * Does the exact same as [measureTimeMillisAndReturn] but uses a shorter name.
 */
inline fun <T> measureTime(block: () -> T) = measureTimeMillisAndReturn(block)

data class ResultTimeMeasure<T>(val result: T, val timeMillis: Long)

/**
 * Executes action as long as it returns not null.
 * If action returns null, the callback onNull will be called.
 *
 * @return The first Object, which ist not null.
 */
inline fun <reified T> doUntilNotNull(noinline onNull: (() -> Unit)? = null, action: () -> T?): T {
    while (true) {
        val result = action()
        if (result == null) onNull?.invoke() else return result
    }
}

/**
 * Measures the execution time of the provided block of code and allows handling the elapsed time.
 * By default, the elapsed time is printed to the console if no custom handler is provided.
 *
 * @param T The return type of the block of code to be measured.
 * @param time A callback function invoked with the measured time in milliseconds. Defaults to printing the time.
 * @param block The block of code whose execution time is to be measured.
 * @return The result of the executed block of code.
 */
inline fun <reified T> measureTimeSilent(time: (Long) -> Unit = { println("Time: $it ms") }, block: () -> T): T {
    val start = System.currentTimeMillis()
    val result = block()
    time(System.currentTimeMillis() - start)
    return result
}


//inline fun <reified T> doUntil(condition: () -> Boolean, noinline onConditionNotFulfilled: (() -> Unit)? = null, action: () -> T?): T {
//    while (condition()) {
//        action()?.let {
//            return it
//        } ?: onConditionNotFulfilled?.invoke()
//    }
//}








