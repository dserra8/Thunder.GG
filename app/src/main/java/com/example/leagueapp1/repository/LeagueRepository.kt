package com.example.leagueapp1.repository

import androidx.lifecycle.LiveData
import com.example.leagueapp1.adapters.HeaderItem
import com.example.leagueapp1.data.local.ChampionMastery
import com.example.leagueapp1.data.local.ChampionRoleRates
import com.example.leagueapp1.data.local.SummonerProperties
import com.example.leagueapp1.data.remote.ChampionRoles
import com.example.leagueapp1.data.remote.requests.SummonerFromKtor
import com.example.leagueapp1.data.local.SortOrder
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.Resource
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

interface LeagueRepository {

    suspend fun login(email: String, password: String): Resource<String>

    suspend fun register(email: String, password: String, summonerName: String): Resource<String>

    suspend fun syncSummonerAndChamps()
    /**
     * Network and Database Functions for SummonerProperties
     */

    var currentSummoner: SummonerProperties?

    suspend fun transformSummonerObject(summoner: SummonerProperties): SummonerFromKtor

    fun getAllSummoners(): Flow<List<SummonerProperties>>

    suspend fun insertSummoner(summoner: SummonerProperties)

    suspend fun changeMainSummoner(puuid: String)

    suspend fun retrieveSaveSummoner()

    /**
     * Network and Database Functions for Champion Roles
     */

    val roleList: LiveData<List<ChampionRoleRates>>

    suspend fun refreshChampionRates(): String

    suspend fun getChampionRatesAsync(): Resource<ChampionRoles>

    suspend fun insertTrueRoleList(list: List<ChampionRoleRates>)

    fun getTrueRoleList(): LiveData<List<ChampionRoleRates>>

    suspend fun getChampRole(id: Int): ChampionRoleRates?

    suspend fun calculateRole(champRates: ChampionRoles): List<ChampionRoleRates>

    companion object {
        val FRESH_TIMEOUT = TimeUnit.DAYS.toMillis(1)
    }

    /**
     * Network and Database Functions for Champion Mastery
     */

    sealed class ChampListState {
        data class Ready(val splashName: String) : ChampListState()
        object Empty : ChampListState()
    }

    suspend fun insertChampions(champs: List<ChampionMastery>)

    suspend fun getChampion(champId: Int, summonerId: String): ChampionMastery?

    suspend fun updateChampionRank(summonerId: String, champId: Int, lp: Int, rank: Constants.Ranks)

    suspend fun getHighestMasteryChampion(): ChampionMastery?

    fun getHeaderInfo(
        name: String,
        profileIconId: Int,
        champion: ChampListState
    ): Flow<HeaderItem>

    fun getChampions(
        searchQuery: String,
        sortOrder: SortOrder,
        showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<Resource<List<ChampionMastery>>>

}
