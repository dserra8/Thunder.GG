package com.example.leagueapp1.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.example.leagueapp1.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class LeagueDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: LeagueDatabase

    private lateinit var summonersDao: SummonersDao
    private lateinit var championsDao: ChampionsDao
    private lateinit var championRoleRatesDao: ChampionRoleRatesDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LeagueDatabase::class.java
        ).allowMainThreadQueries().build()

        summonersDao = database.summonersDao()
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

    @ExperimentalTime
    @Test
    fun getAllSummoners() = runBlockingTest {
        val summoner1 = SummonerProperties(
            "1", "1", "1", "Kate", 2.0, 1.0, 1.0, true, 1, true,
            "Iron", null
        )
        val summoner2 = SummonerProperties(
            "2", "2", "2", "Katie", 2.0, 1.0, 1.0, true, 1, true,
            "Iron", null
        )
        val summoner3 = SummonerProperties(
            "3", "3", "3", "Katy", 2.0, 1.0, 1.0, true, 1, true,
            "Iron", null
        )

        summonersDao.insertSummoner(summoner1)
        summonersDao.insertSummoner(summoner2)
        summonersDao.insertSummoner(summoner3)

        Duration.inWholeSeconds =
        summonersDao.getAllSummoners().test(timeout = duration) {
            assertEquals(listOf(summoner1, summoner2, summoner3), awaitItem())
            awaitComplete()
        }
    }



}