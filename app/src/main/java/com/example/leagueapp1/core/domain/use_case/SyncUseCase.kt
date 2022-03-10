package com.example.leagueapp1.core.domain.use_case

import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.domain.util.transformToKtorSummoner
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.example.leagueapp1.feature_champions.domain.use_case.GetChampionUpdatesUseCase

class SyncUseCase(
    private val coreRepo: CoreRepositoryInterface,
    private val champRepo: ChampionsRepositoryInterface,
    private val insertSummonerAndChampionsUseCase: InsertSummonerAndChampionsUseCase,
    private val getChampionUpdatesUseCase: GetChampionUpdatesUseCase
) {

    suspend operator fun invoke(): Result<Boolean> {
        val currSummoner = coreRepo.getMainSummonerLocal()
        if (currSummoner != null) {
            val ktorSummoner = currSummoner.transformToKtorSummoner()

            //Seems like its crashing here, Also UpdateSummoner not working in server
            val champList = champRepo.getAllChamps(ktorSummoner.id)
            val map = hashMapOf<Int, Champion>()
            champList.forEach {
                map[it.championId] = it
            }
            coreRepo.insertSummonerRemote(ktorSummoner.apply { championList = map })
        }

        return getChampionUpdatesUseCase().onSuccess { isUpdate ->
            insertSummonerAndChampionsUseCase(null)
                .onSuccess {
                    //Call for Updates
                    Result.success(isUpdate)
                }.onFailure {
                    Result.failure<Unit>(it)
                }
        }.onFailure {
            Result.failure<Unit>(it)
        }

    }
}