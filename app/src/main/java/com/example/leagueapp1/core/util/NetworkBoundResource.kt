package com.example.leagueapp1.core.util

import kotlinx.coroutines.flow.*

inline fun <ResultType> networkBoundResource(
    crossinline query: suspend () -> Flow<ResultType>,
    crossinline sync: suspend () -> Unit,
//    crossinline fetch: suspend () -> RequestType,
//    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: suspend (ResultType) -> Boolean
) = flow {
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.Loading(data))

        try {
            sync()
            query().map { Resource.Success(it) }
        } catch (throwable: Throwable) {
            query().map { Resource.Error(throwable, it) }
        }
    } else {
        query().map { Resource.Success(it) }
    }

    emitAll(flow)
}
