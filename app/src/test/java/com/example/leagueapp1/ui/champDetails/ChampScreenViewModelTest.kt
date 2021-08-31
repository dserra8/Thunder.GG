package com.example.leagueapp1.ui.champDetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.leagueapp1.MainCoroutineRule
import com.example.leagueapp1.repository.FakeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

@ExperimentalCoroutinesApi
class ChampScreenViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var repository: FakeRepository
    private lateinit var viewModel: ChampScreenViewModel

//    @Before
//    fun setup(){
//        repository = FakeRepository(TestDispatchers())
//        viewModel = HomeViewModel(repository)
//    }
}