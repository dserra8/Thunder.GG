package com.example.leagueapp1.feature_settings.presentation.main_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leagueapp1.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope
) :  ViewModel() {

    private val settingEventChannel = Channel<SettingsEvents>()
    val settingEvents = settingEventChannel.receiveAsFlow()

    fun onConfirmClick() = applicationScope.launch {
    }

    fun onDeleteSummonerClick() = viewModelScope.launch {
        settingEventChannel.send(SettingsEvents.DeleteSummoner)
    }

    fun onLogout() = viewModelScope.launch {
        settingEventChannel.send(SettingsEvents.Logout)
    }

    sealed class SettingsEvents {
        object DeleteSummoner: SettingsEvents()
        object Logout: SettingsEvents()
    }
}