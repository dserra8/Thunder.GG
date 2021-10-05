package com.example.leagueapp1.repository


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.leagueapp1.BuildConfig
import com.example.leagueapp1.adapters.HeaderItem
import com.example.leagueapp1.data.local.*
import com.example.leagueapp1.data.remote.ChampionRoles
import com.example.leagueapp1.data.remote.MatchDetails
import com.example.leagueapp1.data.remote.RankDetails
import com.example.leagueapp1.data.remote.RiotApiService
import com.example.leagueapp1.data.remote.requests.LoginRequest
import com.example.leagueapp1.data.remote.requests.SummonerFromKtor
import com.example.leagueapp1.database.SortOrder
import com.example.leagueapp1.repository.LeagueRepository.Companion.FRESH_TIMEOUT
import com.example.leagueapp1.util.*
import com.example.leagueapp1.util.Constants.champMap
import com.leagueapp1.data.requests.AccountRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject


class Repository @Inject constructor(
    private val api: RiotApiService,
    private val db: LeagueDatabase,
    private val dispatchers: DispatcherProvider,
    private val context: Application
) : LeagueRepository {

    private val summonersDao = db.summonersDao()
    private val championsDao = db.championsDao()
    private val championRoleRatesDao = db.championRoleRatesDao()


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
        val response = safeApiCall { api.login(LoginRequest(email, password)) }
        var result: Resource<String> = Resource.Loading(null)
        response.onSuccess {
            result = if (it.successful) {
                Resource.Success(it.message)
            } else {
                Resource.Error(Throwable(it.message), null)
            }
        }
        response.onFailure {
            result =
                Resource.Error(Throwable("Couldn't connect to the servers. Check your internet connection"))
        }
        return result
    }

    override suspend fun register(
        email: String,
        password: String,
        summonerName: String
    ): Resource<String> {
        val response = safeApiCall { api.register(AccountRequest(email, password, summonerName)) }
        var result: Resource<String> = Resource.Loading(null)
        response.onSuccess {
            result = if (it.successful) {

                val summonerFromKtor = it.summoner!!
                currentChampionList = summonerFromKtor.championList
                summonerFromKtor.apply {
                    insertSummoner(
                        SummonerProperties(
                            id = id,
                            accountId = accountId,
                            puuid = puuid,
                            name = name,
                            profileIconId = profileIconId,
                            revisionDate = revisionDate,
                            summonerLevel = summonerLevel,
                            isMainSummoner = true,
                            rank = rank
                        )
                    )
                    currentChampionList?.let { champs ->
                        insertChampions(champs)
                    }
                }
                refreshChampionRates()
                Resource.Success(it.message)
            } else {
                Resource.Error(Throwable(it.message), null)
            }
        }
        response.onFailure {
            result =
                Resource.Error(Throwable("Couldn't connect to the servers. Check your internet connection"))
        }
        return result
    }

    private var currentChampionList: List<ChampionMastery>? = null
    override var currentSummoner: SummonerProperties? = null

    override suspend fun syncSummonerAndChamps() {
        currentSummoner = summonersDao.getSummoner()
        currentSummoner?.let {
            val addSummonerResponse = safeApiCall { api.addSummoner(transformSummonerObject(it)) }
            val updatedSummonerResponse = safeApiCall { api.getMainSummoner() }
            updatedSummonerResponse.onSuccess { summonerKtor ->
                currentChampionList = summonerKtor.championList
                summonerKtor.apply {
                    insertSummoner(
                        SummonerProperties(
                            id = id,
                            accountId = accountId,
                            puuid = puuid,
                            name = name,
                            profileIconId = profileIconId,
                            revisionDate = revisionDate,
                            summonerLevel = summonerLevel,
                            isMainSummoner = true,
                            rank = rank
                        )
                    )
                    currentChampionList?.let { champs ->
                        insertChampions(champs)
                    }
                }

            }
        }
    }

    override suspend fun transformSummonerObject(summoner: SummonerProperties): SummonerFromKtor {
        val champList = getChampions(
            "",
            SortOrder.BY_MASTERY_POINTS,
            false,
            false,
            false,
            false,
            false,
            true
        ).first().data
        return SummonerFromKtor(
            id = summoner.id,
            accountId = summoner.accountId,
            puuid = summoner.puuid,
            name = summoner.name,
            profileIconId = summoner.profileIconId,
            revisionDate = summoner.revisionDate,
            summonerLevel = summoner.summonerLevel,
            rank = summoner.rank,
            championList = champList
        )
    }

    /**
     * Network and Database Functions for Summoners
     */

    override val summoner: Flow<SummonerProperties?> = getSummonerFlow()

    override fun getAllSummoners(): Flow<List<SummonerProperties>> =
        summonersDao.getAllSummoners()

    override suspend fun insertSummoner(summoner: SummonerProperties) =
        summonersDao.insertSummoner(summoner)

    override fun getSummonerFlow(): Flow<SummonerProperties?> =
        summonersDao.getSummonerFlow()

    override suspend fun getSummonerByName(summonerName: String): SummonerProperties? =
        summonersDao.getSummonerByName(summonerName)


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
                return if (data != null) {
                    val trueChampionRolesList = calculateRole(result.data)
                    insertTrueRoleList(trueChampionRolesList)
                    "Role List Success"
                } else "Error"
            }
        }.exhaustive
    }

    override suspend fun getChampionRatesAsync(): Resource<ChampionRoles> {
        val response = safeApiCall { api.getChampionRatesAsync(Constants.CHAMPION_RATES_URL) }
        var result: Resource<ChampionRoles> = Resource.Loading(null)
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


    override suspend fun insertChampions(champs: List<ChampionMastery>) {
        champs.forEach {
            championsDao.insertChampion(
                it.apply {
                    champName = champMap[championId]
                    roles = getChampRole(championId)?.roles ?: ChampionRoleRates(0).roles
                    rankInfo = rankInfo ?: ChampRankInfo()
                }
            )
        }
    }

    override suspend fun getChampion(champId: Int, summonerId: String): ChampionMastery? {
        return championsDao.getChampion(champId, summonerId)
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
        val summoner = currentSummoner ?: summonersDao.getSummoner()
        return championsDao.getHighestMasteryChampion(summoner?.id ?: "0")
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
                currentSummoner = summonersDao.getSummoner()
                championsDao.getChampions(
                    searchQuery,
                    sortOrder,
                    currentSummoner?.id ?: "0",
                    showADC,
                    showSup,
                    showMid,
                    showJungle,
                    showTop,
                    showAll
                )
            }
        },
        shouldFetch = { list ->
            val summoner = currentSummoner ?: summonersDao.getSummoner()
            (list.isEmpty() || (summoner?.let {
                championsDao.isFreshSummonerChampions(
                    it.id,
                    System.currentTimeMillis() - FRESH_TIMEOUT
                )
            } == 0)) && checkForInternetConnection(context)
        },
        fetch = {
            syncSummonerAndChamps()
            currentChampionList
        },
        saveFetchResult = { list ->
            list?.let {
                insertChampions(list.onEach {
                    it.timeReceived = System.currentTimeMillis()
                })
            }
        }

    )
}