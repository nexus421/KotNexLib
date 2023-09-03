

import kotlin.system.measureTimeMillis

/**
 * Measures the time like [measureTimeMillis]. But this method can return anything including the execution time.
 */
fun <T> measureTimeMillisAndReturn(block: () -> T): ResultTimeMeasure<T> {
    val result: T
    val time = measureTimeMillis {
        result = block()
    }
    return ResultTimeMeasure(result, time)
}

/**
 * Measures the time like [measureTimeMillis]. But this method can return anything including the execution time.
 * Does the exact same as [measureTimeMillisAndReturn] but uses a shorter name.
 */
fun <T> measureTime(block: () -> T): ResultTimeMeasure<T> {
    val result: T
    val time = measureTimeMillis {
        result = block()
    }
    return ResultTimeMeasure(result, time)
}

data class ResultTimeMeasure<T>(val result: T, val timeMillis: Long)

/**
 * Executes action as long as it returns not null.
 * If action returns null, the callback onNull will be called.
 *
 * @return The first Object, which ist not null.
 */
inline fun <reified T> doUntilNotNull(noinline onNull: (() -> Unit)? = null, action: () -> T?): T {
    while (true) {
        action()?.let {
            return it
        } ?: onNull?.invoke()
    }
}








