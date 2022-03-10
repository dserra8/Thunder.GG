package com.example.leagueapp1.core.data.repository

import android.app.Application
import com.example.leagueapp1.core.data.remote.CoreApi
import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.util.ApiUtil
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.core.domain.models.Summoner
import com.example.leagueapp1.data.local.SummonersDao
import com.example.leagueapp1.util.checkForInternetConnection
import kotlinx.coroutines.flow.Flow

class CoreRepository(
    private val apiUtil: ApiUtil,
    private val coreApi: CoreApi,
    private val summonersDao: SummonersDao,
    private val context: Application
) : CoreRepositoryInterface {

    override suspend fun insertSummonerRemote(summoner: SummonerFromKtor) {
        apiUtil.safeApiCall { coreApi.addSummoner(summoner) }
    }

    override suspend fun isFreshSummoner(name: String, time: Long): Int = summonersDao.isFreshSummoner(name, time)

    override fun checkInternetConnection() = checkForInternetConnection(context)

    override fun getAllSummoners(): Flow<List<Summoner>> = summonersDao.getAllSummoners()

    override suspend fun getMainSummonerLocal(): Summoner? = summonersDao.getSummoner()

    override suspend fun getMainSummonerRemote(): NoLoadResource<SummonerFromKtor>? {
        val apiResult = apiUtil.safeApiCall { coreApi.getMainSummoner() }
        var result: NoLoadResource<SummonerFromKtor>? = null
        apiResult.onSuccess { summoner ->
            result = NoLoadResource.Success(summoner)
        }
        apiResult.onFailure {
            result = NoLoadResource.Error(it)
        }
        return result
    }

    override suspend fun insertSummonerLocal(summoner: Summoner) =
        summonersDao.insertSummoner(summoner)
}