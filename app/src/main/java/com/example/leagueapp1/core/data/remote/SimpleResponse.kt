package com.example.leagueapp1.core.data.remote

data class SimpleResponse(
    val successful: Boolean,
    val message: String,
    val puuid: String?
)