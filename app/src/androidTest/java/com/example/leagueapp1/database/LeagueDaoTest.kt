package com.example.leagueapp1.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import app.cash.turbine.test
import com.example.leagueapp1.core.data.local.LeagueDatabase
import com.example.leagueapp1.core.domain.models.Summoner
import com.example.leagueapp1.data.local.*
import com.example.leagueapp1.core.util.Constants.MILLI_SECONDS_DAY
import com.example.leagueapp1.core.util.createChampion
import com.example.leagueapp1.core.util.createSummoner
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.models.TrueRoles
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

    private lateinit var summoner1: Summoner
    private lateinit var summoner2: Summoner
    private lateinit var summoner3: Summoner


    @Before
    fun setup() {
        hiltRule.inject()
        summonersDao = database.summonersDao()
        summoner1 = createSummoner(
            id = "1",
            name = "Kate",
        )
        summoner2 = createSummoner(
            id = "2",
            name = "Katie",
        )
        summoner3 = createSummoner(
            id = "3",
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
            createSummoner(
                id = "1",
                name = "Kate",
                timeReceived = 1633481485911
            )
        summoner2 =
            createSummoner(
                id = "1",
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
    private lateinit var champion1: Champion
    private lateinit var champion2: Champion
    private lateinit var champion3: Champion
    private lateinit var champion4: Champion
    private lateinit var champion5: Champion
    private lateinit var champion6: Champion
    private lateinit var champion7: Champion
    private lateinit var champion8: Champion
    private lateinit var champion9: Champion
    private lateinit var champList: List<Champion>


    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LeagueDatabase::class.java
        ).allowMainThreadQueries().build()

        championsDao = database.championsDao()

        champion1 = createChampion(
            championId = 1,
            championPoints = 200.0,
            champName = "Annie",
            roles = TrueRoles(middle = true, utility = true)
        )
        champion2 = createChampion(
            championId = 2,
            championPoints = 300.0,
            champName = "Olaf",
            roles = TrueRoles(top = true, jungle = true)
        )
        champion3 = createChampion(
            championId = 3,
            championPoints = 400.0,
            champName = "Darius",
            roles = TrueRoles(top = true)
        )
        champion4 = createChampion(
            championId = 4,
            championPoints = 500.0,
            champName = "Twisted Fate",
            roles = TrueRoles(middle = true)
        )
        champion5 = createChampion(
            championId = 5,
            championPoints = 600.0,
            champName = "Xin Zhao",
            roles = TrueRoles(jungle = true)
        )
        champion6 = createChampion(
            championId = 6,
            championPoints = 700.0,
            champName = "Urgot",
            roles = TrueRoles(top = true)
        )
        champion7 = createChampion(
            championId = 7,
            championPoints = 800.0,
            champName = "Leblanc",
            roles = TrueRoles(middle = true)
        )
        champion8 = createChampion(
            championId = 8,
            championPoints = 900.0,
            champName = "Vladimir",
            roles = TrueRoles(middle = true)
        )
        champion9 = createChampion(
            championId = 9,
            championPoints = 1000.0,
            champName = "Fiddlesticks",
            roles = TrueRoles(utility = true, jungle = true)
        )

        champList = listOf(champion1, champion2, champion3, champion4, champion5, champion6, champion7, champion8, champion9)

        runBlockingTest {
            champList.forEach {
                championsDao.insertChampion(it)
            }
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getChampionsByQueryAndRoles() = runBlockingTest {

        championsDao.insertChampion(
            createChampion(
                championId = 99,
                champName = "Lux",
                roles = TrueRoles(middle = true, utility = true)
            )
        )
        championsDao.insertChampion(
            createChampion(
                championId = 100,
                champName = "Lucian",
                roles = TrueRoles(bottom = true, middle = true)
            )
        )
        val listLMiddle = championsDao.getChampions(
            query = "l",
            sortOrder = SortOrder.BY_NAME,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = true,
            showJungle = false,
            showTop = false,
            showAll = false
        ).first()
        val listLuMiddle = championsDao.getChampions(
            query = "lu",
            sortOrder = SortOrder.BY_NAME,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = true,
            showJungle = false,
            showTop = false,
            showAll = false
        ).first()

        assertThat(listLMiddle.size).isEqualTo(3)
        assertThat(listLuMiddle.size).isEqualTo(2)
    }

    @Test
    fun getChampionsByQuery() = runBlockingTest {
        championsDao.insertChampion(
            createChampion(
                championId = 99,
                champName = "Lux"
            )
        )
        championsDao.insertChampion(
            createChampion(
                championId = 100,
                champName = "Lucian"
            )
        )
        val listL = championsDao.getChampions(
            query = "l",
            sortOrder = SortOrder.BY_NAME,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        val listLu = championsDao.getChampions(
            query = "lu",
            sortOrder = SortOrder.BY_NAME,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        val listD = championsDao.getChampions(
            query = "D",
            sortOrder = SortOrder.BY_NAME,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        val listDaa = championsDao.getChampions(
            query = "Daa",
            sortOrder = SortOrder.BY_NAME,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        assertThat(listD.size).isEqualTo(1)
        assertThat(listDaa .size).isEqualTo(0)
        assertThat(listL.size).isEqualTo(3)
        assertThat(listLu.size).isEqualTo(2)
    }

    @Test
    fun getChampionsByRoles() = runBlockingTest {

        val topChamps = championsDao.getChampions(
            query = "",
            sortOrder = SortOrder.BY_MASTERY_POINTS,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = true,
            showAll = false
        ).first()
        val middleChamps = championsDao.getChampions(
            query = "",
            sortOrder = SortOrder.BY_MASTERY_POINTS,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = true,
            showJungle = false,
            showTop = false,
            showAll = false
        ).first()
        val bottomChamps = championsDao.getChampions(
            query = "",
            sortOrder = SortOrder.BY_MASTERY_POINTS,
            id = "0",
            showADC = true,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = false
        ).first()
        val allChamps = championsDao.getChampions(
            query = "",
            sortOrder = SortOrder.BY_MASTERY_POINTS,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()
        assertThat(topChamps.size).isEqualTo(3)
        assertThat(bottomChamps.size).isEqualTo(0)
        assertThat(middleChamps.size).isEqualTo(4)
        assertThat(allChamps.size).isEqualTo(9)
    }

    @Test
    fun getChampionsOrderedByName() = runBlockingTest {
        val listByName = championsDao.getChampions(
            query = "",
            sortOrder = SortOrder.BY_NAME,
            id = "123",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()

        for(i in 0..listByName.size-2){
            assertThat(listByName[i].champName).isLessThan(listByName[i+1].champName)
        }
    }

    @Test
    fun getChampionsByMastery() = runBlockingTest {
        val listByMastery = championsDao.getChampions(
            query = "",
            sortOrder = SortOrder.BY_MASTERY_POINTS,
            id = "0",
            showADC = false,
            showSup = false,
            showMid = false,
            showJungle = false,
            showTop = false,
            showAll = true
        ).first()

        for(i in 0..listByMastery.size-2){
            assertThat(listByMastery[i].championPoints).isGreaterThan(listByMastery[i+1].championPoints)
        }
    }

    @Test
    fun getHighestMasteryChampion() = runBlockingTest {

        val result = championsDao.getHighestMasteryChampion("0")
        assertThat(result).isEqualTo(champion9)
    }

    @Test
    fun deleteAllChampion() = runBlockingTest {
        championsDao.deleteAllChampions()
        val verify = championsDao.getChampions(
            query = "",
            SortOrder.BY_MASTERY_POINTS,
            "0",
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
        assertThat(championsDao.getChampion(1, "0")).isEqualTo(champion1)
    }
}
