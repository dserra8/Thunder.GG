package com.example.leagueapp1.feature_champions.domain.models

import androidx.room.Embedded
import androidx.room.Entity
import com.example.leagueapp1.core.domain.models.update.UpdateEvent
import com.google.gson.annotations.Expose

@Entity(tableName = "summonerChampions", primaryKeys = ["championId", "summonerId"])
data class Champion(
    val championId: Int,
    val championLevel: Double,
    val championPoints: Double,
    val lastPlayTime: Double,
    val summonerId: String,
    var champName: String = "Unknown",
    val updateEvents: MutableList<UpdateEvent> = mutableListOf(),
    @Embedded var rankInfo: ChampRankInfo? = null,
    @Embedded var roles: TrueRoles? = null
)
