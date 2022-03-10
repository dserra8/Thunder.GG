package com.example.leagueapp1.core.domain.use_case

import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.core.data.repository.CoreRepositoryFake
import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.util.createChampion
import com.example.leagueapp1.feature_champions.data.repository.ChampionsRepositoryFake
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.example.leagueapp1.util.createKtorSummoner
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class InsertSummonerAndChampionsUseCaseTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var insertSummonerAndChampionsUseCase: InsertSummonerAndChampionsUseCase

    private lateinit var fakeCoreRep: CoreRepositoryInterface

    private lateinit var fakeChampRepo: ChampionsRepositoryInterface

    private lateinit var ktor: SummonerFromKtor


    @Before
    fun setup() {
        fakeChampRepo = ChampionsRepositoryFake()
        fakeCoreRep = CoreRepositoryFake()
        insertSummonerAndChampionsUseCase =
            InsertSummonerAndChampionsUseCase(fakeCoreRep, fakeChampRepo)

        val champList = mutableListOf<Champion>()
        (1..10).forEach {
            champList.add(
                createChampion(
                    championId = it,
                    championPoints = it * 100.0
                )
            )
        }

        ktor = createKtorSummoner(id = "0", championList = champList)

    }

    @Test
    fun `Pass Ktor Summoner as parameter, returns Success`() = runBlockingTest {

        val result = insertSummonerAndChampionsUseCase(ktor)
        assertThat(result).isEqualTo(Result.success(Unit))

        val summoner = fakeCoreRep.getMainSummonerLocal()
        val champList = summoner?.let { fakeChampRepo.getAllChamps(it.id) }

        assertThat(summoner).isNotEqualTo(null)

        if (summoner != null) {
            assertThat(summoner.id).isEqualTo(ktor.id)
            assertThat(champList).isEqualTo(ktor.championList)
        }
    }

    @Test
    fun `Get Ktor Summonor from Ktor, returns Success`() = runBlockingTest {

        fakeCoreRep.insertSummonerRemote(ktor)
        val result = insertSummonerAndChampionsUseCase(null)
        assertThat(result).isEqualTo(Result.success(Unit))

        val summoner = fakeCoreRep.getMainSummonerLocal()
        val champList = summoner?.let { fakeChampRepo.getAllChamps(it.id) }

        assertThat(summoner).isNotEqualTo(null)

        if (summoner != null) {
            assertThat(summoner.id).isEqualTo(ktor.id)
            assertThat(champList).isEqualTo(ktor.championList)
        }
    }

    @Test
    fun `No Local Summoner and Network Error, returns Throwable()`() = runBlockingTest {

        val result = insertSummonerAndChampionsUseCase(null)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Main Summoner is null")
    }
}