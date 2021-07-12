package com.example.leagueapp1.champDetails

import androidx.lifecycle.*
import com.example.leagueapp1.champListRecyclerView.ChampItem
import com.example.leagueapp1.database.ChampionMastery
import com.example.leagueapp1.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChampScreenViewModel @Inject constructor(
    private val repository: Repository,
    private val state: SavedStateHandle
) : ViewModel() {

    val summonerFlow = repository.summoner.asLiveData()

    private val champItem = state.get<ChampItem>("championPicked")

    //Live Data for LP text
    private val _lpText = MutableLiveData<Int>()
    val lpText: LiveData<Int> = _lpText

    private val _champ = MutableLiveData<ChampionMastery>()
    val champ: LiveData<ChampionMastery> = _champ


    private val champScreenEventsChannel = Channel<ChampScreenEvents>()
    val champScreenEvents = champScreenEventsChannel.receiveAsFlow()

    fun summonerReady(championId: Int) = viewModelScope.launch {
        val test = champItem?.id
        val champ = repository.getChampion(summonerId = summonerFlow.value?.id!!, champId = championId)
        champScreenEventsChannel.send(ChampScreenEvents.ChampReady(champ))
    }

    fun updateLpText(lp: Int) {
        _lpText.value = lp
    }


    sealed class ChampScreenEvents {
        data class ChampReady(val champ: ChampionMastery) : ChampScreenEvents()
    }
}