package com.example.leagueapp1.core.domain.use_case

import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.core.data.repository.CoreRepositoryFake
import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.util.createSummoner
import com.example.leagueapp1.core.domain.models.Summoner
import com.example.leagueapp1.feature_champions.data.repository.ChampionsRepositoryFake
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.example.leagueapp1.util.createKtorSummoner
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SyncUseCaseTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var insertSummonerAndChampionsUseCase: InsertSummonerAndChampionsUseCase

    private lateinit var syncUseCase: SyncUseCase

    private lateinit var fakeCoreRep: CoreRepositoryInterface

    private lateinit var fakeChampRepo: ChampionsRepositoryInterface

    private lateinit var ktor: SummonerFromKtor

    private lateinit var localSum: Summoner


    @Before
    fun setup() {
        fakeChampRepo = ChampionsRepositoryFake()
        fakeCoreRep = CoreRepositoryFake()
        insertSummonerAndChampionsUseCase =
            InsertSummonerAndChampionsUseCase(fakeCoreRep, fakeChampRepo)
        syncUseCase = SyncUseCase(fakeCoreRep, fakeChampRepo, insertSummonerAndChampionsUseCase)

        ktor = createKtorSummoner(id = "0")
        localSum = createSummoner(id = "0", isMainSummoner = true)

    }

    @Test
    fun `Local Summoner exists, returns Success`() = runBlockingTest {
        fakeCoreRep.insertSummonerLocal(localSum)
        val result = syncUseCase()
        assertThat(result).isEqualTo(Result.success(Unit))
    }

    @Test
    fun `No local summoner and no remote summoner, returns Failure`() = runBlockingTest {
        val result = syncUseCase()
        assertThat(result).isNotEqualTo(Result.success(Unit))
    }

    @Test
    fun `No local summoner and remote summoner exists, returns Success`() = runBlockingTest {
        fakeCoreRep.insertSummonerRemote(ktor)
        val result = syncUseCase()
        assertThat(result).isEqualTo(Result.success(Unit))
    }


}