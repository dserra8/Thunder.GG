package com.example.leagueapp1

import androidx.lifecycle.*
import com.example.leagueapp1.database.PreferencesManager
import com.example.leagueapp1.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val repository: Repository
) : ViewModel() {


    private var firstTime: Boolean = true
    var isActive: Boolean = false

    private val mainActivityEventsChannel = Channel<MainActivityEvents>()
    val mainActivityEvents = mainActivityEventsChannel.receiveAsFlow()

    val preferencesFlow = preferencesManager.mainPreferencesFlow

    private val summonerFlow = repository.summoner

    private val champListStateChannel = Channel<Repository.ChampListState>()
    private val champListEvents = champListStateChannel.receiveAsFlow()

   // private val championEventsFlow = repository.champListEvents

    @ExperimentalCoroutinesApi
    val headerFlow = combine(
        summonerFlow,
        champListEvents
    ){ summoner, champion ->
        Pair(summoner, champion)
    } .flatMapLatest { (summoner, champion) ->
        repository.getHeaderInfo(summoner?.name ?: "No User", summoner?.profileIconId ?: 10.0, champion)
    }

    @ExperimentalCoroutinesApi
    val headerInfo = headerFlow.asLiveData()

    fun triggerHeaderChannel(splashName: String) = viewModelScope.launch{
        champListStateChannel.send(Repository.ChampListState.Ready(splashName))
    }

    fun updateActionBarTitle(title: String, ishome: Boolean) {
        viewModelScope.launch {
            if (ishome) {
                mainActivityEventsChannel.send(MainActivityEvents.ChangeActionBarHome(title))
                firstTime = true
            }
            else{
                if(firstTime){
                    firstTime = false
                    mainActivityEventsChannel.send(MainActivityEvents.FirstTime(title))
                }
                else{
                    mainActivityEventsChannel.send(MainActivityEvents.ChangeActionBarOther(title))
                }
            }
        }
    }

    fun isSummonerActive(isActive: Boolean) = viewModelScope.launch {
        preferencesManager.updateIsSummonerActive(isActive)
    }

    suspend fun collectPreferencesFlow() {
        val data = preferencesFlow.first()
        isActive = data.isSummonerActive
    }


    fun changeNavigationHeader(splashName: String) = viewModelScope.launch {

    }


    sealed class MainActivityEvents{
        data class FirstTime(val name: String): MainActivityEvents()
        data class ChangeActionBarHome(val name: String): MainActivityEvents()
        data class ChangeActionBarOther(val name: String): MainActivityEvents()
        data class ChangeNavigationHeader(val name: String, val iconId: Int, val splashName: String)
    }
}
