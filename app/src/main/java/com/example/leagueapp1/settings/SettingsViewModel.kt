package com.example.leagueapp1.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leagueapp1.di.ApplicationScope
import com.example.leagueapp1.repository.LeagueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: LeagueRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) :  ViewModel() {

    private val settingEventChannel = Channel<SettingsEvents>()
    val settingEvents = settingEventChannel.receiveAsFlow()

    fun onConfirmClick() = applicationScope.launch {
        repository.deleteCurrentSummonerAndChampions()
    }

    fun onDeleteSummonerClick() = viewModelScope.launch {
        settingEventChannel.send(SettingsEvents.DeleteSummoner)
    }
    fun onDeleteAllSummonerClick() = viewModelScope.launch {
        settingEventChannel.send(SettingsEvents.DeleteAllSummoners)
    }

    sealed class SettingsEvents {
        object DeleteSummoner: SettingsEvents()
        object DeleteAllSummoners: SettingsEvents()
    }
}