package com.example.leagueapp1.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.leagueapp1.BuildConfig
import com.example.leagueapp1.adapters.HeaderItem
import com.example.leagueapp1.database.*
import com.example.leagueapp1.network.*
import com.example.leagueapp1.repository.LeagueRepository.Companion.FRESH_TIMEOUT
import com.example.leagueapp1.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import javax.inject.Inject

class FakeRepositoryAndroidTest @Inject constructor(
    private val dispatchers: DispatcherProvider
) : LeagueRepository {
    private val championList = mutableListOf<ChampionMastery>()
    private val summonerList = mutableListOf<SummonerProperties>()
    private val championRoleList = mutableListOf<ChampionRoleRates>()

    private val serverSummonerList =
        listOf("Chasik", "Kirokato", "itsjerez", "ChiTownsFinest", "MysticsJL")

    private val _observableChampionRoleList =
        MutableLiveData<List<ChampionRoleRates>>(championRoleList)
    val observableChampionRoleList: LiveData<List<ChampionRoleRates>> = _observableChampionRoleList


    private var id: Int = 0

    var refreshChampionRatesCalled = false

    //   private val championListFlow = Flow<List<SummonerProperties>>
    private var shouldReturnNetworkError = false

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    /**
     * Function to make safe api calls and returns a kotlin Result.
     */
    private suspend inline fun <T> safeApiCall(crossinline responseFunc: suspend () -> Response<T>): Result<T> {

        val response: Response<T>
        try {
            response = withContext(dispatchers.io) {
                responseFunc.invoke()
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!response.isSuccessful) {
            return Result.failure(Exception(response.errorBody().toString()))
        } else {
            if (response.body() == null) {
                return Result.failure(Exception("Unknown Error"))
            }
        }

        return Result.success(response.body()!!)
    }


    /**
     * Summoner Functions
     */

    override val summoner: Flow<SummonerProperties?> = getSummonerFlow()

    override suspend fun getSummonerPropertiesAsync(url: String): Response<SummonerProperties> {
        return if (shouldReturnNetworkError) {
            Response.error(
                404, "{\"key\":[\"somestuff\"]}"
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
        } else {
            val name = url.substringBefore("?").substringAfterLast("/")
            val response: Response<SummonerProperties>
            if (serverSummonerList.contains(name)) {
                val summoner = createSummonerProperties(
                    id = id.toString(),
                    accountId = id.toString(),
                    puuid = id.toString(),
                    name = name,
                    summonerLevel = 10.0,
                )
                id++
                response = Response.success(summoner)
            } else {
                response = Response.error(
                    404, "{\"key\":[\"notInDatabase\"]}"
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
            response
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
                safeApiCall { getSummonerPropertiesAsync("${Constants.SUMMONER_INFO}$summonerName?api_key=${BuildConfig.API_KEY}") }
            response.onSuccess { summoner ->
                val rankListResponse = getSummonerSoloRank(summoner.id)
                var rank: String? = null
                when (rankListResponse) {
                    is Resource.Error -> {
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
        val response = safeApiCall { getSummonerRankAsync() }
        var result: Resource<List<RankDetails>?> = Resource.Loading()
        response.onSuccess {
            result = Resource.Success(it)
        }
        response.onFailure {
            result = Resource.Error(it)
        }
        return result
    }

    private fun getSummonerRankAsync(): Response<List<RankDetails>> {
        return if (shouldReturnNetworkError) {
            Response.error(
                404, "{\"key\":[\"somestuff\"]}"
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
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
            Response.success(rankList)
        }
    }

    override val roleList: LiveData<List<ChampionRoleRates>> = getTrueRoleList()

    override suspend fun refreshChampionRates(): String {
        refreshChampionRatesCalled = true
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
        val response = safeApiCall { getChampionRatesMockAsync() }
        var result: Resource<ChampionRoles> = Resource.Loading(null)
        response.onSuccess {
            result = Resource.Success(it)
        }
        response.onFailure {
            result = Resource.Error(it, null)
        }
        return result
    }

    private fun getChampionRatesMockAsync(): Response<ChampionRoles> {
        return if (shouldReturnNetworkError) {
            Response.error(
                404, "{\"key\":[\"somestuff\"]}"
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
        } else {
            val data = Data(
                `1` = createChampionRates(),
                `10` = createChampionRates(),
                `101` = createChampionRates(),
                `102` = createChampionRates(),
                `103` = createChampionRates(),
                `104` = createChampionRates(),
                `105` = createChampionRates(),
                `106` = createChampionRates(),
                `107` = createChampionRates(),
                `11` = createChampionRates(),
                `110` = createChampionRates(),
                `111` = createChampionRates(),
                `112` = createChampionRates(),
                `113` = createChampionRates(),
                `114` = createChampionRates(),
                `115` = createChampionRates(),
                `117` = createChampionRates(),
                `119` = createChampionRates(),
                `12` = createChampionRates(),
                `120` = createChampionRates(),
                `121` = createChampionRates(),
                `122` = createChampionRates(),
                `126` = createChampionRates(),
                `127` = createChampionRates(),
                `13` = createChampionRates(),
                `131` = createChampionRates(),
                `133` = createChampionRates(),
                `134` = createChampionRates(),
                `136` = createChampionRates(),
                `14` = createChampionRates(),
                `141` = createChampionRates(),
                `142` = createChampionRates(),
                `143` = createChampionRates(),
                `145` = createChampionRates(),
                `147` = createChampionRates(),
                `15` = createChampionRates(),
                `150` = createChampionRates(),
                `154` = createChampionRates(),
                `157` = createChampionRates(),
                `16` = createChampionRates(),
                `161` = createChampionRates(),
                `163` = createChampionRates(),
                `164` = createChampionRates(),
                `17` = createChampionRates(),
                `18` = createChampionRates(),
                `19` = createChampionRates(),
                `2` = createChampionRates(),
                `20` = createChampionRates(),
                `201` = createChampionRates(),
                `202` = createChampionRates(),
                `203` = createChampionRates(),
                `21` = createChampionRates(),
                `22` = createChampionRates(),
                `222` = createChampionRates(),
                `223` = createChampionRates(),
                `23` = createChampionRates(),
                `234` = createChampionRates(),
                `235` = createChampionRates(),
                `236` = createChampionRates(),
                `238` = createChampionRates(),
                `24` = createChampionRates(),
                `240` = createChampionRates(),
                `245` = createChampionRates(),
                `246` = createChampionRates(),
                `25` = createChampionRates(),
                `254` = createChampionRates(),
                `26` = createChampionRates(),
                `266` = createChampionRates(),
                `267` = createChampionRates(),
                `268` = createChampionRates(),
                `27` = createChampionRates(),
                `28` = createChampionRates(),
                `29` = createChampionRates(),
                `3` = createChampionRates(),
                `30` = createChampionRates(),
                `31` = createChampionRates(),
                `32` = createChampionRates(),
                `33` = createChampionRates(),
                `34` = createChampionRates(),
                `35` = createChampionRates(),
                `350` = createChampionRates(),
                `36` = createChampionRates(),
                `360` = createChampionRates(),
                `37` = createChampionRates(),
                `38` = createChampionRates(),
                `39` = createChampionRates(),
                `4` = createChampionRates(),
                `40` = createChampionRates(),
                `41` = createChampionRates(),
                `412` = createChampionRates(),
                `42` = createChampionRates(),
                `420` = createChampionRates(),
                `421` = createChampionRates(),
                `427` = createChampionRates(),
                `429` = createChampionRates(),
                `43` = createChampionRates(),
                `432` = createChampionRates(),
                `44` = createChampionRates(),
                `45` = createChampionRates(),
                `48` = createChampionRates(),
                `497` = createChampionRates(),
                `498` = createChampionRates(),
                `5` = createChampionRates(),
                `50` = createChampionRates(),
                `51` = createChampionRates(),
                `516` = createChampionRates(),
                `517` = createChampionRates(),
                `518` = createChampionRates(),
                `523` = createChampionRates(),
                `526` = createChampionRates(),
                `53` = createChampionRates(),
                `54` = createChampionRates(),
                `55` = createChampionRates(),
                `555` = createChampionRates(),
                `56` = createChampionRates(),
                `57` = createChampionRates(),
                `58` = createChampionRates(),
                `59` = createChampionRates(),
                `6` = createChampionRates(),
                `60` = createChampionRates(),
                `61` = createChampionRates(),
                `62` = createChampionRates(),
                `63` = createChampionRates(),
                `64` = createChampionRates(),
                `67` = createChampionRates(),
                `68` = createChampionRates(),
                `69` = createChampionRates(),
                `7` = createChampionRates(),
                `72` = createChampionRates(),
                `74` = createChampionRates(),
                `75` = createChampionRates(),
                `76` = createChampionRates(),
                `77` = createChampionRates(),
                `777` = createChampionRates(),
                `78` = createChampionRates(),
                `79` = createChampionRates(),
                `8` = createChampionRates(),
                `80` = createChampionRates(),
                `81` = createChampionRates(),
                `82` = createChampionRates(),
                `83` = createChampionRates(),
                `84` = createChampionRates(),
                `85` = createChampionRates(),
                `86` = createChampionRates(),
                `875` = createChampionRates(),
                `876` = createChampionRates(),
                `887` = createChampionRates(),
                `89` = createChampionRates(),
                `9` = createChampionRates(),
                `90` = createChampionRates(),
                `91` = createChampionRates(),
                `92` = createChampionRates(),
                `96` = createChampionRates(),
                `98` = createChampionRates(),
                `99` = createChampionRates()
            )
            Response.success(ChampionRoles(data, patch = "12.5"))
        }
    }

    override suspend fun insertTrueRoleList(list: List<ChampionRoleRates>) {
        for (item in list) {
            championRoleList.add(item)
        }
        refreshLiveData()
    }

    private fun refreshLiveData() {
        _observableChampionRoleList.postValue(championRoleList)
    }


    override fun getTrueRoleList(): LiveData<List<ChampionRoleRates>> {
        return _observableChampionRoleList
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

    override suspend fun getAllChampionMasteries(url: String): Resource<List<ChampionMastery>?> {
        val response = safeApiCall { getAllChampionMasteriesMockAsync() }
        var result: Resource<List<ChampionMastery>?> = Resource.Loading(null)
        response.onSuccess { list ->
            for (champion in list) {
                champion.champName = Constants.champMap[champion.championId] ?: "Unknown"
            }
            result = Resource.Success(list)
        }
        response.onFailure {
            result = Resource.Error(it, null)
        }
        return result
    }

    private fun getAllChampionMasteriesMockAsync(): Response<List<ChampionMastery>> {
        return if (shouldReturnNetworkError) {
            Response.error(
                404, "{\"key\":[\"somestuff\"]}"
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
        } else {
            val champion1 = createChampionMastery()
            val champion2 = createChampionMastery(
                championId = 2,
                championPoints = 300.0,
                champName = "Olaf")
            val champion3 = createChampionMastery(
                championId = 3,
                championPoints = 400.0,
                champName = "Galio",
                rankInfo = ChampRankInfo(experienceBoost = 20, recentBoost = 0, rank = "SILVER")
            )
            val champion4 = createChampionMastery(
                championId = 4,
                championPoints = 500.0,
                champName = "Twisted Fate")
            Response.success(listOf(champion1, champion2, champion3, champion4))
        }
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
                        ) && id == champ.summonerId
            ) {
                if (champ.champName.startsWith(query)) {
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
                        ) && id == champ.summonerId
            ) {
                if (champ.champName.startsWith(query)) {
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
            val mutableChampionList = mutableListOf<ChampionMastery>()
            val summoner = getCurrentSummoner()
            val url = Constants.ALL_CHAMPION_MASTERIES + (summoner?.id
                    ) + "?api_key=" + BuildConfig.API_KEY
            when (val response = getAllChampionMasteries(url)) {
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                }
                is Resource.Success -> {
                    for (champion in response.data!!) {
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
                        val rankInfo = champion.rankInfo ?: ChampRankInfo()
                        mutableChampionList.add(
                            champion.copy(
                                roles = champRole.roles,
                                timeReceived = System.currentTimeMillis(),
                                rankInfo = rankInfo
                            )
                        )
                    }
                }
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
        var result: Resource<List<String>?> = Resource.Success(null)
        val summoner = getCurrentSummoner()
        if (summoner != null) {
            if (!summoner.initBoostCalculated) {
                val response = safeApiCall { getMatchListAsync() }
                response.onSuccess {
                    result = Resource.Success(it)
                }
                response.onFailure {
                    result = Resource.Error(it, null)
                }
            }
        } else result = Resource.Error(Throwable("Error Retrieving Summoner"))
        return result
    }

    private fun getMatchListAsync(): Response<List<String>> {
        return if (shouldReturnNetworkError) {
            Response.error(
                404, "{\"key\":[\"somestuff\"]}"
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
        } else {
            Response.success(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))

        }
    }

    override suspend fun getMatchDetails(matchId: String): Resource<MatchDetails> {
        val response = safeApiCall { getMatchDetailsMockAsync(matchId) }

        var result: Resource<MatchDetails> = Resource.Loading(null)
        response.onSuccess {
            result = Resource.Success(it)
        }
        response.onFailure {
            result = Resource.Error(it, null)
        }
        return result
    }



    private suspend fun getMatchDetailsMockAsync(matchId: String): Response<MatchDetails> {
        return if (shouldReturnNetworkError) {
            Response.error(
                404, "{\"key\":[\"somestuff\"]}"
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
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
            val participantDataSummoner = createParticipantData(
                championName = "Lux",
                puuid = summoner?.puuid ?: "0"
            )
            val participantData100 = createParticipantData(
                championName = "Annie",
                puuid = "100"
            )
            val participantData200 = createParticipantData(
                championName = "Olaf",
                puuid = "200"
            )
            val participantData500 = createParticipantData(
                championName = "Galio",
                puuid = "500"
            )
            val participantData300 = createParticipantData(
                championName = "Twisted Fate",
                puuid = "300"
            )
            val participantData123 = createParticipantData(
                championId = 5,
                championName = "Xin Zhao",
                puuid = "123",
                win = false
            )
            val participantData101 = createParticipantData(
                championId = 6,
                championName = "Urgot",
                puuid = "101",
                win = false
            )
            val participantData90 = createParticipantData(
                championId = 7,
                championName = "LeBlanc",
                puuid = "90",
                win = false
            )
            val participantData91 = createParticipantData(
                championId = 10,
                championName = "Kayle",
                puuid = "91",
                win = false
            )
            val participantData102 = createParticipantData(
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
            Response.success(matchDetails)
        }
    }

}