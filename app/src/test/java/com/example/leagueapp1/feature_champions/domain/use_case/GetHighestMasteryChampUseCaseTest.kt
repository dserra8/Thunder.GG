package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.core.data.repository.CoreRepositoryFake
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.util.createChampion
import com.example.leagueapp1.core.util.createSummoner
import com.example.leagueapp1.feature_champions.data.repository.ChampionsRepositoryFake
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi

class GetHighestMasteryChampUseCaseTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var getHighestMasteryChampUseCase: GetHighestMasteryChampUseCase

    private lateinit var fakeCoreRep: CoreRepositoryInterface
    private lateinit var fakeChampRepo: ChampionsRepositoryInterface


    @Before
    fun setup() {
        fakeChampRepo = ChampionsRepositoryFake()
        fakeCoreRep = CoreRepositoryFake()
        getHighestMasteryChampUseCase = GetHighestMasteryChampUseCase(fakeChampRepo, fakeCoreRep)

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
        champList.shuffle()

        runBlockingTest {
            fakeCoreRep.insertSummonerLocal(createSummoner(id = "0", isMainSummoner = true))
            fakeCoreRep.insertSummonerLocal(createSummoner(id = "1", isMainSummoner = false))
            fakeChampRepo.insertChampions(champList)
        }

    }

    @Test
    fun `check if highest mastery champion is returned, return Kayle, id=10`() = runBlockingTest {
        val champ = getHighestMasteryChampUseCase()
        assertThat(champ).isNotEqualTo(null)
        if (champ != null) {
            assertThat(champ.champName).isEqualTo("Kayle ")
        }
    }
}