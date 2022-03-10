package com.example.leagueapp1.feature_auth.data.remote.response

import com.example.leagueapp1.core.domain.models.SummonerFromKtor

data class RegisterResponse(
    val summoner: SummonerFromKtor?,
    val successful: Boolean,
    val message: String
)
