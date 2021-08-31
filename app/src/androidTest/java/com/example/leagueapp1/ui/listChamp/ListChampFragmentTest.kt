package com.example.leagueapp1.ui.listChamp

import android.os.SystemClock
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.leagueapp1.R
import com.example.leagueapp1.adapters.ChampItem
import com.example.leagueapp1.adapters.ChampionListAdapterNoHeader
import com.example.leagueapp1.launchFragmentInHiltContainer
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.createSummonerProperties
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class   ListChampFragmentTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

//    @get:Rule
//    var mainCoroutineRule = AndroidMainCoroutineRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun pressRecyclerViewItem_NavigateToIntroScreen() {

        val navController = mock(NavController::class.java)
        var imageId: Int = 0
        var rankId: Int = 0
        launchFragmentInHiltContainer<ListChampFragment> {
            Navigation.setViewNavController(requireView(), navController)
            imageId = resources.getIdentifier("twistedfate", "drawable", activity?.packageName)
        }

        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ChampionListAdapterNoHeader.ChampionItemViewHolder>(
                0,
                click()
            )
        )

        val champItemTest = ChampItem(imageId, "Twisted Fate ", 4, 500, rankId)
        verify(navController).navigate(
            ListChampFragmentDirections.actionListChampFragmentToIntroChampFragment(champItemTest)
        )

    }

    @Test
    fun pressRecyclerViewItemWithRank_NavigateToChampDetailsScreen() {

        val navController2 = mock(NavController::class.java)
        var imageId: Int = 0
        var rankId: Int = 0
        launchFragmentInHiltContainer<ListChampFragment> {
            Navigation.setViewNavController(requireView(), navController2)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.repository.insertSummoner(createSummonerProperties(current = true, rank = "SILVER"))
                imageId = resources.getIdentifier("galio", "drawable", activity?.packageName)
                rankId = resources.getIdentifier("silver", "drawable", activity?.packageName)
            }
        }

        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ChampionListAdapterNoHeader.ChampionItemViewHolder>(
                1,
                click()
            )
        )

        val champItemTest = ChampItem(imageId, "Galio ", 3, 400, rankId)
        verify(navController2).navigate(
            ListChampFragmentDirections.actionListChampFragmentToChampScreenFragment(champItemTest)
        )

    }
}