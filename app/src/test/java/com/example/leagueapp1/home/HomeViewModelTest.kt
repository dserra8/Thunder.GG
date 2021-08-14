package com.example.leagueapp1.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.getOrAwaitValueTest
import com.example.leagueapp1.repository.FakeRepository
import com.example.leagueapp1.util.Resource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup(){
        viewModel = HomeViewModel(FakeRepository())
    }

    @Test
    fun `check an unknown summoner name, return false`() {
        viewModel.changeSummonerName("Chasik")
        viewModel.submitIsClicked()

        val result = viewModel.summonerProperties.getOrAwaitValueTest()

        assertThat(result.data).isEqualTo(null)
        assertThat(result.error?.localizedMessage).isEqualTo("Summoner Not Found")
        
    }


}