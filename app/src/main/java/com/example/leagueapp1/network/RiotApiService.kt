package com.example.leagueapp1.network


import com.example.leagueapp1.database.ChampionMastery
import com.example.leagueapp1.database.SummonerProperties
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RiotApiService {

    @GET
    suspend fun getSummonerPropertiesAsync(@Url url:String): Response<SummonerProperties>

    @GET
    suspend fun getChampionRatesAsync(@Url url:String): Response<ChampionRoles>

    @GET
    suspend fun getAllChampionMasteries(@Url url:String): Response<List<ChampionMastery>>

    @GET
    suspend fun getMatchListAsync(@Url url:String): Response<List<String>>

    @GET
    suspend fun getMatchDetailsAsync(@Url url:String): Response<MatchDetails>

    @GET
    suspend fun getSummonerRankAsync(@Url url:String): Response<List<RankDetails>>
}
