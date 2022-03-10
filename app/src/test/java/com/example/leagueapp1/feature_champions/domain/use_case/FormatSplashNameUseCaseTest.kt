package com.example.leagueapp1.feature_champions.domain.use_case

import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.feature_champions.data.repository.ChampionsRepositoryFake
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FormatSplashNameUseCaseTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var formatSplashNameUseCase: FormatSplashNameUseCase

    private lateinit var fakeChampRepo: ChampionsRepositoryInterface


    @Before
    fun setup() {
        fakeChampRepo = ChampionsRepositoryFake()

        formatSplashNameUseCase = FormatSplashNameUseCase(fakeChampRepo)
    }


    @Test
    fun `No weird syntax, returns correct name`() = runBlockingTest {
        assertThat(formatSplashNameUseCase(1)).isEqualTo("Annie")
        assertThat(formatSplashNameUseCase(875)).isEqualTo("Sett")
        assertThat(formatSplashNameUseCase(516)).isEqualTo("Ornn")
    }

    @Test
    fun `Weird syntax, returns correct name`() = runBlockingTest{
        assertThat(formatSplashNameUseCase(4)).isEqualTo("TwistedFate")
        assertThat(formatSplashNameUseCase(5)).isEqualTo("XinZhao")
        assertThat(formatSplashNameUseCase(11)).isEqualTo("MasterYi")
        assertThat(formatSplashNameUseCase(21)).isEqualTo("MissFortune")
        assertThat(formatSplashNameUseCase(31)).isEqualTo("Chogath")
        assertThat(formatSplashNameUseCase(36)).isEqualTo("DrMundo")
        assertThat(formatSplashNameUseCase(59)).isEqualTo("JarvanIV")
        assertThat(formatSplashNameUseCase(64)).isEqualTo("LeeSin")
        assertThat(formatSplashNameUseCase(121)).isEqualTo("Khazix")
        assertThat(formatSplashNameUseCase(136)).isEqualTo("AurelionSol")
        assertThat(formatSplashNameUseCase(145)).isEqualTo("Kaisa")
        assertThat(formatSplashNameUseCase(161)).isEqualTo("Velkoz")
        assertThat(formatSplashNameUseCase(223)).isEqualTo("TahmKench")
    }

    @Test
    fun `Outliers, name does not follow a pattern, returns correct name`() = runBlockingTest {

        assertThat(formatSplashNameUseCase(20)).isEqualTo("Nunu")
        assertThat(formatSplashNameUseCase(96)).isEqualTo("KogMaw")
        assertThat(formatSplashNameUseCase(421)).isEqualTo("RekSai")
        assertThat(formatSplashNameUseCase(62)).isEqualTo("MonkeyKing")
        assertThat(formatSplashNameUseCase(7)).isEqualTo("Leblanc")

    }
}