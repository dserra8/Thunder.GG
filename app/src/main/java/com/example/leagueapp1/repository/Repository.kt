package com.example.leagueapp1.repository


import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.leagueapp1.champListRecyclerView.HeaderItem
import com.example.leagueapp1.database.*
import com.example.leagueapp1.network.ChampionRoles
import com.example.leagueapp1.network.MatchDetails
import com.example.leagueapp1.network.RankDetails
import com.example.leagueapp1.network.RiotApiService
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.Constants.Companion.champMap
import com.example.leagueapp1.util.Resource
import com.example.leagueapp1.util.networkBoundResource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class Repository @Inject constructor(
    private val api: RiotApiService,
    private val db: LeagueDatabase
) {

    private val summonersDao = db.summonersDao()
    private val championsDao = db.championsDao()
    private val championRoleRatesDao = db.championRoleRatesDao()


    /**
     * Network and Database Functions for Summoners
     */

    val summoner: Flow<SummonerProperties?> = getSummonerFlow()


    private fun getSummonerPropertiesAsync(url: String): Deferred<SummonerProperties> {
        return api.getSummonerPropertiesAsync(url)
    }

    fun getAllSummoners(): Flow<List<SummonerProperties>> =
        summonersDao.getAllSummoners()

    private suspend fun insertSummoner(summoner: SummonerProperties) =
        summonersDao.insertSummoner(summoner)

    private fun getSummonerFlow(): Flow<SummonerProperties?> =
        summonersDao.getSummonerFlow(true)

    suspend fun getCurrentSummoner(): SummonerProperties =
        summonersDao.getSummoner(true)

    suspend fun updateSummoner(summoner: SummonerProperties) =
        summonersDao.update(summoner)

    suspend fun deleteSummoner(summoner: SummonerProperties) {
        summonersDao.deleteSummoner(summoner)
    }

    private suspend fun getSummonerByName(summonerName: String): SummonerProperties? =
        summonersDao.getSummonerByName(summonerName)

    suspend fun checkAndReturnSummoner(summonerName: String): Resource<SummonerProperties> {
        val result = refreshSummoner(summonerName)
        val daoResponse = getSummonerByName(summonerName)
        if (daoResponse != null)
            updateSummonerList(daoResponse.id)
        if (result != null) {
            return Resource.Error(result, daoResponse)
        }
        return Resource.Success(daoResponse!!)
    }

    private suspend fun refreshSummoner(summonerName: String): Exception? {
        val isSummonerFresh =
            summonersDao.isFreshSummoner(summonerName, System.currentTimeMillis() - FRESH_TIMEOUT)
        if (isSummonerFresh == 0) {
            //Refresh Data
            val response =
                getSummonerPropertiesAsync("${Constants.SUMMONER_INFO}$summonerName?api_key=${Constants.API_KEY}")
            return try {
                val summoner = response.await()
                val rankList = getSummonerSoloRank(summoner.id)
                var rank: String? = null
                if (rankList != null && rankList.isNotEmpty()) {
                    for (obj in rankList) {
                        when (obj.queueType) {
                            "RANKED_SOLO_5x5" -> {
                                rank = obj.tier
                                break
                            }
                            else ->{ break }
                        }
                    }
                }
                val updatedSummoner = summoner.copy(timeReceived = System.currentTimeMillis(), rank = rank)
                insertSummoner(updatedSummoner)
                null
            } catch (e: Exception) {
                e
            }
        }
        return null
    }

    private suspend fun updateSummonerList(summonerID: String) {
        val summonerList = getAllSummoners().first()

        for (summoner in summonerList) {
            summoner.current = summoner.id == summonerID
            updateSummoner(summoner)
        }

    }

    private suspend fun getSummonerSoloRank(summonerId: String): List<RankDetails>? {
        val response =
            api.getSummonerRankAsync("${Constants.SUMMONER_RANK_URL}$summonerId?api_key=${Constants.API_KEY}")
        return try {
            response.await()
        } catch (e: Exception) {
             null
        }
    }

    /**
     * Network and Database Functions for Champion Roles
     */

    val roleList: LiveData<List<ChampionRoleRates>> = getTrueRoleList()

    suspend fun refreshChampionRates() {
        withContext(Dispatchers.IO) {
            val championRatesList = getChampionRatesAsync().await()
            val trueChampionRolesList = calculateRole(championRatesList)
            insertTrueRoleList(trueChampionRolesList)
        }
    }

    private fun getChampionRatesAsync(): Deferred<ChampionRoles> {
        return api.getChampionRatesAsync(Constants.CHAMPION_RATES_URL)
    }

    private suspend fun insertTrueRoleList(list: List<ChampionRoleRates>) {
        championRoleRatesDao.insertList(list)
    }

    private fun getTrueRoleList(): LiveData<List<ChampionRoleRates>> =
        championRoleRatesDao.getList()

    private suspend fun getChampRole(id: Int): ChampionRoleRates? =
        championRoleRatesDao.getChampRole(id)


    //Determine what role a champion belongs too based on play rate on that lane
    private fun calculateRole(champRates: ChampionRoles): List<ChampionRoleRates> {
        val champRoles = champRates.data
        val list = mutableListOf<ChampionRoleRates>()


        for (i in Constants.champMap) {

            var bot = false
            var top = false
            var sup = false
            var jung = false
            var mid = false
            val champ = champRoles.get(i.key!!)
            var highestPlayRate: Double? = champ?.BOTTOM?.playRate

            if (champ != null) {
                for (i in 0..3) {
                    if (highestPlayRate != null) {
                        highestPlayRate = when {
                            highestPlayRate < champ.JUNGLE.playRate -> champ.JUNGLE.playRate
                            highestPlayRate < champ.TOP.playRate -> champ.TOP.playRate
                            highestPlayRate < champ.UTILITY.playRate -> champ.UTILITY.playRate
                            highestPlayRate < champ.MIDDLE.playRate -> champ.MIDDLE.playRate
                            else -> break
                        }
                    }
                }

                if (highestPlayRate == champ.BOTTOM.playRate || champ.BOTTOM.playRate >= 1.0)
                    bot = true
                if (highestPlayRate == champ.TOP.playRate || champ.TOP.playRate >= 1.0)
                    top = true
                if (highestPlayRate == champ.UTILITY.playRate || champ.UTILITY.playRate >= 1.0)
                    sup = true
                if (highestPlayRate == champ.MIDDLE.playRate || champ.MIDDLE.playRate >= 1.0)
                    mid = true
                if (highestPlayRate == champ.JUNGLE.playRate || champ.JUNGLE.playRate >= 1.0)
                    jung = true

            }
            list.add(ChampionRoleRates(i.key!!, TrueRoles(top, jung, mid, bot, sup)))
        }
        return list
    }


    /**
     * Network and Database Functions for Champion Mastery
     */

    suspend fun getChampion(champId: Int, summonerId: String): ChampionMastery {
        return championsDao.getChampion(champId, summonerId)
    }

    fun getChampionFlow(champId: Int, summonerId: String): Flow<ChampionMastery> {
        return championsDao.getChampionFlow(champId, summonerId)
    }

    suspend fun updateChampionRecentBoost(summonerId: String, champId: Int, boost: Int) {
        championsDao.updateChampionRecentBoost(summonerId, champId, boost)
    }

    suspend fun updateChampionExperienceBoost(summonerId: String, champId: Int, boost: Int) {
        championsDao.updateChampionExperienceBoost(summonerId, champId, boost)
    }

    suspend fun updateChampionRank(summonerId: String, champId: Int, lp: Int, rank: Constants.Companion.Ranks) {
        championsDao.updateChampionRank(summonerId, champId, lp, rank.toString())
    }

    suspend fun updateChampion(champion: ChampionMastery) {
        championsDao.updateChampion(champion)
    }

    sealed class ChampListState {
        data class Ready(val splashName: String) : ChampListState()
        object Empty : ChampListState()
    }


    suspend fun getHighestMasteryChampion(): ChampionMastery? {
        val summoner = getCurrentSummoner()
        return championsDao.getHighestMasteryChampion(summoner.id)
    }


    fun getHeaderInfo(
        name: String,
        profileIconId: Double,
        champion: Repository.ChampListState
    ): Flow<HeaderItem> {
        return flow {

            val splashName = when (champion) {
                is ChampListState.Ready -> {
                    champion.splashName
                }
                is ChampListState.Empty -> {
                    "Lux"
                }
            }
            emit(
                HeaderItem(
                    name = name,
                    summonerIconId = profileIconId.toInt(),
                    splashName = splashName
                )
            )
        }
    }


    private suspend fun getAllChampionMasteries(url: String): List<ChampionMastery> {
        val list = api.getAllChampionMasteries(url)
        for (champion in list) {
            champion.champName = champMap[champion.championId.toInt()] ?: "Unknown"
        }
        return list
    }

    fun getChampions(
        searchQuery: String,
        sortOrder: SortOrder,
        showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ) = networkBoundResource(

        query = {
            db.withTransaction {
                val summoner = getCurrentSummoner()
                championsDao.getChampions(
                    searchQuery,
                    sortOrder,
                    summoner.id,
                    showADC,
                    showSup,
                    showMid,
                    showJungle,
                    showTop,
                    showAll
                )
            }
        },
        shouldFetch = {
            db.withTransaction {
                val summoner = getCurrentSummoner()
                it.isEmpty() || it == null || (championsDao.isFreshSummonerChampions(
                    summoner.id,
                    System.currentTimeMillis() - FRESH_TIMEOUT
                ) == 0)
            }
        },
        fetch = {
            db.withTransaction {
                val summoner = getCurrentSummoner()
                val url = Constants.ALL_CHAMPION_MASTERIES + (summoner.id
                    ?: "") + "?api_key=" + Constants.API_KEY
                val championList = getAllChampionMasteries(url)
                val mutableChampionList = mutableListOf<ChampionMastery>()
                for (champion in championList) {
                    val champRole = getChampRole(champion.championId) ?: ChampionRoleRates(
                        0,
                        TrueRoles(
                            TOP = false,
                            JUNGLE = false,
                            MIDDLE = false,
                            BOTTOM = false,
                            UTILITY = false,
                            ALL = true
                        )
                    )
                    mutableChampionList.add(
                        champion.copy(
                            roles = champRole.roles,
                            timeReceived = System.currentTimeMillis(),
                            rankInfo = ChampRankInfo()
                        )
                    )
                }
                mutableChampionList
            }
        },
        saveFetchResult = { championList ->
            db.withTransaction {
                championsDao.insertChampionList(championList)
            }
        }

    )

    companion object {
        val FRESH_TIMEOUT = TimeUnit.DAYS.toMillis(1)
    }


    /**
     * Network and database functions relating to League Matches
     */

    suspend fun matchListForInitBoost(): Resource<List<String>?> {
        val summoner = getCurrentSummoner()
        if(!summoner.initBoostCalculated) {
            val url =
                Constants.MATCH_LIST + summoner.puuid + "/ids?start=0&count=15&api_key=" + Constants.API_KEY

            val response = api.getMatchListAsync(url)
            return try {
                Resource.Success(response.await())
            } catch (e: Exception) {
                Resource.Error(e,null)
            }
        }
        return Resource.Success(null)
    }



    suspend fun getMatchDetails(matchId: String): Resource<MatchDetails> {
        val url = Constants.MATCH_DETAIL_URL + matchId + "?api_key=" + Constants.API_KEY
        val response = api.getMatchDetailsAsync(url)
        return try {
            Resource.Success(response.await())
        } catch (e: java.lang.Exception) {
            Resource.Error(e, null)
        }
    }
}