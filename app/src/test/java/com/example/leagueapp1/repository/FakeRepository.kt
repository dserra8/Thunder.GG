package com.example.leagueapp1.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.leagueapp1.BuildConfig
import com.example.leagueapp1.champListRecyclerView.HeaderItem
import com.example.leagueapp1.database.*
import com.example.leagueapp1.network.*
import com.example.leagueapp1.repository.LeagueRepository.Companion.FRESH_TIMEOUT
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.Resource
import com.example.leagueapp1.util.exhaustive
import com.example.leagueapp1.util.networkBoundResource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class FakeRepository : LeagueRepository {
    private val championList = mutableListOf<ChampionMastery>()
    private val summonerList = mutableListOf<SummonerProperties>()
    private val championRoleList = mutableListOf<ChampionRoleRates>()

    private val observableChampionRoleList =
        MutableLiveData<List<ChampionRoleRates>>(championRoleList)

    private var id: Int = 0

    //   private val championListFlow = Flow<List<SummonerProperties>>
    private var shouldReturnNetworkError = true

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    override val summoner: Flow<SummonerProperties?> = getSummonerFlow()

    override suspend fun getSummonerPropertiesAsync(url: String): Response<SummonerProperties> {
        return if (shouldReturnNetworkError) {
            Response.error(404, "{\"key\":[\"somestuff\"]}"
                .toResponseBody("application/json".toMediaTypeOrNull())
            )
        } else {
            val name = url.substring(0, 65).substringBefore("?")
            val summoner = SummonerProperties(
                id = id.toString(),
                accountId = id.toString(),
                puuid = id.toString(),
                name = name,
                profileIconId = 0.0,
                revisionDate = 0.0,
                summonerLevel = 10.0,
                current = false,
                timeReceived = 0,
                initBoostCalculated = false,
                rank = null,
                status = null
            )
            id++
           Response.success(summoner)
        }
    }

    override fun getAllSummoners(): Flow<List<SummonerProperties>> = flow {
        emit(summonerList)
    }

    override suspend fun insertSummoner(summoner: SummonerProperties) {
        summonerList.add(summoner)
    }

    override fun getSummonerFlow(): Flow<SummonerProperties?> = flow {
        for (summoner in summonerList) {
            if (summoner.current) {
                emit(summoner)
            }
        }
    }

    override suspend fun getCurrentSummoner(): SummonerProperties? {
        for (summoner in summonerList) {
            if (summoner.current) {
                return summoner
            }
        }
        return null
    }

    override suspend fun updateSummoner(summoner: SummonerProperties) {
        summonerList.forEachIndexed { index, item ->
            if (item.id == summoner.id) {
                summonerList[index] = summoner
            }
        }
    }

    private fun deleteCurrentSummoner(summoner: SummonerProperties) {
        summonerList.remove(summoner)
    }

    private fun deleteSummonerChampions(summoner: SummonerProperties) {
        for (champ in championList) {
            if (champ.summonerId == summoner.id) {
                championList.remove(champ)
            }
        }
    }

    override suspend fun deleteCurrentSummonerAndChampions() {
        val summoner = getCurrentSummoner()

        if (summoner != null) {
            deleteCurrentSummoner(summoner)
            deleteSummonerChampions(summoner)
        }
    }

    override suspend fun getSummonerByName(summonerName: String): SummonerProperties? {
        for (summoner in summonerList) {
            if (summoner.name == summonerName) {
                return summoner
            }
        }
        return null
    }

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

    private suspend fun checkSummonerFreshness(summonerName: String): Boolean {
        val summoner = getSummonerByName(summonerName)
        if (summoner != null) {
            return summoner.timeReceived >= (System.currentTimeMillis() - FRESH_TIMEOUT)
        }
        return false
    }

    override suspend fun refreshSummoner(summonerName: String): Exception? {
        val isFreshSummoner = checkSummonerFreshness(summonerName)
        if (!isFreshSummoner) {
            val response =
                getSummonerPropertiesAsync("${Constants.SUMMONER_INFO}$summonerName?api_key=${BuildConfig.API_KEY}")
            return if (response.isSuccessful) {
                val summoner = response.body()
                if (summoner != null) {
                    val rankList = getSummonerSoloRank(summoner.id)
                    var rank: String? = null
                    if (rankList != null && rankList.isNotEmpty()) {
                        for (obj in rankList) {
                            when (obj.queueType) {
                                "RANKED_SOLO_5x5" -> {
                                    rank = obj.tier
                                    break
                                }
                                else -> {
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
                Exception("Summoner Not Found")
            }
            else{
                Exception("Summoner Not Found")
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

    override suspend fun getSummonerSoloRank(summonerId: String): List<RankDetails>? {
        val response =
            getSummonerRankAsync()
        return try {
            response.await()
        } catch (e: Exception) {
            null
        }
    }

    private fun getSummonerRankAsync(): Deferred<List<RankDetails>> {
        return if (shouldReturnNetworkError) {
            CompletableDeferred(null)
        } else {
            val rank1 = RankDetails(
                queueType = "RANKED_SOLO_5X5",
                tier = "GOLD",
                rank = "IV",
                summonerId = "0",
                summonerName = "None",
                wins = 20,
                losses = 20
            )
            val rank2 = RankDetails(
                queueType = "NORMAL",
                tier = "GOLD",
                rank = "IV",
                summonerId = "0",
                summonerName = "None",
                wins = 20,
                losses = 20
            )
            val rankList = listOf(rank1, rank2)
            CompletableDeferred(rankList)
        }
    }

    override val roleList: LiveData<List<ChampionRoleRates>> = getTrueRoleList()

    override suspend fun refreshChampionRates(): String {
        val result = getChampionRatesAsync()
        return when (result) {
            is Resource.Error -> {
                "Error"
            }
            is Resource.Loading -> {
                "Nothing"
            }
            is Resource.Success -> {
                val trueChampionRolesList = calculateRole(result.data!!)
                insertTrueRoleList(trueChampionRolesList)
                "Role List Success"
            }
        }.exhaustive
    }

    override suspend fun getChampionRatesAsync(): Resource<ChampionRoles> {
        val response = getChampionRatesMockAsync()
        return try {
            Resource.Success(response.await())
        } catch (e: Exception) {
            Resource.Error(e, null)
        }
    }

    private fun getChampionRatesMockAsync(): Deferred<ChampionRoles> {
        return if (shouldReturnNetworkError) {
            CompletableDeferred(null)
        } else {
            val data = Data(
                `1` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `10` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `101` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `102` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `103` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `104` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `105` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `106` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `107` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `11` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `110` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `111` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `112` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `113` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `114` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `115` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `117` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `119` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `12` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `120` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `121` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `122` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `126` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `127` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `13` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `131` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `133` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `134` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `136` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `14` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `141` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `142` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `143` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `145` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `147` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `15` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `150` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `154` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `157` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `16` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `161` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `163` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `164` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `17` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `18` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `19` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `2` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `20` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `201` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `202` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `203` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `21` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `22` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `222` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `223` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `23` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `234` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `235` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `236` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `238` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `24` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `240` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `245` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `246` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `25` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `254` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `26` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `266` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `267` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `268` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `27` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `28` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `29` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `3` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `30` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `31` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `32` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `33` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `34` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `35` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `350` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `36` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `360` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `37` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `38` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `39` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `4` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `40` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `41` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `412` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `42` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `420` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `421` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `427` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `429` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `43` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `432` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `44` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `45` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `48` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `497` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `498` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `5` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `50` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `51` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `516` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `517` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `518` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `523` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `526` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `53` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `54` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `55` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `555` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `56` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `57` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `58` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `59` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `6` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `60` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `61` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `62` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `63` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `64` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `67` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `68` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `69` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `7` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `72` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `74` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `75` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `76` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `77` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `777` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `78` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `79` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `8` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `80` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `81` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `82` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `83` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `84` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `85` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `86` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `875` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `876` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `887` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `89` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `9` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `90` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `91` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `92` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `96` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `98` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                ),
                `99` = ChampionRates(
                    UTILITY = Rate(1.2),
                    JUNGLE = Rate(0.1),
                    BOTTOM = Rate(0.2),
                    MIDDLE = Rate(0.7),
                    TOP = Rate(0.2)
                )
            )
            CompletableDeferred(ChampionRoles(data, patch = "12.5"))
        }
    }

    override suspend fun insertTrueRoleList(list: List<ChampionRoleRates>) {
        for (item in list) {
            championRoleList.add(item)
        }
        refreshLiveData()
    }

    private fun refreshLiveData() {
        observableChampionRoleList.postValue(championRoleList)
    }


    override fun getTrueRoleList(): LiveData<List<ChampionRoleRates>> {
        return observableChampionRoleList
    }

    override suspend fun getChampRole(id: Int): ChampionRoleRates? {
        for (champ in championRoleList) {
            if (champ.id == id) {
                return champ
            }
        }
        return null
    }

    override suspend fun calculateRole(champRates: ChampionRoles): List<ChampionRoleRates> {
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

    override suspend fun getChampion(champId: Int, summonerId: String): ChampionMastery? {
        for (champ in championList) {
            if (champ.championId == champId && champ.summonerId == summonerId) {
                return champ
            }
        }
        return null
    }

    override suspend fun updateChampionRecentBoost(summonerId: String, champId: Int, boost: Int) {
        championList.forEachIndexed { index, champ ->
            if (champ.championId == champId && champ.summonerId == summonerId) {
                val rankInfo = champ.rankInfo
                if (rankInfo != null) {
                    val newRankInfo = rankInfo.copy(recentBoost = boost)
                    championList[index] = champ.copy(rankInfo = newRankInfo)
                }
            }
        }
    }

    override suspend fun updateChampionExperienceBoost(
        summonerId: String,
        champId: Int,
        boost: Int
    ) {
        championList.forEachIndexed { index, champ ->
            if (champ.championId == champId && champ.summonerId == summonerId) {
                val rankInfo = champ.rankInfo
                if (rankInfo != null) {
                    val newRankInfo = rankInfo.copy(experienceBoost = boost)
                    championList[index] = champ.copy(rankInfo = newRankInfo)
                }
            }
        }
    }

    override suspend fun updateChampionRank(
        summonerId: String,
        champId: Int,
        lp: Int,
        rank: Constants.Ranks
    ) {
        championList.forEachIndexed { index, champ ->
            if (champ.championId == champId && champ.summonerId == summonerId) {
                val rankInfo = champ.rankInfo
                if (rankInfo != null) {
                    val newRankInfo = rankInfo.copy(rank = rank.toString(), lp = lp)
                    championList[index] = champ.copy(rankInfo = newRankInfo)
                }
            }
        }
    }

    override suspend fun getHighestMasteryChampion(): ChampionMastery? {
        return championList.maxByOrNull { it.championPoints }
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

    override suspend fun getAllChampionMasteries(url: String): List<ChampionMastery> {
        val list = getAllChampionMasteriesMockAsync()
        for (champion in list) {
            champion.champName = Constants.champMap[champion.championId] ?: "Unknown"
        }
        return list
    }

    private fun getAllChampionMasteriesMockAsync(): List<ChampionMastery> {
        val champion1 = ChampionMastery(
            championId = 1,
            championLevel = 1.0,
            championPoints = 200.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "123",
            champName = "Lux",
            timeReceived = 10000,
            rankInfo = null,
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        val champion2 = ChampionMastery(
            championId = 2,
            championLevel = 1.0,
            championPoints = 300.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "123",
            champName = "Abs",
            timeReceived = 10000,
            rankInfo = null,
            roles = null
        )
        val champion3 = ChampionMastery(
            championId = 3,
            championLevel = 1.0,
            championPoints = 400.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "123",
            champName = "Darius",
            timeReceived = 10000,
            rankInfo = null,
            roles = null
        )
        val champion4 = ChampionMastery(
            championId = 4,
            championLevel = 1.0,
            championPoints = 500.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "123",
            champName = "Zion",
            timeReceived = 10000,
            rankInfo = null,
            roles = null
        )
        return listOf(champion1, champion2, champion3, champion4)
    }

    private fun getChampionsMock(
        query: String,
        sortOrder: SortOrder,
        id: String,
        showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<List<ChampionMastery>> =
        when (sortOrder) {
            SortOrder.BY_NAME -> getChampionsByName(
                id,
                query,
                showADC,
                showSup,
                showMid,
                showJungle,
                showTop,
                showAll
            )
            SortOrder.BY_MASTERY_POINTS -> getChampionsByMasteryPoints(
                id,
                query,
                showADC,
                showSup,
                showMid,
                showJungle,
                showTop,
                showAll
            )
        }

    private fun getChampionsByName(
        id: String,
        query: String,
        showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<List<ChampionMastery>> = flow {
        val newChampList = mutableListOf<ChampionMastery>()
        championList.sortBy { it.champName }
        for (champ in championList) {
            if (((champ.roles?.BOTTOM == showADC && showADC) || (champ.roles?.UTILITY == showSup && showSup)
                || (champ.roles?.MIDDLE == showMid && showMid) || (champ.roles?.JUNGLE == showJungle && showJungle)
                || (champ.roles?.TOP == showTop && showTop) || (champ.roles?.ALL == showAll && showAll)
            ) && id == champ.summonerId) {
                if(champ.champName.startsWith(query)){
                    newChampList.add(champ)
                }
            }
        }
        emit(newChampList)
    }


    private fun getChampionsByMasteryPoints(
        id: String,
        query: String,
        showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<List<ChampionMastery>> = flow {
        val newChampList = mutableListOf<ChampionMastery>()
        championList.sortByDescending { it.championPoints }
        for (champ in championList) {
            if (((champ.roles?.BOTTOM == showADC && showADC) || (champ.roles?.UTILITY == showSup && showSup)
                || (champ.roles?.MIDDLE == showMid && showMid) || (champ.roles?.JUNGLE == showJungle && showJungle)
                || (champ.roles?.TOP == showTop && showTop) || (champ.roles?.ALL == showAll && showAll)
            ) && id == champ.summonerId) {
                if(champ.champName.startsWith(query)){
                    newChampList.add(champ)
                }
            }
        }
        emit(newChampList)
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
    ): Flow<Resource<List<ChampionMastery>>> = networkBoundResource(
        query = {

            val summoner = getCurrentSummoner()
            getChampionsMock(
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
        },
        shouldFetch = {
            val summoner = getCurrentSummoner()
            it.isEmpty() || it == null || (summoner?.let { it1 ->
                checkSummonerFreshness(it1.id)
            } == false)
        },
        fetch = {
            val summoner = getCurrentSummoner()
            val url = Constants.ALL_CHAMPION_MASTERIES + (summoner?.id
                    ) + "?api_key=" + BuildConfig.API_KEY
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
        },
        saveFetchResult = { championList ->
            insertChampionList(championList)

        }
    )

    private fun insertChampionList(list: List<ChampionMastery>) {
        for (item in list) {
            championList.add(item)
        }
    }

    override suspend fun matchListForInitBoost(): Resource<List<String>?> {
        val summoner = getCurrentSummoner()
        if (summoner != null) {
            if (!summoner.initBoostCalculated) {
                val response = getMatchListAsync()
                return try {
                    Resource.Success(response.await())
                } catch (e: Exception) {
                    Resource.Error(e, null)
                }
            }
        }
        return Resource.Success(null)
    }

    private fun getMatchListAsync(): Deferred<List<String>> {
        return if (shouldReturnNetworkError) {
            CompletableDeferred(null)
        } else {
            val list = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
            CompletableDeferred(list)
        }
    }

    override suspend fun getMatchDetails(matchId: String): Resource<MatchDetails> {
        val response = getMatchDetailsMockAsync(matchId)
        return try {
            Resource.Success(response.await())
        } catch (e: java.lang.Exception) {
            Resource.Error(e, null)
        }
    }

    private suspend fun getMatchDetailsMockAsync(matchId: String): Deferred<MatchDetails> {
        return if (shouldReturnNetworkError) {
            CompletableDeferred(null)
        } else {
            val summoner = getCurrentSummoner()
            val participantList = listOf(
                summoner?.puuid ?: "0",
                "100",
                "200",
                "500",
                "300",
                "123",
                "101",
                "90",
                "91",
                "102"
            )
            val metaData = MetaData(matchId, participantList)
            val participantDataSummoner = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 99,
                championName = "Lux",
                puuid = summoner?.puuid ?: "0",
                win = true
            )
            val participantData100 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 1,
                championName = "Annie",
                puuid = "100",
                win = true
            )
            val participantData200 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 2,
                championName = "Olaf",
                puuid = "200",
                win = true
            )
            val participantData500 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 3,
                championName = "Galio",
                puuid = "500",
                win = true
            )
            val participantData300 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 4,
                championName = "Twisted Fate",
                puuid = "300",
                win = true
            )
            val participantData123 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 5,
                championName = "Xin Zhao",
                puuid = "123",
                win = false
            )
            val participantData101 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 6,
                championName = "Urgot",
                puuid = "101",
                win = false
            )
            val participantData90 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 7,
                championName = "LeBlanc",
                puuid = "90",
                win = false
            )
            val participantData91 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 10,
                championName = "Kayle",
                puuid = "91",
                win = false
            )
            val participantData102 = ParticipantData(
                assists = 1,
                baronKills = 3,
                deaths = 9,
                dragonKills = 2,
                kills = 4,
                neutralMinionsKilled = 60,
                objectivesStolen = 0,
                totalDamageDealtToChampions = 30000.0,
                totalMinionsKilled = 100,
                visionScore = 20,
                championId = 12,
                championName = "Alistar",
                puuid = "102",
                win = false
            )
            val participantDataList = listOf(
                participantData100,
                participantData101,
                participantData102,
                participantData123,
                participantData200,
                participantData300,
                participantData500,
                participantData90,
                participantData91,
                participantDataSummoner
            )
            val info =
                Info(gameDuration = 20.0, gameMode = "CLASSIC", participants = participantDataList)
            val matchDetails = MatchDetails(metadata = metaData, info = info, status = null)
            CompletableDeferred(matchDetails)
        }
    }

}