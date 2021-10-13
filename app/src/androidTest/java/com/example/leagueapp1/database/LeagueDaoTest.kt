package com.example.leagueapp1.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.example.leagueapp1.data.local.*
import com.example.leagueapp1.getOrAwaitValue
import com.example.leagueapp1.util.Constants.MILLI_SECONDS_DAY
import com.example.leagueapp1.util.createChampionMastery
import com.example.leagueapp1.util.createSummonerPropertiesTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class SummonerDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: LeagueDatabase

    private lateinit var summonersDao: SummonersDao

    private lateinit var summoner1: SummonerProperties
    private lateinit var summoner2: SummonerProperties
    private lateinit var summoner3: SummonerProperties


    @Before
    fun setup() {
        hiltRule.inject()
        summonersDao = database.summonersDao()
        summoner1 = createSummonerPropertiesTest(
            id = "1",
            accountId = "1",
            puuid = "1",
            name = "Kate",
        )
        summoner2 = createSummonerPropertiesTest(
            id = "2",
            accountId = "2",
            puuid = "2",
            name = "Katie",
        )
        summoner3 = createSummonerPropertiesTest(
            id = "3",
            accountId = "3",
            puuid = "3",
            name = "Katy",
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertSummoner() = runBlockingTest {
        summonersDao.insertSummoner(summoner1)
        val verifySummoner = summonersDao.getSummonerByName("Kate")
        assertThat(verifySummoner).isEqualTo(summoner1)
    }

    @Test
    fun getAllSummoners() = runBlockingTest {

        summonersDao.insertSummoner(summoner1)
        summonersDao.insertSummoner(summoner2)
        summonersDao.insertSummoner(summoner3)
        summonersDao.getAllSummoners().test() {
            assertEquals(listOf(summoner1, summoner2, summoner3), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSummonerFlow() = runBlockingTest {
        summonersDao.insertSummoner(summoner3)
        summonersDao.getSummonerFlow().test() {
            assertThat(summoner3).isEqualTo(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSummoner() = runBlockingTest {
        summonersDao.insertSummoner(summoner3.apply { isMainSummoner = true })
        summonersDao.insertSummoner(summoner1)
        val verifySummonerFound = summonersDao.getSummoner()

        assertThat(verifySummonerFound).isEqualTo(summoner3)
        assertThat(verifySummonerFound).isNotEqualTo(summoner1)
    }

    @Test
    fun deleteAllSummoners() = runBlockingTest {
        summonersDao.insertSummoner(summoner1)
        summonersDao.insertSummoner(summoner2)
        summonersDao.insertSummoner(summoner3)
        summonersDao.deleteAllSummoners()
        val list = summonersDao.getAllSummoners().first()
        assertThat(list).isEmpty()
    }


    @Test
    fun isFreshSummoner() = runBlockingTest {
        summoner1 =
            createSummonerPropertiesTest(
                id = "1",
                accountId = "1",
                puuid = "1",
                name = "Kate",
                timeReceived = 1633481485911
            )
        summoner2 =
            createSummonerPropertiesTest(
                id = "1",
                accountId = "1",
                puuid = "1",
                name = "Katie",
                timeReceived = 9
            )

        summonersDao.insertSummoner(summoner1)
        summonersDao.insertSummoner(summoner2)

        val currentTime = System.currentTimeMillis()
        val time2 = 1633485187381

        assertThat(summonersDao.isFreshSummoner("Kate", currentTime - MILLI_SECONDS_DAY)).isEqualTo(0)
        assertThat(summonersDao.isFreshSummoner("Kate", time2 - MILLI_SECONDS_DAY)).isEqualTo(0)
    //    assertThat(summonersDao.isFreshSummoner("Katie", time)).isEqualTo(1)
    }
}


@ExperimentalTime
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ChampionDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: LeagueDatabase
    private lateinit var championsDao: ChampionsDao
    private lateinit var champion1: ChampionMastery
    private lateinit var champion2: ChampionMastery
    private lateinit var champion3: ChampionMastery
    private lateinit var champion4: ChampionMastery


    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LeagueDatabase::class.java
        ).allowMainThreadQueries().build()

        championsDao = database.championsDao()

        champion1 = createChampionMastery(
            championId = 1,
            championPoints = 200.0,
            summonerId = "123",
            champName = "Lux",
        )
        champion2 = createChampionMastery(
            championId = 2,
            championLevel = 1.0,
            championPoints = 300.0,
            summonerId = "123",
            champName = "Abs",
        )
        champion3 = createChampionMastery(
            championId = 3,
            championPoints = 400.0,
            summonerId = "123",
            champName = "Darius",
        )
        champion4 = createChampionMastery(
            championId = 4,
            championPoints = 500.0,
            summonerId = "123",
            champName = "Zion",
        )
    }

    @After
    fun teardown() {
        database.close()
    }


    @Test
    fun getChampions() = runBlockingTest {
        championsDao.insertChampion(champion1)
        championsDao.insertChampion(champion2)
        championsDao.insertChampion(champion3)
        championsDao.insertChampion(champion4)
        val listByName = championsDao.getChampions(
            query = "",
            sortOrder = SortOrder.BY_NAME,
            id = "123",
            false,
            false,
            false,
            false,
            false,
            true
        ).first()
        val listByMastery = championsDao.getChampions(
            query = "",
            sortOrder = SortOrder.BY_MASTERY_POINTS,
            id = "123",
            false,
            false,
            false,
            false,
            false,
            true
        ).first()
        val listOnlyName = mutableListOf<String>()
        for (item in listByName) {
            listOnlyName.add(item.champName!!)
        }
        assertThat(listOnlyName).isInOrder()

        val listOnlyMastery = mutableListOf<Double>()
        for (item in listByMastery) {
            listOnlyMastery.add(item.championPoints)
        }
        assertThat(listOnlyMastery.reversed()).isInOrder()

    }

    @Test
    fun getChampionsByName() = runBlockingTest {
        championsDao.insertChampion(champion1)
        championsDao.insertChampion(champion2)
        championsDao.insertChampion(champion3)
        championsDao.insertChampion(champion4)

        val list =
            championsDao.getChampionsByName("123", "", false, false, false, false, false, true)
                .first()
        val listOnlyName = mutableListOf<String>()
        for (item in list) {
            listOnlyName.add(item.champName!!)
        }
        assertThat(listOnlyName).isInOrder()
    }

    @Test
    fun getChampionsByMastery() = runBlockingTest {
        championsDao.insertChampion(champion1)
        championsDao.insertChampion(champion2)
        championsDao.insertChampion(champion3)
        championsDao.insertChampion(champion4)

        val list = championsDao.getChampionsByMasteryPoints(
            "123",
            "",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        val listOnlyPoints = mutableListOf<Double>()
        for (item in list) {
            listOnlyPoints.add(item.championPoints)
        }

        assertThat(listOnlyPoints.asReversed()).isInStrictOrder()
    }

    @Test
    fun getHighestMasteryChampion() = runBlockingTest {
        championsDao.insertChampion(champion1)
        championsDao.insertChampion(champion2)
        championsDao.insertChampion(champion3)
        championsDao.insertChampion(champion4)

        val result = championsDao.getHighestMasteryChampion("123")

        assertThat(result).isEqualTo(champion4)
    }

    @Test
    fun insertChampion() = runBlockingTest {
        championsDao.insertChampion(champion1)
        val verify = championsDao.getChampion(champion1.championId, champion1.summonerId)
        val champ2 = createChampionMastery(
            champName = "",
        )
        assertThat(verify).isEqualTo(champion1)
    }

    @Test
    fun insertChampionList() = runBlockingTest {
        val list = listOf(champion1, champion2, champion3, champion4)
        championsDao.insertChampionList(list)
        val verify = championsDao.getChampions(
            query = "",
            SortOrder.BY_MASTERY_POINTS,
            "123",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        assertThat(verify).containsExactlyElementsIn(list)
    }

    @Test
    fun deleteAllChampion() = runBlockingTest {
        val list = listOf(champion1, champion2, champion3, champion4)
        championsDao.insertChampionList(list)
        championsDao.deleteAllChampions()
        val verify = championsDao.getChampions(
            query = "",
            SortOrder.BY_MASTERY_POINTS,
            "123",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        assertThat(verify).isEmpty()
    }

    @Test
    fun getChampion() = runBlockingTest {
        val champion5 = createChampionMastery(
            championId = 1,
            summonerId = "1234",
            champName = "Lux"
        )
        championsDao.insertChampion(champion5)
        assertThat(championsDao.getChampion(1, "1234")).isEqualTo(champion5)
    }
}

@ExperimentalTime
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ChampionRoleRatesTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: LeagueDatabase
    private lateinit var championRoleRatesDao: ChampionRoleRatesDao


    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LeagueDatabase::class.java
        ).allowMainThreadQueries().build()
        championRoleRatesDao = database.championRoleRatesDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetList() = runBlockingTest {
        val rates1 = ChampionRoleRates(
            1,
            TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false)
        )
        val rates2 = ChampionRoleRates(
            2,
            TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false)
        )
        val rates3 = ChampionRoleRates(
            3,
            TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false)
        )

        val list = listOf<ChampionRoleRates>(rates1, rates2, rates3)
        championRoleRatesDao.insertList(list)
        val verifyList = championRoleRatesDao.getList().getOrAwaitValue()
        assertThat(verifyList).isEqualTo(list)
    }

    @Test
    fun getChampRole() = runBlockingTest {
        val rates1 = ChampionRoleRates(
            1,
            TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false)
        )
        val rates2 = ChampionRoleRates(
            2,
            TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false)
        )
        val rates3 = ChampionRoleRates(
            3,
            TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false)
        )

        val list = listOf<ChampionRoleRates>(rates1, rates2, rates3)
        championRoleRatesDao.insertList(list)
        val verifyRole = championRoleRatesDao.getChampRole(2)
        assertThat(verifyRole).isEqualTo(rates2)
    }
}