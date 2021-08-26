package com.example.leagueapp1.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.getOrAwaitValueTest
import com.example.leagueapp1.repository.FakeRepository
import com.example.leagueapp1.util.Resource
import com.example.leagueapp1.util.TestDispatchers
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
    private lateinit var repository: FakeRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup(){
        repository = FakeRepository(TestDispatchers())
        viewModel = HomeViewModel(repository)
    }

    @Test
    fun `check an unknown summoner name, return false`() {
        repository.setShouldReturnNetworkError(false)
        viewModel.changeSummonerName("Cha")
        viewModel.submitIsClicked()
        val result = viewModel.summonerProperties.getOrAwaitValueTest()
        assertThat(result.data).isEqualTo(null)
        assertThat(result.error?.localizedMessage).isEqualTo("Summoner Not Found")

    }

    @Test
    fun `search for a summoner with network error, return false `() {
        repository.setShouldReturnNetworkError(true)
        viewModel.changeSummonerName("Chasik")
        viewModel.submitIsClicked()

        val result = viewModel.summonerProperties.getOrAwaitValueTest()

        assertThat(result.data).isEqualTo(null)
        assertThat(result.error?.localizedMessage).isEqualTo("Summoner Not Found")

    }

    @Test
    fun `search for a summoner, return true `() {
        repository.setShouldReturnNetworkError(false)
        viewModel.changeSummonerName("Chasik")
        viewModel.submitIsClicked()

        val result = viewModel.summonerProperties.getOrAwaitValueTest()

        assertThat(result.data?.name).isEqualTo("Chasik")
    }

    @Test
    fun `search for a summoner already in database with network error, return true `() {
        repository.setShouldReturnNetworkError(false)
        viewModel.changeSummonerName("Chasik")
        viewModel.submitIsClicked()

        repository.setShouldReturnNetworkError(true)
        viewModel.changeSummonerName("Chasik")
        viewModel.submitIsClicked()


        val result = viewModel.summonerProperties.getOrAwaitValueTest()

        assertThat(result.data?.name).isEqualTo("Chasik")
    }

    @Test
    fun `check for empty role list with network error, return false`() {
        repository.setShouldReturnNetworkError(true)
        assertThat(viewModel.roleList.value).isEmpty()
        viewModel.checkRoleList(viewModel.roleList.value)
        val result = viewModel.roleList.getOrAwaitValueTest()
        assertThat(result).isEmpty()
    }

    @Test
    fun `check for empty role list, create new role list,return true`() {
        repository.setShouldReturnNetworkError(false)
        assertThat(viewModel.roleList.value).isEmpty()
        viewModel.checkRoleList(viewModel.roleList.value)
        val result = viewModel.roleList.getOrAwaitValueTest()
        assertThat(result).isNotEmpty()
    }

    @Test
    fun `check for an existing role list, does not fetch a new list, return true`() {
        repository.setShouldReturnNetworkError(false)
        assertThat(viewModel.roleList.value).isEmpty()
        viewModel.checkRoleList(viewModel.roleList.value)
        repository.refreshChampionRatesCalled = false
        viewModel.checkRoleList(viewModel.roleList.value)
        val result = viewModel.roleList.getOrAwaitValueTest()
        assertThat(result).isNotEmpty()
        assertThat(repository.refreshChampionRatesCalled).isFalse()
    }

    @Test
    fun `check for an existing role list with network error, does not fetch a new list, return true`() {
        repository.setShouldReturnNetworkError(false)
        assertThat(viewModel.roleList.value).isEmpty()
        viewModel.checkRoleList(viewModel.roleList.value)
        repository.refreshChampionRatesCalled = false
        repository.setShouldReturnNetworkError(true)
        viewModel.checkRoleList(viewModel.roleList.value)
        val result = viewModel.roleList.getOrAwaitValueTest()
        assertThat(result).isNotEmpty()
        assertThat(repository.refreshChampionRatesCalled).isFalse()
    }
}