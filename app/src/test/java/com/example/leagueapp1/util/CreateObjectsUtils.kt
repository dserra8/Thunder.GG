package com.example.leagueapp1.util

import com.example.leagueapp1.core.domain.models.Rank
import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.feature_champions.domain.models.Champion

fun createKtorSummoner(
    id: String = "0",
    name: String = "",
    profileIconId: Int = 0,
    revisionDate: Long = 0.0.toLong(),
    summonerLevel: Long = 0.0.toLong(),
    rank: Rank? = null,
    championList: List<Champion>? = null
) = SummonerFromKtor(
    id = id,
    accountId = id,
    puuid = id,
    name = name,
    profileIconId = profileIconId,
    revisionDate = revisionDate,
    summonerLevel = summonerLevel,
    rank = rank,
    championList = championList
)
