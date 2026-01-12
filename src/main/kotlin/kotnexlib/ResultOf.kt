package kotnexlib

sealed class ResultOf<out T> {
    data class Success<out R>(val value: R): ResultOf<R>()
    data class Failure(val message: String?, val throwable: Throwable? = null): ResultOf<Nothing>()

    /**
     * Returns the encapsulated value if this instance represents [ResultOf2.Success] or null if it is [ResultOf2.Failure].
     */
    fun getOrNull(): T? = (this as? Success)?.value

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

sealed class ResultOf2<out T, out V> {
    data class Success<out E>(val value: E): ResultOf2<E, Nothing>()
    data class Failure<out Q>(val value: Q): ResultOf2<Nothing, Q>()

    /**
     * Returns the encapsulated value if this instance represents [ResultOf2.Success] or null if it is [ResultOf2.Failure].
     */
    fun getOrNull(): T? = (this as? Success)?.value
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

sealed class ResultOf3<out T, out V, out X> {
    data class Success<out E>(val value: E): ResultOf3<E, Nothing, Nothing>()
    data class Failure<out Q, out I>(val value: Q, val value2: I): ResultOf3<Nothing, Q, I>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

sealed class ResultOfEmpty<out T> {
    data object Success : ResultOfEmpty<Nothing>()
    data class Failure<out T>(val value: T): ResultOfEmpty<T>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

sealed class ResultOfTripple<out T, out V, out M> {
    data class Success<T>(val value: T) : ResultOfTripple<T, Nothing, Nothing>()
    data class Warning<T>(val value: T) : ResultOfTripple<Nothing, T, Nothing>()
    data class Error<T>(val value: T) : ResultOfTripple<Nothing, Nothing, T>()

    val isSuccess: Boolean get() = this is Success
    val isWarning: Boolean get() = this is Warning
    val isError: Boolean get() = this is Error
}

/**
 * Will always be executed after [runCatching] was called.
 * It gives you a more traditional way to handle exceptions like try/catch/finally.
 *
 * This should always be used as the last block!
 *
 * @param runCatching If true, the [doThis] block will be executed in a try-catch block. Exceptions will be caught and ignored. Use this if you want to chain multiple [finally] results and avoid errors within one [finally] block to avoid running the others.
 * @param doThis The action to be executed.
 */
inline fun <T> Result<T>.finally(runCatching: Boolean = false, doThis: Result<T>.() -> Unit): Result<T> {
    if (runCatching) tryOrNull { doThis() } else doThis()
    return this
}