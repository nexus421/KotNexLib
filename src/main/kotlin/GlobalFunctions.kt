

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
        action()?.let {
            return it
        } ?: onNull?.invoke()
    }
}

//inline fun <reified T> doUntil(condition: () -> Boolean, noinline onConditionNotFulfilled: (() -> Unit)? = null, action: () -> T?): T {
//    while (condition()) {
//        action()?.let {
//            return it
//        } ?: onConditionNotFulfilled?.invoke()
//    }
//}








