package com.example.leagueapp1.ui

import androidx.lifecycle.*
import com.example.leagueapp1.data.local.PreferencesManager
import com.example.leagueapp1.data.local.SummonerProperties
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val mainActivityEventsChannel = Channel<MainActivityEvents>()
    val mainActivityEvents = mainActivityEventsChannel.receiveAsFlow()

    private val champListStateChannel = Channel<LeagueRepository.ChampListState>()
    private val champListEvents = champListStateChannel.receiveAsFlow()

   // private val championEventsFlow = repository.champListEvents

    private val headerFlow = champListEvents.flatMapLatest {
        val summoner = repository.getSummoner()
        repository.getHeaderInfo(summoner?.name ?: "No User", summoner?.profileIconId ?: 10, it)
    }

    @ExperimentalCoroutinesApi
    val headerInfo = headerFlow.asLiveData()

    fun triggerHeaderChannel(splashName: String) = viewModelScope.launch{
        champListStateChannel.send(LeagueRepository.ChampListState.Ready(splashName))
    }

    fun updateActionBarTitle(title: String, ishome: Boolean) {
        viewModelScope.launch {
            if (ishome) {
            //    mainActivityEventsChannel.send(MainActivityEvents.ChangeActionBarHome(title))
            }
            else{
                    mainActivityEventsChannel.send(MainActivityEvents.ChangeActionBarOther(title))
                }
            }
        }

    sealed class MainActivityEvents{
    //    data class ChangeActionBarHome(val name: String): MainActivityEvents()
        data class ChangeActionBarOther(val name: String): MainActivityEvents()
    //    data class ChangeNavigationHeader(val name: String, val iconId: Int, val splashName: String)
    }
}
