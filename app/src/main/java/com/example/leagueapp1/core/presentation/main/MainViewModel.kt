package com.example.leagueapp1.core.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.leagueapp1.core.domain.use_case.GetHeaderInfoUseCase
import com.example.leagueapp1.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getHeaderInfoUseCase: GetHeaderInfoUseCase,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val mainActivityEventsChannel = Channel<MainActivityEvents>()
    val mainActivityEvents = mainActivityEventsChannel.receiveAsFlow()

    private val champListStateChannel = Channel<GetHeaderInfoUseCase.ChampListState>()
    private val champListEvents = champListStateChannel.receiveAsFlow()

    // private val championEventsFlow = repository.champListEvents

    private val headerFlow = champListEvents.flatMapLatest {
        withContext(dispatchers.io){
            getHeaderInfoUseCase(it)
        }
    }

    @ExperimentalCoroutinesApi
    val headerInfo = headerFlow.asLiveData()

    fun triggerHeaderChannel(splashName: String) = viewModelScope.launch {
        champListStateChannel.send(GetHeaderInfoUseCase.ChampListState.Ready(splashName))
    }

    fun updateActionBarTitle(title: String) {
        viewModelScope.launch {
            mainActivityEventsChannel.send(MainActivityEvents.ChangeActionBarTitle(title))
        }
    }

    sealed class MainActivityEvents {
        data class ChangeActionBarTitle(val name: String) : MainActivityEvents()
    }
}
