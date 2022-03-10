package com.example.leagueapp1.feature_search_summoner.domain.use_case

import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.domain.models.Summoner
import kotlinx.coroutines.flow.Flow

class GetAllSummonersUseCase(
    private val repo: CoreRepositoryInterface
) {
    operator fun invoke(): Flow<List<Summoner>> = repo.getAllSummoners()
}