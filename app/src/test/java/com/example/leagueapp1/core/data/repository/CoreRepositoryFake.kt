package com.example.leagueapp1.core.data.repository

import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.core.domain.models.Summoner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CoreRepositoryFake : CoreRepositoryInterface {

    private val summonerListLocal = mutableListOf<Summoner>()
 //   private val summonerListRemote = mutableListOf<SummonerFromKtor>()

    private var mainSummonerRemote : SummonerFromKtor? = null

    private var shouldReturnNetworkError = false

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    override suspend fun insertSummonerRemote(summoner: SummonerFromKtor) {
    //    summonerListRemote.add(summoner)
        mainSummonerRemote = summoner
    }

    override fun getAllSummoners(): Flow<List<Summoner>> {
        return flow {
            emit(summonerListLocal)
        }
    }

    override suspend fun getMainSummonerLocal(): Summoner? {
        return summonerListLocal.find { it.isMainSummoner }
    }

    override suspend fun getMainSummonerRemote(): NoLoadResource<SummonerFromKtor>? {
        return when {
            shouldReturnNetworkError -> NoLoadResource.Error(Throwable("Network Error"))
            mainSummonerRemote == null -> NoLoadResource.Error(Throwable("Main Summoner is null"))
            else -> NoLoadResource.Success(mainSummonerRemote!!)
        }
    }

    override suspend fun insertSummonerLocal(summoner: Summoner) {
        val exists = summonerListLocal.find { it.id == summoner.id }
        if(exists != null){
            summonerListLocal.remove(exists)
            summonerListLocal.add(summoner)
        } else summonerListLocal.add(summoner)
    }

    override suspend fun isFreshSummoner(name: String, time: Long): Int {
        val summoner = summonerListLocal.find { it.name == name && it.timeReceived ?: 0 >= time}
        return if(summoner != null) 1 else 0
    }

    override fun checkInternetConnection(): Boolean {
        return !shouldReturnNetworkError
    }
}
