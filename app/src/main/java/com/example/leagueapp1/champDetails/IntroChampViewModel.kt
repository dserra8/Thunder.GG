package com.example.leagueapp1.champDetails

import androidx.lifecycle.*
import com.example.leagueapp1.R
import com.example.leagueapp1.champListRecyclerView.ChampItem
import com.example.leagueapp1.network.MatchDetails
import com.example.leagueapp1.repository.Repository
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class IntroChampViewModel @Inject constructor(
    val repository: Repository
) : ViewModel(){

    val summonerFlow = repository.summoner.asLiveData()

    private val introChampEventsChannel = Channel<IntroChampEvents>()
    val introChampEvents = introChampEventsChannel.receiveAsFlow()

    //Live Data for LP text
    private val _lpText = MutableLiveData<Int>()
    val lpText: LiveData<Int> = _lpText

    private val lpSeparation = 100

    private val requiredAmountOfGames = 3


    fun matchListForRecentBoost() = viewModelScope.launch {
        when (val result = repository.matchListForInitBoost()) {
            is Resource.Success ->{
                if(result.data != null){
                    calculateRecentBoost(result.data)
                }else
                    introChampEventsChannel.send(IntroChampEvents.RecentInitBoostDetermined)
            }
            is Resource.Error -> {
                introChampEventsChannel.send(IntroChampEvents.Error("Retrieving Match List Error: " + result.error?.message!!))
            }
            is Resource.Loading -> {
                introChampEventsChannel.send(IntroChampEvents.Error("Retrieving Match List Error: " + result.error?.message!!))
            }
        }
    }

    data class ChampWinRate(var wins: Int, var total: Int)

    private suspend fun calculateRecentBoost(matchList :List<String>) {
        withContext(Dispatchers.Default) {
            val winRatesList = hashMapOf<Int, ChampWinRate>()
            for (matchId in matchList) {
                val match = repository.getMatchDetails(matchId)
                val data = match.data
                when (match) {
                    is Resource.Success -> {
                        determineIfWin(data, winRatesList)
                    }
                    is Resource.Error -> {
                        introChampEventsChannel.send(IntroChampEvents.Error("Calculating Init Boost Error: " + match.error?.message!!))
                    }
                    is Resource.Loading -> {
                        introChampEventsChannel.send(IntroChampEvents.Error("Calculating Init Boost Error: " + match.error?.message!!))
                    }
                }
            }
            updateDatabaseForRecentBoost(winRatesList)
            introChampEventsChannel.send(IntroChampEvents.RecentInitBoostDetermined)
        }
    }

    private fun determineIfWin(data: MatchDetails?, winRatesList: HashMap<Int, ChampWinRate>) {
        val index = findSummonerIndex(data?.metadata?.participants!!)
        if (index != null) {
            val gameInfo = data.info.participants[index]
            val outcome = gameInfo.win
            val champId = gameInfo.championId
            val win = if (outcome) 1 else 0
            val entry = winRatesList.getOrPut(
                champId,
                {
                    ChampWinRate(win, win)
                }
            )
            entry.wins = entry.wins + win
            entry.total = entry.total + 1
        }
    }

    private fun findSummonerIndex(list: List<String?>): Int? {
        for ((index, summoner) in list.withIndex()) {
            if(summoner == summonerFlow.value?.puuid ?: "")
                return index
        }
        return null
    }

    private suspend fun updateDatabaseForRecentBoost(winRatesList: HashMap<Int, ChampWinRate>) {
        for (champion in winRatesList) {
            var recentBoost = when ((champion.value.wins.toFloat() / champion.value.total.toFloat()).times(100).toInt()) {
                50 -> 10
                51 -> 20
                52 -> 30
                53 -> 40
                54 -> 50
                55 -> 60
                56 -> 70
                57 -> 80
                58 -> 90
                in 59..100 -> 100
                else -> 0
            }
            if(champion.value.total <= requiredAmountOfGames) recentBoost = 0

            repository.updateChampionRecentBoost(summonerId = summonerFlow.value?.id!!, champId = champion.key, boost = recentBoost)
        }
        repository.updateSummoner(summonerFlow.value?.copy(initBoostCalculated = true)!!)
    }

    private fun calculateExperienceBoost(introChamp: ChampItem): Int {
        if(introChamp.masteryPoints ?: 0 > 20000){
            return when(summonerFlow.value?.rank){
                "IRON" -> 0
                "BRONZE" -> 0
                "SILVER" -> lpSeparation
                "GOLD" -> lpSeparation*2
                "PLATINUM" -> lpSeparation*3
                "DIAMOND" -> lpSeparation*4
                "MASTER" -> lpSeparation*5
                "GRANDMASTER" -> lpSeparation*6
                "CHALLENGER" -> lpSeparation*7
                else -> 0
            }
        }
        return 0
    }

    fun recentBoostReady(introChamp: ChampItem) = viewModelScope.launch {
        val champion = repository.getChampion(champId = introChamp.id, summonerId = summonerFlow.value?.id!!)
        val experienceBoost = if (champion.rankInfo?.experienceBoost == null) {
            val boost = calculateExperienceBoost(introChamp)
            repository.updateChampionExperienceBoost(
                summonerId = summonerFlow.value?.id!!,
                champId = introChamp.id,
                boost = boost
            )
            boost
        } else {
            champion.rankInfo.experienceBoost
        }
        introChampEventsChannel.send(IntroChampEvents.InitBoostReady((champion.rankInfo?.recentBoost
            ?: 0) + experienceBoost))
    }

    suspend fun updateChampionRank(lp: Int, rankKey: Int, champId: Int) {
        repository.updateChampionRank(summonerFlow.value?.id!!, rank = calculateRank(rankKey*lpSeparation), champId = champId, lp = lp)
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
        introChampEventsChannel.send(IntroChampEvents.AnimationEnded)
    }

    sealed class IntroChampEvents {
        object RecentInitBoostDetermined: IntroChampEvents()
        object AnimationEnded: IntroChampEvents()
        data class InitBoostReady(val boost: Int): IntroChampEvents()
        data class Error(val error: String): IntroChampEvents()

    }
}