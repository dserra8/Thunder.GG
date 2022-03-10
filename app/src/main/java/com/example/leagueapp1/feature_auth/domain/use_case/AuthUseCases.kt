package com.example.leagueapp1.feature_auth.domain.use_case

data class AuthUseCases(
    val loginUseCase: LoginUseCase,
    val registerUseCase: RegisterUseCase,
    val changeMainSummonerUseCase: ChangeMainSummonerUseCase
)
