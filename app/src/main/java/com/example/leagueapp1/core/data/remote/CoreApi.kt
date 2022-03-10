package com.example.leagueapp1.core.data.remote


import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.domain.models.update.UpdateChampsRanks
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CoreApi {
    @GET("/getSummoner")
    suspend fun getMainSummoner(): Response<SummonerFromKtor>

    @POST("/addSummoner")
    suspend fun addSummoner(
            @Body summoner: SummonerFromKtor
    ) : Response<ResponseBody>

}
