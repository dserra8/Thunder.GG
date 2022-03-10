package com.example.leagueapp1.feature_champions.domain.models

data class TrueRoles(
    val top: Boolean = false,
    val jungle: Boolean = false,
    val middle: Boolean = false,
    val bottom: Boolean = false,
    val utility: Boolean = false,
    val all: Boolean = true
)
