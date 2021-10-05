package com.example.leagueapp1.data.local

import androidx.room.*
import androidx.room.ColumnInfo.NOCASE
import com.google.gson.annotations.Expose


@Entity(tableName = "summoners")
data class SummonerProperties(
        @PrimaryKey val id: String,
        val accountId: String,
        val puuid: String,
        @ColumnInfo(collate = NOCASE) val name: String,
        val profileIconId: Int,
        val revisionDate: Long,
        val summonerLevel: Long,
        @Expose(deserialize = false, serialize = false)
        var timeReceived: Long? = null,
        @Expose(deserialize = false, serialize = false)
        var initBoostCalculated: Boolean = false,
        @Expose(deserialize = false, serialize = false)
        var isMainSummoner: Boolean = false,
        @Embedded var rank: Rank? = null
)

data class Rank(
    val tier: String,
    val rank: String,
    val wins: Int,
    val losses: Int
)

@Entity(tableName = "summonerChampions", primaryKeys = ["championId", "summonerId"])
data class ChampionMastery(
        val championId: Int,
        val championLevel: Double,
        val championPoints: Double,
        val lastPlayTime: Double,
        val summonerId: String,
        var champName: String? = null,
        var timeReceived: Long? = null,
        @Embedded var rankInfo: ChampRankInfo? = null,
        @Embedded var roles: TrueRoles? = null
)

data class ChampRankInfo(
    val lp: Int? = null,
    val rank: String = "NONE"
)

data class Status(
    val message: String,
    val status_code: String
)

@Entity(tableName = "trueChampionRoles")
data class ChampionRoleRates(
    @PrimaryKey val id: Int,
    @Embedded val roles: TrueRoles = TrueRoles()
)

data class TrueRoles(
    val TOP: Boolean = false,
    val JUNGLE: Boolean = false,
    val MIDDLE: Boolean = false,
    val BOTTOM: Boolean = false,
    val UTILITY: Boolean = false,
    val ALL: Boolean = true
)

