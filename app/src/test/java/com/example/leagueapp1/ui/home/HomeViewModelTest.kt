package com.example.leagueapp1.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.feature_search_summoner.presentation.search_summoner.HomeViewModel
import com.example.leagueapp1.getOrAwaitValueTest
import com.example.leagueapp1.util.TestDispatchers
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class HomeViewModelTest {

//    @get:Rule
//    var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//
//    @get:Rule
//    var mainCoroutineRule = MainCoroutineRule()
//
//    private lateinit var repository: FakeRepository
//    private lateinit var viewModel: HomeViewModel
//
//    @Before
//    fun setup(){
//        repository = FakeRepository(TestDispatchers())
//        viewModel = HomeViewModel(repository)
//    }
//
//    @Test
//    fun `check an unknown summoner name, return false`() {
//        repository.setShouldReturnNetworkError(false)
//        viewModel.changeSummonerName("Cha")
//        viewModel.submitIsClicked()
//        val result = viewModel.summoner.getOrAwaitValueTest()
//        assertThat(result.data).isEqualTo(null)
//        assertThat(result.error?.localizedMessage).isEqualTo("Summoner Not Found")
//
//    }
//
//    @Test
//    fun `search for a summoner with network error, return false `() {
//        repository.setShouldReturnNetworkError(true)
//        viewModel.changeSummonerName("Chasik")
//        viewModel.submitIsClicked()
//
//        val result = viewModel.summoner.getOrAwaitValueTest()
//
//        assertThat(result.data).isEqualTo(null)
//        assertThat(result.error?.localizedMessage).isEqualTo("Summoner Not Found")
//
//    }
//
//    @Test
//    fun `search for a summoner, return true `() {
//        repository.setShouldReturnNetworkError(false)
//        viewModel.changeSummonerName("Chasik")
//        viewModel.submitIsClicked()
//
//        val result = viewModel.summoner.getOrAwaitValueTest()
//
//        assertThat(result.data?.name).isEqualTo("Chasik")
//    }
//
//    @Test
//    fun `search for a summoner already in database with network error, return true `() {
//        repository.setShouldReturnNetworkError(false)
//        viewModel.changeSummonerName("Chasik")
//        viewModel.submitIsClicked()
//
//        repository.setShouldReturnNetworkError(true)
//        viewModel.changeSummonerName("Chasik")
//        viewModel.submitIsClicked()
//
//
//        val result = viewModel.summoner.getOrAwaitValueTest()
//
//        assertThat(result.data?.name).isEqualTo("Chasik")
//    }
}