package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface

class GetChampionUseCase(
    private val coreRepo: CoreRepositoryInterface,
    private val champRepo: ChampionsRepositoryInterface,
) {

    suspend operator fun invoke(id: Int) : Champion? {
        val summoner = coreRepo.getMainSummonerLocal()
        return if (summoner != null) {
            champRepo.getChampion(champId = id, summonerId = summoner.id)
        } else null
    }
}