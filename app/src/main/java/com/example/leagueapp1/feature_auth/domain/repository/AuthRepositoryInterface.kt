package com.example.leagueapp1.feature_auth.domain.repository

import com.example.leagueapp1.core.data.remote.SimpleResponse
import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.util.NoLoadResource

interface AuthRepositoryInterface {

    suspend fun login(username: String, password: String): NoLoadResource<String>

    suspend fun register(username: String, password: String, summonerName: String): NoLoadResource<SummonerFromKtor>

    suspend fun authenticate(): SimpleResponse
}