package com.example.leagueapp1.repository


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.leagueapp1.adapters.HeaderItem
import com.example.leagueapp1.data.local.*
import com.example.leagueapp1.data.remote.ChampionRoles
import com.example.leagueapp1.data.remote.RiotApiService
import com.example.leagueapp1.data.remote.requests.LoginRequest
import com.example.leagueapp1.data.remote.requests.SummonerFromKtor
import com.example.leagueapp1.util.*
import com.example.leagueapp1.util.Constants.MILLI_SECONDS_DAY
import com.example.leagueapp1.util.Constants.champMap
import com.leagueapp1.data.requests.AccountRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.System.currentTimeMillis
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
                changeMainSummoner(it.puuid!!)
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

    override suspend fun changeMainSummoner(puuid: String) {
        val summonerList = getAllSummoners().first()
        summonerList.forEach {
            if (puuid != it.puuid) {
                insertSummoner(
                    it.apply { isMainSummoner = false }
                )
            } else {
                insertSummoner(
                    it.apply { isMainSummoner = true }
                )
            }
        }
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
                            rank = rank,
                            timeReceived = currentTimeMillis()
                        )
                    )
                    currentChampionList?.let { champs ->
                        insertChampions(champs)
                    }
                }
                refreshChampionRates()
                changeMainSummoner(summonerFromKtor.puuid)
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
        if (currentSummoner != null) {
            safeApiCall {
                api.addSummoner(
                    transformSummonerObject(
                        currentSummoner!!
                    )
                )
            }
            retrieveSaveSummoner()
        } else {
            retrieveSaveSummoner()
        }

    }

    override suspend fun retrieveSaveSummoner() {
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
                        rank = rank,
                        timeReceived = currentTimeMillis()
                    )
                )
                currentChampionList?.let { champs ->
                    insertChampions(champs)
                }
            }
        }
    }

    override suspend fun transformSummonerObject(summoner: SummonerProperties): SummonerFromKtor {
        val champList = getChampions(
            "",
            SortOrder.BY_MASTERY_POINTS,
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
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

    suspend fun getSummoner(): SummonerProperties? = summonersDao.getSummoner()

    override fun getAllSummoners(): Flow<List<SummonerProperties>> =
        summonersDao.getAllSummoners()

    override suspend fun insertSummoner(summoner: SummonerProperties) =
        summonersDao.insertSummoner(summoner)

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
                    summonerIconId = profileIconId,
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
            summoner?.let {
                val time = currentTimeMillis()
                val isFresh = summonersDao.isFreshSummoner(
                    summoner.name,
                    time - MILLI_SECONDS_DAY
                ) == 1
                (list.isEmpty() || !isFresh) && checkForInternetConnection(context)
            } ?: checkForInternetConnection(context)

        },
        fetch = {
            syncSummonerAndChamps()
            currentChampionList
        },
        saveFetchResult = { list ->
            list?.let {
                insertChampions(it)
            }
        }

    )
}