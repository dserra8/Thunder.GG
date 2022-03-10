package com.example.leagueapp1.feature_champions.domain.use_case

import androidx.room.Transaction
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.domain.use_case.SyncUseCase
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.core.util.Resource
import com.example.leagueapp1.data.local.FilterPreferences
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.example.leagueapp1.core.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetChampionsUseCase(
    private val champRepo: ChampionsRepositoryInterface,
    private val coreRepo: CoreRepositoryInterface,
    private val syncUseCase: SyncUseCase,
) {
    suspend operator fun invoke(
        filterPreferences: FilterPreferences,
        query: String,
        navigatedFromOtherScreen: Boolean,
        shouldRefresh: Boolean
    ): Flow<Resource<List<Champion>>> {

        var finalQuery = query
        if (query == "" && navigatedFromOtherScreen) {
            finalQuery = filterPreferences.query
        }
        val summoner = coreRepo.getMainSummonerLocal()
            ?: return flow { emit(Resource.Error(Throwable("No summoner found"))) }


        return networkBoundResource(
            query = {
                champRepo.getChampions(
                    filterPreferences = filterPreferences,
                    query = finalQuery,
                    id = summoner.id
                )
            },
            shouldFetch = { list ->
                val isFresh = coreRepo.isFreshSummoner(
                    summoner.name,
                    System.currentTimeMillis() - Constants.MILLI_SECONDS_DAY
                ) == 1
                ((list.isEmpty() || !isFresh) || shouldRefresh) && coreRepo.checkInternetConnection()
            },
            sync = {
                syncUseCase()
            },
        )
    }
}
