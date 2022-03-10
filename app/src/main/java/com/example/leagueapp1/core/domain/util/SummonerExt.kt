package com.example.leagueapp1.core.domain.util

import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.domain.models.Summoner

fun Summoner.transformToKtorSummoner(): SummonerFromKtor = SummonerFromKtor(
    id = id,
    accountId = accountId,
    puuid = puuid,
    name = name,
    profileIconId = profileIconId,
    revisionDate = revisionDate,
    summonerLevel = summonerLevel,
    rank = rank
)