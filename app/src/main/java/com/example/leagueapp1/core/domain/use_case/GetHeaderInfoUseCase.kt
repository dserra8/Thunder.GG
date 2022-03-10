package com.example.leagueapp1.core.domain.use_case

import com.example.leagueapp1.adapters.HeaderItem
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetHeaderInfoUseCase(
    private val coreRepo: CoreRepositoryInterface
) {

    suspend operator fun invoke(
        champion: ChampListState
    ): Flow<HeaderItem> {

        val summoner = coreRepo.getMainSummonerLocal()

        return flow {
            val splashName = when (champion) {
                is ChampListState.Ready -> {
                    champion.splashName
                }
                is ChampListState.Empty -> {
                    "Lux"
                }
            }
            emit(
                HeaderItem(
                    name = summoner?.name ?: "No User",
                    summonerIconId = summoner?.profileIconId ?: 10,
                    splashName = splashName
                )
            )
        }
    }

    sealed class ChampListState {
        data class Ready(val splashName: String) : ChampListState()
        object Empty : ChampListState()
    }
}
