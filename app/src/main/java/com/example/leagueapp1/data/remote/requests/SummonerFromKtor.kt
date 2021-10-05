package com.example.leagueapp1.data.remote.requests

import com.example.leagueapp1.data.local.ChampionMastery
import com.example.leagueapp1.data.local.Rank

data class SummonerFromKtor(
    val id: String,
    val accountId: String,
    val puuid: String,
    val name: String,
    val profileIconId: Int,
    val revisionDate: Long,
    val summonerLevel: Long,
    val rank: Rank? = null,
    val championList: List<ChampionMastery>? = null
)