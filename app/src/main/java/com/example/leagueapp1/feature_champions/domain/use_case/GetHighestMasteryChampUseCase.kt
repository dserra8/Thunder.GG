package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface

class GetHighestMasteryChampUseCase(
    private val champRepo: ChampionsRepositoryInterface,
    private val coreRepo: CoreRepositoryInterface
) {

    suspend operator fun invoke(): Champion? {

        val summoner = coreRepo.getMainSummonerLocal()
        var champ : Champion? = null
        summoner?.let {
            champ = champRepo.getHighestMasteryChampion(it.id)
        }
        return champ
    }
}