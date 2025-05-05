package kotnexlib

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.annotations.Range
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Emits a continuous flow of durations representing the elapsed time between emissions.
 * The rate of emissions is determined by the `emissionsPerSecond` parameter.
 *
 * @param emissionsPerSecond The number of emissions per second. Must be between 1 and `Int.MAX_VALUE`. Defaults to 1.
 * @return A flow emitting durations representing the elapsed time between each emission.
 */
fun timeAndEmit(emissionsPerSecond: @Range(from = 1L, to = Int.MAX_VALUE.toLong()) Int = 1): Flow<Duration> {
    return flow {
        val interval = 1000L / emissionsPerSecond
        var lastEmitTime = System.currentTimeMillis()
        emit(Duration.ZERO)
        while (true) {
            delay(interval)
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - lastEmitTime

            emit(elapsedTime.milliseconds)
            lastEmitTime = currentTime
        }
    }
}