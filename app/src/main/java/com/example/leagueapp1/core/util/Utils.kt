package com.example.leagueapp1.core.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leagueapp1.core.domain.models.Rank
import com.example.leagueapp1.core.domain.models.Summoner
import com.example.leagueapp1.feature_champions.domain.models.ChampRankInfo
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.models.TrueRoles

val <T> T.exhaustive: T
    get() = this

fun RecyclerView.getCurrentPosition(): Int {
    return (this.layoutManager as? LinearLayoutManager?)?.findFirstVisibleItemPosition() ?: 0
}


fun createChampion(
    championId: Int = 99,
    championLevel: Double = 1.0,
    championPoints: Double = 200.0,
    lastPlayTime: Double = 1000.0,
    summonerId: String = "0",
    champName: String = "",
    rankInfo: ChampRankInfo? = null,
    roles: TrueRoles = TrueRoles()
) = Champion(
    championId = championId,
    championLevel = championLevel,
    championPoints = championPoints,
    lastPlayTime = lastPlayTime,
    summonerId = summonerId,
    champName = champName,
    rankInfo = rankInfo,
    roles = roles
)

fun createSummoner(
    id: String = "0",
    name: String = "",
    profileIconId: Int = 0,
    revisionDate: Long = 0.0.toLong(),
    summonerLevel: Long = 0.0.toLong(),
    timeReceived: Long = 1000,
    rank: Rank? = null,
    isMainSummoner: Boolean = false
) = Summoner(
    id = id,
    accountId = id,
    puuid = id,
    name = name,
    profileIconId = profileIconId,
    revisionDate = revisionDate,
    summonerLevel = summonerLevel,
    timeReceived = timeReceived,
    rank = rank,
    isMainSummoner = isMainSummoner
)

