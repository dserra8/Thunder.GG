package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.core.domain.models.update.UpdateChampsRanks
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface

class GetChampionUpdatesUseCase(
    private val champRepo: ChampionsRepositoryInterface,
) {
    suspend operator fun invoke(): Result<Boolean> {

        lateinit var update: UpdateChampsRanks

        return when (val updateResult = champRepo.updateChampsRanks()) {
            is NoLoadResource.Error -> {
                Result.failure(updateResult.error ?: Exception("Network Error"))
            }
            is NoLoadResource.Success -> {
                update = updateResult.data!!
                if(update.newUpdate) Result.success(true)
                else Result.success(false)
            }
            null -> {
                Result.failure(Exception("Unknown Error"))
            }
        }
    }

}