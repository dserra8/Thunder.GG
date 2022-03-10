package com.example.leagueapp1.core.domain.models

import com.example.leagueapp1.feature_champions.domain.models.Champion

data class SummonerFromKtor(
    val id: String,
    val accountId: String,
    val puuid: String,
    val name: String,
    val profileIconId: Int,
    val revisionDate: Long,
    val summonerLevel: Long,
    val rank: Rank? = null,
    var championList: HashMap<Int, Champion> = hashMapOf()
)