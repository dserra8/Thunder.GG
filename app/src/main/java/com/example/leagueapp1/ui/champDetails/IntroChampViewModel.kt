package com.example.leagueapp1.ui.champDetails

import androidx.lifecycle.*
import com.example.leagueapp1.R
import com.example.leagueapp1.adapters.ChampItem
import com.example.leagueapp1.data.remote.MatchDetails
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.DispatcherProvider
import com.example.leagueapp1.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class IntroChampViewModel @Inject constructor(
    val repository: LeagueRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel(){

  //  val summonerFlow = repository.summoner.asLiveData()

    private val introChampEventsChannel = Channel<IntroChampEvents>()
    val introChampEvents = introChampEventsChannel.receiveAsFlow()

    //Live Data for LP text
    private val _lpText = MutableLiveData<Int>()
    val lpText: LiveData<Int> = _lpText

    private val lpSeparation = 100

    private val requiredAmountOfGames = 3

    private var transitioned = false



    suspend fun updateChampionRank(lp: Int, rankKey: Int, champId: Int) {

    }

    fun updateLpText(value: String) {
        _lpText.value = value.toInt()
    }



    fun chooseRankImage(value: Int): Int {
        return when (calculateRank(value * lpSeparation)) {
            Constants.Ranks.IRON -> R.drawable.emblem_iron
            Constants.Ranks.BRONZE -> R.drawable.bronze
            Constants.Ranks.SILVER -> R.drawable.silver
            Constants.Ranks.GOLD -> R.drawable.gold
            Constants.Ranks.PLATINUM -> R.drawable.platinum
            Constants.Ranks.DIAMOND -> R.drawable.diamond
            Constants.Ranks.MASTER -> R.drawable.master
            Constants.Ranks.GRANDMASTER -> R.drawable.grandmaster
            else -> R.drawable.challenger
        }
    }

    private fun calculateRank(value: Int): Constants.Ranks {
        return when (value) {
            in 0 until lpSeparation -> Constants.Ranks.IRON
            in lpSeparation until lpSeparation*2 -> Constants.Ranks.BRONZE
            in lpSeparation*2 until lpSeparation*3 -> Constants.Ranks.SILVER
            in lpSeparation*3 until lpSeparation*4 -> Constants.Ranks.GOLD
            in lpSeparation*4 until lpSeparation*5 -> Constants.Ranks.PLATINUM
            in lpSeparation*5 until lpSeparation*6 -> Constants.Ranks.DIAMOND
            in lpSeparation*6 until lpSeparation*7 -> Constants.Ranks.MASTER
            in lpSeparation*7 until lpSeparation*8 -> Constants.Ranks.GRANDMASTER
            else -> Constants.Ranks.CHALLENGER
        }
    }

    fun animationEnded() = viewModelScope.launch {
        if(!transitioned) {
            transitioned = true
            introChampEventsChannel.send(IntroChampEvents.AnimationEnded)
        }
    }

    sealed class IntroChampEvents {
        object RecentInitBoostDetermined: IntroChampEvents()
        object AnimationEnded: IntroChampEvents()
        data class InitBoostReady(val boost: Int): IntroChampEvents()
        data class Error(val error: String): IntroChampEvents()

    }
}