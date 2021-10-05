package com.example.leagueapp1.util

import com.example.leagueapp1.data.local.*
import com.example.leagueapp1.data.remote.ChampionRates
import com.example.leagueapp1.data.remote.ParticipantData
import com.example.leagueapp1.data.remote.Rate
import com.example.leagueapp1.database.*

fun createParticipantData(
    assists : Int = 1,
    baronKills : Int = 3,
    deaths : Int= 9,
    dragonKills : Int = 2,
    kills : Int = 4,
    neutralMinionsKilled : Int = 60,
    objectivesStolen : Int = 0,
    totalDamageDealtToChampions : Double = 30000.0,
    totalMinionsKilled : Int = 100,
    visionScore : Int = 20,
    championId : Int = 99,
    championName : String = "Lux",
    puuid : String = "0",
    win : Boolean = true
) = ParticipantData(
    assists = assists,
    baronKills = baronKills,
    deaths = deaths,
    dragonKills = dragonKills,
    kills = kills,
    neutralMinionsKilled = neutralMinionsKilled,
    objectivesStolen = objectivesStolen,
    totalDamageDealtToChampions = totalDamageDealtToChampions,
    totalMinionsKilled =totalMinionsKilled,
    visionScore = visionScore,
    championId = championId,
    championName = championName,
    puuid = puuid,
    win = win
)

fun createChampionRates(
    UTILITY: Rate = Rate(1.2),
    JUNGLE: Rate = Rate(0.1),
    BOTTOM: Rate = Rate(0.2),
    MIDDLE: Rate = Rate(0.7),
    TOP: Rate = Rate(0.2)
) = ChampionRates(
    UTILITY = UTILITY,
    JUNGLE = JUNGLE,
    BOTTOM = BOTTOM,
    MIDDLE = MIDDLE,
    TOP = TOP
)

fun createChampionMastery(
    championId: Int = 99,
    championLevel: Double = 1.0,
    championPoints: Double = 200.0,
    lastPlayTime : Double = 1000.0,
//    championPointsSinceLastLevel: Double = 10.0,
//    championPointsUntilNextLevel: Double = 100.0,
//    chestGranted : Boolean = false,
//    tokensEarned : Double = 10.0,
    summonerId : String = "0",
    champName : String = "Lux",
    timeReceived : Long = 10000,
    rankInfo : ChampRankInfo? = null,
    roles : TrueRoles = TrueRoles(
        TOP = false,
        JUNGLE = false,
        MIDDLE = true,
        BOTTOM = false,
        UTILITY = true
    )
) = ChampionMastery(
    championId = championId,
    championLevel = championLevel,
    championPoints = championPoints,
    lastPlayTime = lastPlayTime,
//    championPointsSinceLastLevel = championPointsSinceLastLevel,
//    championPointsUntilNextLevel = championPointsUntilNextLevel,
//    chestGranted = chestGranted,
//    tokensEarned = tokensEarned,
    summonerId = summonerId,
    champName = champName,
    timeReceived = timeReceived,
    rankInfo = rankInfo,
    roles = roles
)

fun createSummonerProperties(
    id : String = "0",
    accountId : String = "0",
    puuid : String = "0",
    name : String = "",
    profileIconId : Int = 0,
    revisionDate : Long = 0.0.toLong(),
    summonerLevel : Long = 0.0.toLong(),
 //   current : Boolean = false,
    timeReceived : Long = 0,
    initBoostCalculated : Boolean = false,
    rank : Rank? = null,
  //  status : Status? = null

) = SummonerProperties(
    id = id,
    accountId = accountId,
    puuid = puuid,
    name = name,
    profileIconId = profileIconId,
    revisionDate = revisionDate,
    summonerLevel = summonerLevel,
 //   current = current,
    timeReceived = timeReceived,
    initBoostCalculated = initBoostCalculated,
    rank = rank,
 //   status = status
)