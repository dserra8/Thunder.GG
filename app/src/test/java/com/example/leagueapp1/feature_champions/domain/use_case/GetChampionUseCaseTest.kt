package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.core.data.repository.CoreRepositoryFake
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.util.createChampion
import com.example.leagueapp1.core.util.createSummoner
import com.example.leagueapp1.feature_champions.data.repository.ChampionsRepositoryFake
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GetChampionUseCaseTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var getChampionUseCase: GetChampionUseCase

    private lateinit var fakeCoreRep: CoreRepositoryInterface
    private lateinit var fakeChampRepo: ChampionsRepositoryInterface


    @Before
    fun setup() {
        fakeChampRepo = ChampionsRepositoryFake()
        fakeCoreRep = CoreRepositoryFake()
        getChampionUseCase = GetChampionUseCase(fakeCoreRep, fakeChampRepo)

        val champList = mutableListOf<Champion>()
        (1..10).forEach {
            champList.add(
                createChampion(
                    championId = it,
                    championPoints = it * 100.0
                )
            )
        }
        champList.add(createChampion(championId = 99, championPoints = 1000000.0, summonerId = "1"))
        champList.add(createChampion(championId = 100, championPoints = 1000000.0, summonerId = "2"))

        champList.shuffle()

        runBlockingTest {
            fakeCoreRep.insertSummonerLocal(createSummoner(id = "0", isMainSummoner = true))
            fakeCoreRep.insertSummonerLocal(createSummoner(id = "1", isMainSummoner = false))
            fakeCoreRep.insertSummonerLocal(createSummoner(id = "2", isMainSummoner = false))
            fakeChampRepo.insertChampions(champList)
        }

    }

    @Test
    fun `Check if champion is returned with Id, returns Kayle`() = runBlockingTest {
        val champ1 = getChampionUseCase(id = 99)
        val champ2 = getChampionUseCase(id = 10)
        Truth.assertThat(champ1).isEqualTo(null)
        if (champ2 != null) {
            Truth.assertThat(champ2.champName).isEqualTo("Kayle ")
        }
    }
}