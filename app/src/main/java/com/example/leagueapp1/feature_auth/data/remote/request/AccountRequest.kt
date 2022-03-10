package com.example.leagueapp1.feature_auth.data.remote.request

data class AccountRequest(
    val email: String,
    val password: String,
    val summonerName: String
)

