package com.example.leagueapp1.feature_champions.domain.repository

import com.example.leagueapp1.core.domain.models.update.UpdateChampsRanks
import com.example.leagueapp1.core.domain.models.update.UpdateEvent
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.data.local.FilterPreferences
import com.example.leagueapp1.feature_champions.domain.models.Champion
import kotlinx.coroutines.flow.Flow

interface ChampionsRepositoryInterface {

    fun getChampions(
        filterPreferences: FilterPreferences,
        query: String,
        id: String
    ): Flow<List<Champion>>

    suspend fun getHighestMasteryChampion(id: String): Champion?

    suspend fun insertChampions(champs: HashMap<Int,Champion>)

    suspend fun getChampion(champId: Int, summonerId: String): Champion?

    suspend fun getAllChamps(id: String): List<Champion>

    suspend fun getChampName(champId: Int): String

    suspend fun updateChampsRanks(): NoLoadResource<UpdateChampsRanks>?

    suspend fun updateUpdateEventList(list: MutableList<UpdateEvent>, summonerId: String, champId: Int)

    suspend fun updateChampion(champ: Champion)
}