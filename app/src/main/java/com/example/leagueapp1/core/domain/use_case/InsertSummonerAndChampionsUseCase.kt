package com.example.leagueapp1.core.domain.use_case

import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.domain.util.transformToSummoner
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.core.util.exhaustive
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface

class InsertSummonerAndChampionsUseCase(
    private val coreRepo: CoreRepositoryInterface,
    private val champRepo: ChampionsRepositoryInterface
) {
    suspend operator fun invoke(ktorSummoner: SummonerFromKtor?): Result<Unit> {

        val data = ktorSummoner
            ?: when (val response = coreRepo.getMainSummonerRemote()) {
                is NoLoadResource.Success -> {
                    response.data!!
                }
                is NoLoadResource.Error -> {
                    return Result.failure(response.error ?: Throwable("Network Error"))
                }
                else -> return Result.failure(Throwable("Network Error"))
            }


        val champList = data.championList
        val summoner = data.transformToSummoner()

        coreRepo.insertSummonerLocal(
            summoner.apply {
                isMainSummoner = true
                timeReceived = System.currentTimeMillis()
            }
        )

        if (champList.isNotEmpty()) {
            champRepo.insertChampions(champList)
        }
        return Result.success(Unit)
    }
}