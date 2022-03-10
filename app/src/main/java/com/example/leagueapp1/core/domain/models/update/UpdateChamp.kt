package com.example.leagueapp1.core.domain.models.update

import com.example.leagueapp1.feature_champions.domain.models.ChampRankInfo

data class UpdateChamp(
    val currentRank: ChampRankInfo,
    val updateEvents: List<UpdateEvent> = emptyList()
)
