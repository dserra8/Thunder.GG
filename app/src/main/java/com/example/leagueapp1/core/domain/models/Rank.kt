package com.example.leagueapp1.core.domain.models

data class Rank(
    val tier: String,
    val rank: String,
    val wins: Int,
    val losses: Int
)
