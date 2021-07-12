package com.example.leagueapp1.network


import com.example.leagueapp1.database.ChampionMastery
import com.example.leagueapp1.database.SummonerProperties
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

import retrofit2.http.Url

interface RiotApiService {

    @GET
    fun getSummonerPropertiesAsync(@Url url:String): Deferred<SummonerProperties>

    @GET
    fun getChampionRatesAsync(@Url url:String): Deferred<ChampionRoles>

    @GET
    suspend fun getAllChampionMasteries(@Url url:String): List<ChampionMastery>

    @GET
    fun getMatchListAsync(@Url url:String): Deferred<List<String>>

    @GET
    fun getMatchDetailsAsync(@Url url:String): Deferred<MatchDetails>

    @GET
    fun getSummonerRankAsync(@Url url:String): Deferred<List<RankDetails>>
}
