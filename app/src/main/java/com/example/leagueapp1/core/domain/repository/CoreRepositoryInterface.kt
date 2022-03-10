package com.example.leagueapp1.core.domain.repository

import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.core.domain.models.Summoner
import kotlinx.coroutines.flow.Flow

interface CoreRepositoryInterface {

    suspend fun insertSummonerRemote(summoner: SummonerFromKtor)

    fun getAllSummoners(): Flow<List<Summoner>>

    suspend fun getMainSummonerLocal(): Summoner?

    suspend fun getMainSummonerRemote(): NoLoadResource<SummonerFromKtor>?

    suspend fun insertSummonerLocal(summoner: Summoner)

    suspend fun isFreshSummoner(name: String, time: Long): Int

    fun checkInternetConnection() : Boolean

}