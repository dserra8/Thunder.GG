package com.example.leagueapp1.feature_auth.data.repository

import com.example.leagueapp1.core.data.remote.SimpleResponse
import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.util.ApiUtil
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.feature_auth.data.remote.AuthApi
import com.example.leagueapp1.feature_auth.data.remote.request.AccountRequest
import com.example.leagueapp1.feature_auth.data.remote.request.LoginRequest
import com.example.leagueapp1.feature_auth.domain.repository.AuthRepositoryInterface

class AuthRepository(
    private val api: AuthApi,
    private val apiUtil: ApiUtil
) : AuthRepositoryInterface {

    override suspend fun login(username: String, password: String): NoLoadResource<String> {
        val response = apiUtil.safeApiCall { api.login(LoginRequest(username, password)) }
        var result: NoLoadResource<String> = NoLoadResource.Error(Throwable("Unknown Error"))
        response.onSuccess {
            result = if (it.successful) {
                NoLoadResource.Success(it.puuid!!)
            } else {
                NoLoadResource.Error(Throwable(it.message), null)
            }
        }
        response.onFailure {
            result =
                NoLoadResource.Error(Throwable("Couldn't connect to the servers. Check your internet connection"))
        }
        return result
    }

    override suspend fun register(
        username: String,
        password: String,
        summonerName: String
    ): NoLoadResource<SummonerFromKtor> {
        val response =
            apiUtil.safeApiCall { api.register(AccountRequest(username, password, summonerName)) }
        var result: NoLoadResource<SummonerFromKtor> =
            NoLoadResource.Error(Throwable("Unknown Error"))
        response.onSuccess {
            result = if (it.successful) {
                NoLoadResource.Success(it.summoner!!)
            } else {
                NoLoadResource.Error(Throwable(it.message), null)
            }
        }
        response.onFailure {
            result =
                NoLoadResource.Error(Throwable("Couldn't connect to the servers. Check your internet connection"))
        }
        return result
    }

    override suspend fun authenticate(): SimpleResponse {
        TODO("Not yet implemented")
    }


}