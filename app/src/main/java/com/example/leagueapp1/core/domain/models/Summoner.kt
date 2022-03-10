package com.example.leagueapp1.core.domain.models

import androidx.room.*
import androidx.room.ColumnInfo.NOCASE
import com.example.leagueapp1.core.domain.models.Rank
import com.google.gson.annotations.Expose


@Entity(tableName = "summoners")
data class Summoner(
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
        var isMainSummoner: Boolean = false,
        @Embedded var rank: Rank? = null
)





