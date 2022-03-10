package com.example.leagueapp1.di

import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.feature_search_summoner.domain.repository.SearchSummonerRepositoryInterface
import com.example.leagueapp1.feature_search_summoner.domain.use_case.GetAllSummonersUseCase
import com.example.leagueapp1.feature_search_summoner.domain.use_case.SearchSummonerUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchSummonerModule {

    @Provides
    @Singleton
    fun provideSearchSummonerUseCases(
        getAllSummonersUseCase: GetAllSummonersUseCase
    ) : SearchSummonerUseCases = SearchSummonerUseCases(getAllSummonersUseCase)

    @Provides
    @Singleton
    fun provideGetAllSummonersUseCase(
        repo: CoreRepositoryInterface
    ) : GetAllSummonersUseCase = GetAllSummonersUseCase(repo)
}