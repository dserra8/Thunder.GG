package com.example.leagueapp1.ui.listChamp

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.leagueapp1.AndroidMainCoroutineRule
import com.example.leagueapp1.R
import com.example.leagueapp1.adapters.ChampItem
import com.example.leagueapp1.adapters.ChampionListAdapterNoHeader
import com.example.leagueapp1.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.io.File

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class ListChampFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = AndroidMainCoroutineRule()


    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun cleanup(){
        File(ApplicationProvider.getApplicationContext<Context>().filesDir,
        "datastore"
        ).deleteRecursively()
    }


    @Test
    fun pressRecyclerViewItemWithRank_NavigateToChampDetailsScreen() = runBlockingTest{

//        val navController2 = mock(NavController::class.java)
//        var imageId: Int = 0
//        var rankId: Int = 0
//        launchFragmentInHiltContainer<ListChampFragment> {
//            Navigation.setViewNavController(requireView(), navController2)
//            viewLifecycleOwner.lifecycleScope.launch {
//                viewModel.repository.insertSummoner(createSummonerProperties(current = true, rank = "SILVER"))
//                imageId = resources.getIdentifier("galio", "drawable", activity?.packageName)
//                rankId = resources.getIdentifier("silver", "drawable", activity?.packageName)
//            }
//        }
//
//        onView(withId(R.id.recycler_view)).perform(
//            RecyclerViewActions.actionOnItemAtPosition<ChampionListAdapterNoHeader.ChampionItemViewHolder>(
//                1,
//                click()
//            )
//        )
//
//        val champItemTest = ChampItem(imageId, "Galio ", 3, 400, rankId)
//        verify(navController2).navigate(
//            ListChampFragmentDirections.actionListChampFragmentToChampScreenFragment(champItemTest)
//        )

    }
}