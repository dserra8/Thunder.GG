package com.example.leagueapp1.database

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.NOCASE
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.leagueapp1.util.Constants


@Entity(tableName = "summoners")
data class SummonerProperties(

    @PrimaryKey val id: String,
    val accountId: String,
    val puuid: String,
    @ColumnInfo(collate = NOCASE) val name: String,
    val profileIconId: Double,
    val revisionDate: Double,
    val summonerLevel: Double,
    var current: Boolean = false,
    val timeReceived: Long,
    val initBoostCalculated: Boolean = false,
    val rank: String? = null,
    @Embedded val status: Status? = null
)


@Entity(tableName = "summonerChampions", primaryKeys = ["championId", "summonerId"])
data class ChampionMastery(
    val championId: Int,
    val championLevel: Double,
    val championPoints: Double,
    val lastPlayTime: Double,
    val championPointsSinceLastLevel: Double,
    val championPointsUntilNextLevel: Double,
    val chestGranted: Boolean,
    val tokensEarned: Double,
    val summonerId: String,
    var champName: String,
    val timeReceived: Long,
    @Embedded val rankInfo: ChampRankInfo? = null,
    @Embedded var roles: TrueRoles? = null
//    @Ignore val status: Status
)

data class ChampRankInfo(
    val recentBoost: Int = 0,
    val experienceBoost: Int? = null,
    val lp: Int = 0,
    val rank: String = "NONE"
)
data class Status(
    val message: String,
    val status_code: String
)

@Entity(tableName = "trueChampionRoles")
data class ChampionRoleRates(
    @PrimaryKey val id: Int,
    @Embedded val roles: TrueRoles
)

data class TrueRoles(
    val TOP: Boolean = false,
    val JUNGLE: Boolean = false,
    val MIDDLE: Boolean = false,
    val BOTTOM: Boolean = false,
    val UTILITY: Boolean = false,
    val ALL: Boolean = true
)

