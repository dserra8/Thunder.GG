package com.example.leagueapp1.core.domain.use_case

data class CoreUseCases(
    val insertSummonerAndChampionsUseCase: InsertSummonerAndChampionsUseCase,
    val syncUseCase: SyncUseCase,
    val getHeaderInfoUseCase: GetHeaderInfoUseCase
)
