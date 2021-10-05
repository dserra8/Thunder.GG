package com.example.leagueapp1.data.remote


import com.example.leagueapp1.data.local.ChampionMastery
import com.example.leagueapp1.data.local.SummonerProperties
import com.example.leagueapp1.data.remote.requests.LoginRequest
import com.example.leagueapp1.data.remote.requests.SummonerFromKtor
import com.example.leagueapp1.data.remote.responses.SimpleResponse
import com.leagueapp1.data.requests.AccountRequest
import com.leagueapp1.data.responses.RegisterResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface RiotApiService {

    @GET("/getSummoner")
    suspend fun getMainSummoner(): Response<SummonerFromKtor>

    @POST("/addSummoner")
    suspend fun addSummoner(
            @Body summoner: SummonerFromKtor
    ) : Response<ResponseBody>

    @POST("/register")
    suspend fun register(
        @Body registerRequest: AccountRequest
    ) : Response<RegisterResponse>

    @POST("/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ) : Response<SimpleResponse>

    @GET
    suspend fun getChampionRatesAsync(@Url url:String): Response<ChampionRoles>
}
