package com.example.leagueapp1.core.domain.models.update

data class UpdateEvent(
    val lpGained: Int,
    val outcome: Boolean,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val position: String,
    val time: Long
)
