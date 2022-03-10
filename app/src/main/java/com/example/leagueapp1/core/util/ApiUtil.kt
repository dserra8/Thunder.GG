package com.example.leagueapp1.core.util

import com.example.leagueapp1.util.DispatcherProvider
import com.example.leagueapp1.util.StandardDispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response


class ApiUtil(
   val dispatchers: DispatcherProvider
) {

    /**
     * Function to make safe api calls and returns a kotlin Result.
     */
   suspend inline fun <T> safeApiCall(crossinline responseFunc: suspend () -> Response<T>): Result<T> {

        val response: Response<T>
        try {
            response = withContext(dispatchers.io) {
                responseFunc.invoke()
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!response.isSuccessful) {
            return Result.failure(Exception(response.errorBody().toString()))
        } else {
            if (response.body() == null) {
                return Result.failure(Exception("Unknown Error"))
            }
        }

        return Result.success(response.body()!!)
    }
}