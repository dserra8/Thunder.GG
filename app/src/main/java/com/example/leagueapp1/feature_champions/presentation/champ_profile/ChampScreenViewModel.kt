package com.example.leagueapp1.feature_champions.presentation.champ_profile

import androidx.lifecycle.*
import com.example.leagueapp1.R
import com.example.leagueapp1.adapters.ChampItem
import com.example.leagueapp1.core.domain.models.update.UpdateEvent
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.use_case.ChampionUseCases
import com.example.leagueapp1.feature_champions.domain.use_case.FormatSplashNameUseCase
import com.example.leagueapp1.feature_champions.domain.use_case.GetChampionUseCase
import com.example.leagueapp1.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChampScreenViewModel @Inject constructor(
    private val championUseCases: ChampionUseCases,
    private val getChampionUseCase: GetChampionUseCase,
    private val formatSplashNameUseCase: FormatSplashNameUseCase,
    private val state: SavedStateHandle,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    val champObj = state.get<ChampItem>("championPicked")

    private val _lpText = MutableStateFlow(0)
    val lpText = _lpText.asStateFlow()

    private val _rankImage = MutableStateFlow(R.drawable.ic_error)
    val rankImage = _rankImage.asStateFlow()

    private val champScreenEventsChannel = Channel<ChampScreenEvents>()
    val champScreenEvents = champScreenEventsChannel.receiveAsFlow()

    private val _champState = MutableStateFlow<Champion?>(null)
    val champState = _champState.asStateFlow()

    private lateinit var gameEventList : MutableList<UpdateEvent>

    init {
        viewModelScope.launch {
            champScreenEventsChannel.send(ChampScreenEvents.SplashReady(getSplashName()))
        }
    }

    private suspend fun getSplashName(): String {
        return champObj?.let { formatSplashNameUseCase(it.id) } ?: "Akali_1"
    }

    fun summonerReady(championId: Int) = viewModelScope.launch {
        val champ = withContext(dispatchers.io) {
            getChampionUseCase(championId)
        }
        champ?.let {
            gameEventList = it.updateEvents
            _champState.value = it
         //   champScreenEventsChannel.send(ChampScreenEvents.ChampReady(it))
        }
    }

    fun updateLpText(lp: Int) {
        _lpText.value = lp
    }

    fun updateRankImg(champ: Champion) {
        _rankImage.value = when(champ.rankInfo?.rank) {
            Constants.Ranks.IRON.toString() -> R.drawable.emblem_iron
            Constants.Ranks.BRONZE.toString() -> R.drawable.bronze
            Constants.Ranks.SILVER.toString() -> R.drawable.silver
            Constants.Ranks.GOLD.toString() -> R.drawable.gold
            Constants.Ranks.PLATINUM.toString() -> R.drawable.platinum
            Constants.Ranks.DIAMOND.toString() -> R.drawable.diamond
            Constants.Ranks.MASTER.toString() -> R.drawable.master
            Constants.Ranks.GRANDMASTER.toString() -> R.drawable.grandmaster
            Constants.Ranks.CHALLENGER.toString() -> R.drawable.challenger
            else -> R.drawable.ic_error
        }
    }

    fun removeUpdateEvent() = viewModelScope.launch{
        gameEventList.removeFirst()
        withContext(dispatchers.io){
            champObj?.let { championUseCases.updateEventListUseCase(gameEventList, champObj.id) }
        }
    }

    fun getNextUpdateEvent() = gameEventList.first()

    fun isUpdateEvents() = gameEventList.isNotEmpty()


    sealed class ChampScreenEvents {
        data class ChampReady(val champ: Champion) : ChampScreenEvents()
        data class SplashReady(val name: String): ChampScreenEvents()
    }
}