package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.example.leagueapp1.feature_champions.util.cleanChampionName
import java.util.*

class FormatSplashNameUseCase(
    private val champRepo: ChampionsRepositoryInterface
) {

    suspend operator fun invoke(id: Int): String {
        val name = champRepo.getChampName(id)
        val filterPair =
            cleanChampionName(name)
        var splashName = filterPair.first
        if (filterPair.second) {
            splashName = splashName.lowercase(Locale.ROOT)
            splashName =
                splashName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
        return when (splashName) {
            "Kogmaw" -> "KogMaw"
            "Reksai" -> "RekSai"
            "Nunuwillump" -> "Nunu"
            "Wukong" -> "MonkeyKing"
            "LeBlanc" -> "Leblanc"
            else -> splashName
        }
    }
}