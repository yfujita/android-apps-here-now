package io.github.yfujita.herenow.domain.model

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()

    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            is Error -> null
        }

    fun getOrDefault(default: @UnsafeVariance T): T =
        when (this) {
            is Success -> data
            is Error -> default
        }

    fun <R> map(transform: (T) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }

    suspend fun <R> mapSuspend(transform: suspend (T) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)

        fun error(
            message: String,
            exception: Throwable? = null,
        ): Result<Nothing> = Error(message, exception)
    }
}

inline fun <T> runCatching(block: () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error", e)
    }
}

suspend inline fun <T> runCatchingSuspend(crossinline block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error", e)
    }
}
