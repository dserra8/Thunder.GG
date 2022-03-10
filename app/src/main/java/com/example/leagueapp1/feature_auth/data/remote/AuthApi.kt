package com.example.leagueapp1.feature_auth.data.remote

import com.example.leagueapp1.core.data.remote.SimpleResponse
import com.example.leagueapp1.feature_auth.data.remote.request.AccountRequest
import com.example.leagueapp1.feature_auth.data.remote.request.LoginRequest
import com.example.leagueapp1.feature_auth.data.remote.response.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/register")
    suspend fun register(
        @Body registerRequest: AccountRequest
    ) : Response<RegisterResponse>

    @POST("/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ) : Response<SimpleResponse>

}