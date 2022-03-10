package com.example.leagueapp1.feature_champions.data.remote

import com.example.leagueapp1.core.domain.models.update.UpdateChampsRanks
import retrofit2.Response
import retrofit2.http.GET


interface ChampApi {

    @GET("/updateChampRanks")
    suspend fun updateChampRanks(): Response<UpdateChampsRanks>
}