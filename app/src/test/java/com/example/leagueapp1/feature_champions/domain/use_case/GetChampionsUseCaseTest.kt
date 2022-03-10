package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.core.data.repository.CoreRepositoryFake
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.domain.use_case.InsertSummonerAndChampionsUseCase
import com.example.leagueapp1.core.domain.use_case.SyncUseCase
import com.example.leagueapp1.core.util.createChampion
import com.example.leagueapp1.core.util.createSummoner
import com.example.leagueapp1.data.local.FilterPreferences
import com.example.leagueapp1.data.local.SortOrder
import com.example.leagueapp1.feature_champions.data.repository.ChampionsRepositoryFake
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class GetChampionsUseCaseTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var getChampionsUseCase: GetChampionsUseCase

    private lateinit var syncUseCase: SyncUseCase

    private lateinit var insertSummonerAndChampionsUseCase: InsertSummonerAndChampionsUseCase

    private lateinit var fakeCoreRep: CoreRepositoryInterface
    private lateinit var fakeChampRepo: ChampionsRepositoryInterface


    @Before
    fun setup() {
        fakeChampRepo = ChampionsRepositoryFake()
        fakeCoreRep = CoreRepositoryFake()
        insertSummonerAndChampionsUseCase =
            InsertSummonerAndChampionsUseCase(fakeCoreRep, fakeChampRepo)
        syncUseCase = SyncUseCase(fakeCoreRep, fakeChampRepo, insertSummonerAndChampionsUseCase)
        getChampionsUseCase = GetChampionsUseCase(fakeChampRepo, fakeCoreRep, syncUseCase)

        val champList = mutableListOf<Champion>()
        (1..45).forEach {
            champList.add(
                createChampion(
                    championId = it,
                    championPoints = it * 100.0
                )
            )
        }
        champList.add(createChampion(championId = 99, championPoints = 1000000.0, summonerId = "1"))
        champList.add(
            createChampion(
                championId = 100,
                championPoints = 1000000.0,
                summonerId = "2"
            )
        )

        champList.shuffle()

        runBlockingTest {
            fakeCoreRep.insertSummonerLocal(createSummoner(id = "0", name = "kalie", isMainSummoner = true, timeReceived = System.currentTimeMillis()))
            fakeCoreRep.insertSummonerLocal(createSummoner(id = "1", name = "yo" ,isMainSummoner = false))
            fakeCoreRep.insertSummonerLocal(createSummoner(id = "2", name = "luffy", isMainSummoner = false))
            fakeChampRepo.insertChampions(champList)
        }

    }

    @Test
    fun `Not navigated from other screen, no query, returns correct list`() =
        runBlockingTest {
            val preferences = FilterPreferences(
                sortOrder = SortOrder.BY_MASTERY_POINTS
            )
            val flow = getChampionsUseCase(
                filterPreferences = preferences,
                query = "",
                navigatedFromOtherScreen = false
            ).first()

            val list = flow.data!!

            for(i in 0..list.size-2) {
                assertThat(list[i].championPoints).isGreaterThan(list[i+1].championPoints)
            }
        }

    @Test
    fun `Not navigated from other screen, current query, returns correct list`() =
        runBlockingTest {
            val query = "D"
            val preferences = FilterPreferences(
                sortOrder = SortOrder.BY_MASTERY_POINTS
            )
            val flow = getChampionsUseCase(
                filterPreferences = preferences,
                query = query,
                navigatedFromOtherScreen = false
            ).first()

            val list = flow.data!!

            for(i in list.indices) {
                assertThat(list[i].champName).startsWith(query)
            }
        }

    @Test
    fun `Navigated from other screen, previous query, returns correct list`() =
        runBlockingTest {
            val query = "L"
            val preferences = FilterPreferences(
                sortOrder = SortOrder.BY_MASTERY_POINTS,
                query = query
            )
            val flow = getChampionsUseCase(
                filterPreferences = preferences,
                query = "",
                navigatedFromOtherScreen = true
            ).first()

            val list = flow.data!!

            for(i in list.indices) {
                assertThat(list[i].champName).startsWith(query)
            }
        }

    @Test
    fun `Summoner is fresh, no call to sync`() =
        runBlockingTest {
            val query = ""
            val preferences = FilterPreferences(
                sortOrder = SortOrder.BY_MASTERY_POINTS,
            )
            val flow = getChampionsUseCase(
                filterPreferences = preferences,
                query = "",
                navigatedFromOtherScreen = true
            ).collectLatest {  }

            val ktorSummoner = fakeCoreRep.getMainSummonerRemote()?.data

            assertThat(ktorSummoner).isEqualTo(null)
        }

    @Test
    fun `Summoner is not fresh, call sync`() =
        runBlockingTest {
            val updatedSummoner = createSummoner(id = "0", name = "kalie", isMainSummoner = true, timeReceived = 1)
            fakeCoreRep.insertSummonerLocal(updatedSummoner)

            val preferences = FilterPreferences(
                sortOrder = SortOrder.BY_MASTERY_POINTS,
            )
            val flow = getChampionsUseCase(
                filterPreferences = preferences,
                query = "",
                navigatedFromOtherScreen = false
            ).collectLatest {

            }

            val ktorSummoner = fakeCoreRep.getMainSummonerRemote()?.data

            assertThat(ktorSummoner).isNotEqualTo(null)
        }
}