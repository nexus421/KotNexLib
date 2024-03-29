package kotnexlib

sealed class ResultOf<out T> {
    data class Success<out R>(val value: R): ResultOf<R>()
    data class Failure(val message: String?, val throwable: Throwable? = null): ResultOf<Nothing>()
}

sealed class ResultOf2<out T, out V> {
    data class Success<out E>(val value: E): ResultOf2<E, Nothing>()
    data class Failure<out Q>(val value: Q): ResultOf2<Nothing, Q>()
}

sealed class ResultOf3<out T, out V, out X> {
    data class Success<out E>(val value: E): ResultOf3<E, Nothing, Nothing>()
    data class Failure<out Q, out I>(val value: Q, val value2: I): ResultOf3<Nothing, Q, I>()
}

sealed class ResultOfEmpty<out T> {
    data object Success : ResultOfEmpty<Nothing>()
    data class Failure<out T>(val value: T): ResultOfEmpty<T>()
}

sealed class ResultOfTripple<out T, out V, out M> {
    data class Success<T>(val value: T) : ResultOfTripple<T, Nothing, Nothing>()
    data class Warning<T>(val value: T) : ResultOfTripple<Nothing, T, Nothing>()
    data class Error<T>(val value: T) : ResultOfTripple<Nothing, Nothing, T>()
}
