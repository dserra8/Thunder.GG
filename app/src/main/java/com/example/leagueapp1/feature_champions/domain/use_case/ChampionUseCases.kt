package com.example.leagueapp1.feature_champions.domain.use_case

data class ChampionUseCases(
    val getHighestMasteryChampUseCase: GetHighestMasteryChampUseCase,
    val getChampionsUseCase: GetChampionsUseCase,
    val formatSplashNameUseCase: FormatSplashNameUseCase,
    val getChampionUpdatesUseCase: GetChampionUpdatesUseCase,
    val updateEventListUseCase: UpdateEventListUseCase
)
