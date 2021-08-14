package com.example.leagueapp1.repository


import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import androidx.work.ListenableWorker
import com.example.leagueapp1.BuildConfig
import com.example.leagueapp1.champListRecyclerView.HeaderItem
import com.example.leagueapp1.database.*
import com.example.leagueapp1.network.*
import com.example.leagueapp1.repository.LeagueRepository.Companion.FRESH_TIMEOUT
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.Constants.champMap
import com.example.leagueapp1.util.Resource
import com.example.leagueapp1.util.exhaustive
import com.example.leagueapp1.util.networkBoundResource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject


class Repository @Inject constructor(
    private val api: RiotApiService,
    private val db: LeagueDatabase,
) : LeagueRepository {

    private val summonersDao = db.summonersDao()
    private val championsDao = db.championsDao()
    private val championRoleRatesDao = db.championRoleRatesDao()


    /**
     * Network and Database Functions for Summoners
     */

    override val summoner: Flow<SummonerProperties?> = getSummonerFlow()


    override suspend fun getSummonerPropertiesAsync(url: String): Response<SummonerProperties> {
        return api.getSummonerPropertiesAsync(url)
    }

    override fun getAllSummoners(): Flow<List<SummonerProperties>> =
        summonersDao.getAllSummoners()

    override suspend fun insertSummoner(summoner: SummonerProperties) =
        summonersDao.insertSummoner(summoner)

    override fun getSummonerFlow(): Flow<SummonerProperties?> =
        summonersDao.getSummonerFlow()

    override suspend fun getCurrentSummoner(): SummonerProperties? =
        summonersDao.getSummoner()

    override suspend fun updateSummoner(summoner: SummonerProperties) =
        summonersDao.update(summoner)

    override suspend fun deleteCurrentSummonerAndChampions() {
        val summoner = getCurrentSummoner()
        if (summoner != null) {
            championsDao.deleteSummonerChampions(summoner.id)
        }
        summonersDao.deleteCurrentSummoner()
    }

    override suspend fun getSummonerByName(summonerName: String): SummonerProperties? =
        summonersDao.getSummonerByName(summonerName)

    override suspend fun checkAndReturnSummoner(summonerName: String): Resource<SummonerProperties> {
        val result = refreshSummoner(summonerName)
        val daoResponse = getSummonerByName(summonerName)
        if (daoResponse != null)
            updateSummonerList(daoResponse.id)
        if (result != null) {
            return Resource.Error(result, daoResponse)
        }
        return Resource.Success(daoResponse!!)
    }

    override suspend fun refreshSummoner(summonerName: String): Exception? {
        val isSummonerFresh =
            summonersDao.isFreshSummoner(summonerName, System.currentTimeMillis() - FRESH_TIMEOUT)
        if (isSummonerFresh == 0) {
            //Refresh Data
            val response = safeApiCall {
                getSummonerPropertiesAsync("${Constants.SUMMONER_INFO}$summonerName?api_key=${BuildConfig.API_KEY}")
            }
            response.onSuccess { summoner ->
                val rankListResponse = getSummonerSoloRank(summoner.id)
                var rank: String? = null
                when (rankListResponse) {
                    is Resource.Error ->{
                        return Exception(rankListResponse.error)
                    }
                    is Resource.Loading -> {
                        return Exception("Error while loading Summoner rank list")
                    }
                    is Resource.Success -> {
                        val rankList = rankListResponse.data!!
                        if (rankList.isNotEmpty()) {
                            for (obj in rankList) {
                                when (obj.queueType) {
                                    "RANKED_SOLO_5x5" -> {
                                        rank = obj.tier
                                        break
                                    }
                                }
                            }
                        }
                        val updatedSummoner =
                            summoner.copy(timeReceived = System.currentTimeMillis(), rank = rank)
                        insertSummoner(updatedSummoner)
                        return null
                    }
                }
            }
            response.onFailure {
                return Exception("Summoner Not Found")
            }
        }
        return null
    }

    override suspend fun updateSummonerList(summonerID: String) {
        val summonerList = getAllSummoners().first()

        for (summoner in summonerList) {
            summoner.current = summoner.id == summonerID
            updateSummoner(summoner)
        }

    }

    override suspend fun getSummonerSoloRank(summonerId: String): Resource<List<RankDetails>?> {
        val response = safeApiCall{
            api.getSummonerRankAsync("${Constants.SUMMONER_RANK_URL}$summonerId?api_key=${BuildConfig.API_KEY}")
        }
        var result: Resource<List<RankDetails>?> = Resource.Loading()
        response.onSuccess {
            result = Resource.Success(it)
        }
        response.onFailure {
            result = Resource.Error(it)
        }
        return result
    }

    /**
     * Network and Database Functions for Champion Roles
     */

    override val roleList: LiveData<List<ChampionRoleRates>> = getTrueRoleList()

    override suspend fun refreshChampionRates(): String {
        val result = getChampionRatesAsync()
        return when (result) {
            is Resource.Error -> {
                result.error.toString()
            }
            is Resource.Loading -> {
                "Unexpected Error"
            }
            is Resource.Success -> {
                val data = result.data
                return if(data != null) {
                    val trueChampionRolesList = calculateRole(result.data)
                    insertTrueRoleList(trueChampionRolesList)
                    "Role List Success"
                } else "Error"
            }
        }.exhaustive
    }

    override suspend fun getChampionRatesAsync(): Resource<ChampionRoles> {
        val response = safeApiCall { api.getChampionRatesAsync(Constants.CHAMPION_RATES_URL) }
        var result : Resource<ChampionRoles> = Resource.Loading(null)
        response.onSuccess {
           result = Resource.Success(it)
        }
        response.onFailure {
            result = Resource.Error(it, null)
        }
        return result
    }

    override suspend fun insertTrueRoleList(list: List<ChampionRoleRates>) {
        championRoleRatesDao.insertList(list)
    }

    override fun getTrueRoleList(): LiveData<List<ChampionRoleRates>> =
        championRoleRatesDao.getList()

    override suspend fun getChampRole(id: Int): ChampionRoleRates? =
        championRoleRatesDao.getChampRole(id)

    //Determine what role a champion belongs too based on play rate on that lane
    override suspend fun calculateRole(champRates: ChampionRoles): List<ChampionRoleRates> {
        val champRoles = champRates.data
        val list = mutableListOf<ChampionRoleRates>()


        for (i in champMap) {

            var bot = false
            var top = false
            var sup = false
            var jung = false
            var mid = false
            val champ = champRoles.get(i.key!!)
            var highestPlayRate: Double? = champ?.BOTTOM?.playRate

            if (champ != null) {
                for (e in 0..3) {
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

    override suspend fun getChampion(champId: Int, summonerId: String): ChampionMastery? {
        return championsDao.getChampion(champId, summonerId)
    }

    override suspend fun updateChampionRecentBoost(summonerId: String, champId: Int, boost: Int) {
        championsDao.updateChampionRecentBoost(summonerId, champId, boost)
    }

    override suspend fun updateChampionExperienceBoost(
        summonerId: String,
        champId: Int,
        boost: Int
    ) {
        championsDao.updateChampionExperienceBoost(summonerId, champId, boost)
    }

    override suspend fun updateChampionRank(
        summonerId: String,
        champId: Int,
        lp: Int,
        rank: Constants.Ranks
    ) {
        championsDao.updateChampionRank(summonerId, champId, lp, rank.toString())
    }

    override suspend fun getHighestMasteryChampion(): ChampionMastery? {
        val summoner = getCurrentSummoner()
        return championsDao.getHighestMasteryChampion(summoner?.id ?: "0")
    }


    override fun getHeaderInfo(
        name: String,
        profileIconId: Double,
        champion: LeagueRepository.ChampListState
    ): Flow<HeaderItem> {
        return flow {
            val splashName = when (champion) {
                is LeagueRepository.ChampListState.Ready -> {
                    champion.splashName
                }
                is LeagueRepository.ChampListState.Empty -> {
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


    override suspend fun getAllChampionMasteries(url: String): List<ChampionMastery>? {
        val response = api.getAllChampionMasteries(url)
        return if(response.isSuccessful) {
            val list = response.body()!!
            for (champion in list) {
                champion.champName = champMap[champion.championId] ?: "Unknown"
            }
            list
        } else null
    }

    override fun getChampions(
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
                    summoner?.id ?: "0",
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
                it.isEmpty() || it == null || (summoner?.let { it1 ->
                    championsDao.isFreshSummonerChampions(
                        it1.id,
                        System.currentTimeMillis() - FRESH_TIMEOUT
                    )
                } == 0)
            }
        },
        fetch = {
            db.withTransaction {
                val summoner = getCurrentSummoner()
                val url = Constants.ALL_CHAMPION_MASTERIES + (summoner?.id
                        ) + "?api_key=" + BuildConfig.API_KEY
                val championList = getAllChampionMasteries(url)
                val mutableChampionList = mutableListOf<ChampionMastery>()
                if (championList != null) {
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

    /**
     * Network and database functions relating to League Matches
     */

    override suspend fun matchListForInitBoost(): Resource<List<String>?> {
        val summoner = getCurrentSummoner()
        if (summoner != null) {
            if (!summoner.initBoostCalculated) {
                val url =
                    Constants.MATCH_LIST + summoner.puuid + "/ids?start=0&count=15&api_key=" + BuildConfig.API_KEY

                val response = api.getMatchListAsync(url)
                return if(response.isSuccessful) {
                    Resource.Success(response.body())
                } else {
                    Resource.Error(Exception(response.errorBody().toString()), null)
                }
            }
        }
        return Resource.Success(null)
    }

    override suspend fun getMatchDetails(matchId: String): Resource<MatchDetails> {
        val url = Constants.MATCH_DETAIL_URL + matchId + "?api_key=" + BuildConfig.API_KEY
        val response = api.getMatchDetailsAsync(url)
        return if(response.isSuccessful) {
            Resource.Success(response.body()!!)
        } else{
            Resource.Error(Exception(response.errorBody().toString()), null)
        }
    }
}