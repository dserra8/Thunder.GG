package com.example.leagueapp1.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.leagueapp1.adapters.HeaderItem
import com.example.leagueapp1.data.local.*
import com.example.leagueapp1.data.remote.*
import com.example.leagueapp1.data.remote.requests.SummonerFromKtor
import com.example.leagueapp1.repository.LeagueRepository.Companion.FRESH_TIMEOUT
import com.example.leagueapp1.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import javax.inject.Inject

class FakeRepository @Inject constructor(
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

    override suspend fun login(email: String, password: String): Resource<String> {
        TODO("Not yet implemented")
    }

    override suspend fun register(
        email: String,
        password: String,
        summonerName: String
    ): Resource<String> {
        TODO("Not yet implemented")
    }

    override suspend fun syncSummonerAndChamps() {
        TODO("Not yet implemented")
    }


    /**
     * Summoner Functions
     */

    override var currentSummoner: SummonerProperties? = null
    override suspend fun transformSummonerObject(summoner: SummonerProperties): SummonerFromKtor {
        TODO("Not yet implemented")
    }

    override fun getAllSummoners(): Flow<List<SummonerProperties>> = flow {
        emit(summonerList)
    }

    override suspend fun insertSummoner(summoner: SummonerProperties) {
        summonerList.add(summoner)
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

    override suspend fun getSummonerByName(summonerName: String): SummonerProperties? {
        for (summoner in summonerList) {
            if (summoner.name == summonerName) {
                return summoner
            }
        }
        return null
    }

    override suspend fun changeMainSummoner(puuid: String) {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveSaveSummoner() {
        TODO("Not yet implemented")
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

    private fun createChampionRates(
        UTILITY: Rate = Rate(1.2),
        JUNGLE: Rate = Rate(0.1),
        BOTTOM: Rate = Rate(0.2),
        MIDDLE: Rate = Rate(0.7),
        TOP: Rate = Rate(0.2)
    ) = ChampionRates(
        UTILITY = UTILITY,
        JUNGLE = JUNGLE,
        BOTTOM = BOTTOM,
        MIDDLE = MIDDLE,
        TOP = TOP
    )


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

    override suspend fun insertChampions(champs: List<ChampionMastery>) {
        TODO("Not yet implemented")
    }

    override suspend fun getChampion(champId: Int, summonerId: String): ChampionMastery? {
        for (champ in championList) {
            if (champ.championId == champId && champ.summonerId == summonerId) {
                return champ
            }
        }
        return null
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
        profileIconId: Int,
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
                if (champ.champName?.startsWith(query) == true) {
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
                if (champ.champName?.startsWith(query) == true) {
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
            getChampionsMock(
                searchQuery,
                sortOrder,
                 "0",
                showADC,
                showSup,
                showMid,
                showJungle,
                showTop,
                showAll
            )
        },
        shouldFetch = {
//            val summoner = getCurrentSummoner()
//            it.isEmpty() || it == null || (summoner?.let { it1 ->
//                checkSummonerFreshness(it1.id)
//            } == false)
                      true
        },
        fetch = {
            val mutableChampionList = mutableListOf<ChampionMastery>()
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



    private suspend fun getMatchDetailsMockAsync(matchId: String): Response<MatchDetails> {
        return if (shouldReturnNetworkError) {
            Response.error(
                404, "{\"key\":[\"somestuff\"]}"
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
        } else {
            val summoner =currentSummoner
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
            Response.success(matchDetails)
        }
    }

}