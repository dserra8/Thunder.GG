package com.example.leagueapp1.repository

import androidx.lifecycle.LiveData
import com.example.leagueapp1.champListRecyclerView.HeaderItem
import com.example.leagueapp1.database.*
import com.example.leagueapp1.network.ChampionRoles
import com.example.leagueapp1.network.MatchDetails
import com.example.leagueapp1.network.RankDetails
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.Resource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.util.concurrent.TimeUnit

interface LeagueRepository {

    /**
     * Network and Database Functions for SummonerProperties
     */

    val summoner: Flow<SummonerProperties?>

    suspend fun getSummonerPropertiesAsync(url: String): Response<SummonerProperties>

    fun getAllSummoners(): Flow<List<SummonerProperties>>

    suspend fun insertSummoner(summoner: SummonerProperties)

    fun getSummonerFlow(): Flow<SummonerProperties?>

    suspend fun getCurrentSummoner(): SummonerProperties?

    suspend fun updateSummoner(summoner: SummonerProperties)

    suspend fun deleteCurrentSummonerAndChampions()

    suspend fun getSummonerByName(summonerName: String): SummonerProperties?

    suspend fun checkAndReturnSummoner(summonerName: String): Resource<SummonerProperties>

    suspend fun refreshSummoner(summonerName: String): Exception?

    suspend fun updateSummonerList(summonerID: String)

    suspend fun getSummonerSoloRank(summonerId: String): Resource<List<RankDetails>?>

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

    suspend fun getChampion(champId: Int, summonerId: String): ChampionMastery?

    suspend fun updateChampionRecentBoost(summonerId: String, champId: Int, boost: Int)

    suspend fun updateChampionExperienceBoost(summonerId: String, champId: Int, boost: Int)

    suspend fun updateChampionRank(summonerId: String, champId: Int, lp: Int, rank: Constants.Ranks)

    suspend fun getHighestMasteryChampion(): ChampionMastery?

    fun getHeaderInfo(
        name: String,
        profileIconId: Double,
        champion: ChampListState
    ): Flow<HeaderItem>

    suspend fun getAllChampionMasteries(url: String): List<ChampionMastery>?

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

    /**
     * Network and database functions relating to League Matches
     */

    suspend fun matchListForInitBoost(): Resource<List<String>?>

    suspend fun getMatchDetails(matchId: String): Resource<MatchDetails>
}