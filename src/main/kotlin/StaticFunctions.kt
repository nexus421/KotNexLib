import kotlinx.coroutines.delay


/**
 * Calculates the Heap-Memory aka RAM for this App in Megabyte
 * If the Heap is full and your App requests more than is available, the App will throw an OutOfMemory-Exception
 */
fun getHeapInfo(): HeapInfo {
    val runtime = Runtime.getRuntime()
    val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
    val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
    val availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB

    return HeapInfo(usedMemInMB, maxHeapSizeInMB, availHeapSizeInMB)
}

data class HeapInfo(val usedMemInMB: Long, val maxHeapSizeInMB: Long, val availableHeapSizeInMB: Long)

/**
 * Creates a random String from allowedChars with a given length
 *
 * @param length of the created string
 * @param allowedChars chars which can be used for creating
 */
fun getRandomString(length: Int, allowedChars: List<Char> = ('A'..'Z') + ('a'..'z') + ('0'..'9')) = (1..length)
    .map { allowedChars.random() }
    .joinToString("")

/**
 * Calculates the block check character (BCC) for error detection.
 * This BCC is calculated by XOR-ing each byte with the result of the previous XOR.
 *
 * @return BCC result as String
 */
fun String.calcBcc(ignoreFirstCharacter: Boolean = true): String {

    //Der erste character muss ignoriert werden.
    //Eigentlich nur das erste STX oder SOH
    var cbcChar = this[if (ignoreFirstCharacter) 1 else 0].code
    for (i in (if (ignoreFirstCharacter) 2 else 1) until length) {
        cbcChar = cbcChar xor this[i].code
    }
    return cbcChar.toChar().toString()
}

/**
 * This method executes [doThis]. If an [Throwable] will be thrown, [doThis] will be executed again after [waitBetweenRetryMillis] as long as all [attempts] are over.
 *
 * @param doubleWaitingTimeOnRetry if true, the time from [waitBetweenRetryMillis] will be doubled on each new try.
 * @param onError If not null, will be called with the thrown [Throwable].
 *
 * @return [T] if any attempt succeeded or null if all attempts failed.
 */
suspend fun <T> retryOnError(
    attempts: Int = 3,
    waitBetweenRetryMillis: Long = 100,
    doubleWaitingTimeOnRetry: Boolean = false,
    onError: ((Throwable) -> Unit)? = null,
    doThis: suspend () -> T
): T? = try {
    doThis()
} catch (t: Throwable) {
    onError?.let { it(t) }
    if (attempts > 0) {
        if (waitBetweenRetryMillis > 0) delay(waitBetweenRetryMillis)
        retryOnError(
            attempts - 1,
            waitBetweenRetryMillis * if (doubleWaitingTimeOnRetry) 2 else 1,
            doubleWaitingTimeOnRetry,
            onError,
            doThis
        )
    } else null
}


/**
 * This method executes [doThis]. If an [Throwable] will be thrown, [doThis] will be executed again after [waitBetweenRetryMillis] as long as all [attempts] are over.
 *
 * @param doubleWaitingTimeOnRetry if true, the time from [waitBetweenRetryMillis] will be doubled on each new try.
 * @param onError If not null, will be called with the thrown [Throwable].
 *
 * @throws [Throwable] if all [attempts] failed
 * @return [T] if any attempt succeeded
 */
suspend fun <T> retryOnErrorOrThrow(
    attempts: Int = 3,
    waitBetweenRetryMillis: Long = 100,
    doubleWaitingTimeOnRetry: Boolean = false,
    onError: ((Throwable) -> Unit)? = null,
    doThis: suspend () -> T
): T = try {
    doThis()
} catch (t: Throwable) {
    onError?.let { it(t) }
    if (attempts > 0) {
        if (waitBetweenRetryMillis > 0) delay(waitBetweenRetryMillis)
        retryOnErrorOrThrow(
            attempts - 1,
            waitBetweenRetryMillis * if (doubleWaitingTimeOnRetry) 2 else 1,
            doubleWaitingTimeOnRetry,
            onError,
            doThis
        )
    } else throw t
}


