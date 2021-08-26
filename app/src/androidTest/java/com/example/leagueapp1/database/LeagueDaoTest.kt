package com.example.leagueapp1.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.example.leagueapp1.getOrAwaitValue
import com.example.leagueapp1.home.HomeFragment
import com.example.leagueapp1.launchFragmentInHiltContainer
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
import javax.inject.Named
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
        summoner1 = SummonerProperties(
            "1", "1", "1", "Kate", 2.0, 1.0, 1.0, true, 1, true,
            "Iron", null
        )
        summoner2 = SummonerProperties(
            "2", "2", "2", "Katie", 2.0, 1.0, 1.0, true, 1, true,
            "Iron", null
        )
        summoner3 = SummonerProperties(
            "3", "3", "3", "Katy", 2.0, 1.0, 1.0, true, 1, true,
            "Iron", null
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertSummoner() = runBlockingTest {
        val summoner = SummonerProperties(
            "1234", "5678", "91011", "Kate", 2.0, 1.0, 1.0, true, 1, true,
            "Iron", null
        )

        summonersDao.insertSummoner(summoner)

        val verifySummoner = summonersDao.getSummonerByName("Kate")

        assertThat(verifySummoner).isEqualTo(summoner)
    }

    @Test
    fun updateSummoner() = runBlockingTest {
        val summoner = SummonerProperties(
            "1234", "5678", "91011", "Kate", 2.0, 1.0, 1.0, true, 1, true,
            "Iron", null
        )
        val updatedSummoner = SummonerProperties(
            "1234", "5678", "91011", "Kate", 3.0, 1.0, 100.0, true, 1000, true,
            "Iron", null
        )
        summonersDao.insertSummoner(summoner)
        summonersDao.update(updatedSummoner)
        val verifySummoner = summonersDao.getSummonerByName("Kate")
        assertThat(verifySummoner).isEqualTo(updatedSummoner)
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
        summonersDao.insertSummoner(summoner3)
        val verifySummoner = summonersDao.getSummoner()
        assertThat(verifySummoner).isEqualTo(summoner3)
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

        summoner1 = SummonerProperties(
            "1", "1", "1", "Kate", 2.0, 1.0, 1.0, true, 100, true,
            "Iron", null
        )
        summoner2 = SummonerProperties(
            "2", "2", "2", "Katie", 2.0, 1.0, 1.0, true, 150, true,
            "Iron", null
        )

        summonersDao.insertSummoner(summoner1)
        summonersDao.insertSummoner(summoner2)

        val time: Long = 120
        val time2: Long = 100

        assertThat(summonersDao.isFreshSummoner("Kate", time)).isEqualTo(0)
        assertThat(summonersDao.isFreshSummoner("Kate", time2)).isEqualTo(1)
        assertThat(summonersDao.isFreshSummoner("Katie", time)).isEqualTo(1)
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

        champion1 = ChampionMastery(
            championId = 1,
            championLevel = 1.0,
            championPoints = 200.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "123",
            champName = "Lux",
            timeReceived = 10000,
            rankInfo = null,
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        champion2 = ChampionMastery(
            championId = 2,
            championLevel = 1.0,
            championPoints = 300.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "123",
            champName = "Abs",
            timeReceived = 10000,
            rankInfo = null,
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        champion3 = ChampionMastery(
            championId = 3,
            championLevel = 1.0,
            championPoints = 400.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "123",
            champName = "Darius",
            timeReceived = 10000,
            rankInfo = null,
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        champion4 = ChampionMastery(
            championId = 4,
            championLevel = 1.0,
            championPoints = 500.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "123",
            champName = "Zion",
            timeReceived = 10000,
            rankInfo = null,
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
    }

    @After
    fun teardown() {
        database.close()
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
            listOnlyName.add(item.champName)
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
    fun deleteSummonerChampions() = runBlockingTest {
        val champion5 = ChampionMastery(
            championId = 1,
            championLevel = 1.0,
            championPoints = 200.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "1234",
            champName = "Lux",
            timeReceived = 10000,
            rankInfo = null,
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        championsDao.insertChampion(champion5)
        val list = listOf(champion1, champion2, champion3, champion4)
        championsDao.insertChampionList(list)
        championsDao.deleteSummonerChampions("123")
        val verifyDeletedChamps = championsDao.getChampions(
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
        val verifyOtherSummonerChamps = championsDao.getChampions(
            query = "",
            SortOrder.BY_MASTERY_POINTS,
            "1234",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        assertThat(verifyDeletedChamps).isEmpty()
        assertThat(verifyOtherSummonerChamps).containsExactly(champion5)
    }

    @Test
    fun isFreshSummonerChampions() = runBlockingTest {
        val champion5 = ChampionMastery(
            championId = 1,
            championLevel = 1.0,
            championPoints = 200.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "1234",
            champName = "Lux",
            timeReceived = 10000,
            rankInfo = null,
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        championsDao.insertChampion(champion5)
        val time: Long = 10500
        val time2: Long = 9000
        val time3: Long = 10000
        assertThat(championsDao.isFreshSummonerChampions("1234", time)).isEqualTo(0)
        assertThat(championsDao.isFreshSummonerChampions("1234", time2)).isEqualTo(1)
        assertThat(championsDao.isFreshSummonerChampions("1234", time3)).isEqualTo(1)
    }

    @Test
    fun updateChampionRecentBoost() = runBlockingTest {
        val champion5 = ChampionMastery(
            championId = 1,
            championLevel = 1.0,
            championPoints = 200.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "1234",
            champName = "Lux",
            timeReceived = 10000,
            rankInfo = ChampRankInfo(
                recentBoost = 100,
                experienceBoost = 20,
                lp = 0,
                rank = "Iron"
            ),
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        championsDao.insertChampion(champion5)
        championsDao.updateChampionRecentBoost("1234", 1, 200)
        val verify = championsDao.getChampion(1, "1234")?.rankInfo?.recentBoost
        assertThat(verify).isEqualTo(200)
    }


    @Test
    fun updateChampionExperienceBoost() = runBlockingTest {
        val champion5 = ChampionMastery(
            championId = 1,
            championLevel = 1.0,
            championPoints = 200.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "1234",
            champName = "Lux",
            timeReceived = 10000,
            rankInfo = ChampRankInfo(
                recentBoost = 100,
                experienceBoost = 20,
                lp = 0,
                rank = "Iron"
            ),
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        championsDao.insertChampion(champion5)
        championsDao.updateChampionExperienceBoost("1234", 1, 200)
        val verify = championsDao.getChampion(1, "1234")?.rankInfo?.experienceBoost
        assertThat(verify).isEqualTo(200)
    }

    @Test
    fun updateChampionRank() = runBlockingTest {
        val champion5 = ChampionMastery(
            championId = 1,
            championLevel = 1.0,
            championPoints = 200.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "1234",
            champName = "Lux",
            timeReceived = 10000,
            rankInfo = ChampRankInfo(
                recentBoost = 100,
                experienceBoost = 20,
                lp = 0,
                rank = "Iron"
            ),
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        championsDao.insertChampion(champion5)
        championsDao.updateChampionRank("1234", 1, 20, "Gold")
        val champ = championsDao.getChampion(1, "1234")
        val verifyLp = champ?.rankInfo?.experienceBoost
        val verifyRank = champ?.rankInfo?.rank

        assertThat(verifyLp).isEqualTo(20)
        assertThat(verifyRank).isEqualTo("Gold")
    }

    @Test
    fun getChampion() = runBlockingTest {
        val champion5 = ChampionMastery(
            championId = 1,
            championLevel = 1.0,
            championPoints = 200.0,
            lastPlayTime = 1000.0,
            championPointsSinceLastLevel = 10.0,
            championPointsUntilNextLevel = 100.0,
            chestGranted = false,
            tokensEarned = 10.0,
            summonerId = "1234",
            champName = "Lux",
            timeReceived = 10000,
            rankInfo = ChampRankInfo(
                recentBoost = 100,
                experienceBoost = 20,
                lp = 0,
                rank = "Iron"
            ),
            roles = TrueRoles(
                TOP = false,
                JUNGLE = false,
                MIDDLE = true,
                BOTTOM = false,
                UTILITY = true
            )
        )
        championsDao.insertChampion(champion5)
        assertThat(championsDao.getChampion(1,"1234")).isEqualTo(champion5)
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
    fun insertAndGetList() = runBlockingTest{
        val rates1 = ChampionRoleRates(1, TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false))
        val rates2 = ChampionRoleRates(2, TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false))
        val rates3 = ChampionRoleRates(3, TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false))

        val list = listOf<ChampionRoleRates>(rates1,rates2, rates3)
        championRoleRatesDao.insertList(list)
        val verifyList = championRoleRatesDao.getList().getOrAwaitValue()
        assertThat(verifyList).isEqualTo(list)
    }

    @Test
    fun getChampRole() = runBlockingTest{
        val rates1 = ChampionRoleRates(1, TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false))
        val rates2 = ChampionRoleRates(2, TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false))
        val rates3 = ChampionRoleRates(3, TrueRoles(TOP = false, JUNGLE = false, MIDDLE = true, BOTTOM = false, UTILITY = false))

        val list = listOf<ChampionRoleRates>(rates1,rates2, rates3)
        championRoleRatesDao.insertList(list)
        val verifyRole = championRoleRatesDao.getChampRole(2)
        assertThat(verifyRole).isEqualTo(rates2)
    }
}