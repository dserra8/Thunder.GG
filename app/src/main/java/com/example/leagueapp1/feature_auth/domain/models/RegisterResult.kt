package com.example.leagueapp1.feature_auth.domain.models

import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.util.NoLoadResource


data class RegisterResult(
    val usernameError: String? = null,
    val passwordError: String? = null,
    val summonerError: String? = null,
    val result: NoLoadResource<SummonerFromKtor>? = null
)
