package com.example.leagueapp1.core.util

typealias SimpleResource = NoLoadResource<Unit>

sealed class Resource<T>(
    val data: T? = null,
    val error: Throwable? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(throwable: Throwable, data: T? = null) : Resource<T>(data, throwable)
}

sealed class NoLoadResource<T>(
    val data: T? = null,
    val error: Throwable? = null
) {
    class Success<T>(data: T) : NoLoadResource<T>(data)
    class Error<T>(throwable: Throwable, data: T? = null) : NoLoadResource<T>(data, throwable)
}