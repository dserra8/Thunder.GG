package com.example.leagueapp1.ui.home

import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.leagueapp1.R
import com.example.leagueapp1.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*


@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class HomeFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup(){
        hiltRule.inject()
    }

    @Test
    fun clickOnSubmit_navigateToListChampFragment() {
        val navController = mock(NavController::class.java)
       // val navController = mockk<NavController>()
        launchFragmentInHiltContainer<HomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }


        onView(withId(R.id.summonerNameTextView)).perform(replaceText("Chasik"))
        onView(withId(R.id.submitButton)).perform(click())


        verify(navController, after(2000)).navigate(
            HomeFragmentDirections.actionHomeFragmentToListChampFragment()
        )
    }
}