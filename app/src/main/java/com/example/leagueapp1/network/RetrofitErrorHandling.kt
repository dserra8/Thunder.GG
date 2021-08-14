package com.example.leagueapp1.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

suspend inline fun <T> safeApiCall(crossinline responseFunc: suspend () -> Response<T>) : Result<T> {

    val response: Response<T>
    try {
        response = withContext(Dispatchers.IO) {
            responseFunc.invoke()
        }
    } catch (e: Exception) {
        return Result.failure(e)
    }

    if(!response.isSuccessful){
        return Result.failure(Exception(response.errorBody().toString()))
    } else {
        if (response.body() == null) {
            return Result.failure(Exception("Unknown Error"))
        }
    }

    return Result.success(response.body()!!)
}

enum class ErrorType{
    NETWORK, //IO
    TIMEOUT, //Socket
    UNKNOWN //Anything Else
}