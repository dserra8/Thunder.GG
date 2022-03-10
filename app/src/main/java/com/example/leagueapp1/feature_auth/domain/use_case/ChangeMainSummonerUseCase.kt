package com.example.leagueapp1.feature_auth.domain.use_case

import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.feature_auth.domain.repository.AuthRepositoryInterface
import kotlinx.coroutines.flow.first

class ChangeMainSummonerUseCase(
    private val coreRepository: CoreRepositoryInterface
) {
    suspend operator fun invoke(puuid: String) : Result<Unit> {
        val summonerList = coreRepository.getAllSummoners().first()
        summonerList.forEach {
            if (puuid != it.puuid) {
                coreRepository.insertSummonerLocal(
                    it.apply { isMainSummoner = false }
                )
            } else {
                coreRepository.insertSummonerLocal(
                    it.apply { isMainSummoner = true }
                )
            }
        }
        return Result.success(Unit)
    }
}