package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.core.domain.models.update.UpdateEvent
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface

class UpdateEventListUseCase(
    private val champRepo: ChampionsRepositoryInterface,
    private val coreRepo: CoreRepositoryInterface
) {
    suspend operator fun invoke(list: MutableList<UpdateEvent>, champId: Int) {
        val summoner = coreRepo.getMainSummonerLocal()
        summoner?.let {
            champRepo.updateUpdateEventList(list, it.id, champId)
        }
    }
}