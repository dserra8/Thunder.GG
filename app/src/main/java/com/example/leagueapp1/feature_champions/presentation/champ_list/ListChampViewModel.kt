package com.example.leagueapp1.feature_champions.presentation.champ_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.example.leagueapp1.adapters.ChampItem
import com.example.leagueapp1.data.local.PreferencesManager
import com.example.leagueapp1.data.local.SortOrder
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.use_case.ChampionUseCases
import com.example.leagueapp1.feature_champions.util.cleanChampionName
import com.example.leagueapp1.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ListChampViewModel @Inject constructor(
    private val championUseCases: ChampionUseCases,
    private val preferencesManager: PreferencesManager,
    private val dispatchers: DispatcherProvider,
    state: SavedStateHandle
) : ViewModel() {

    private var navigatedFromOtherScreen: Boolean = false

    var refresh: Boolean = false

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val refreshFlow = MutableStateFlow(false)

    private val _championListEventsFlow = MutableSharedFlow<ChampListEvents>()
    val championListEventsFlow = _championListEventsFlow.asSharedFlow()

    private val _highestMasteryChampionFlow = MutableSharedFlow<Champion?>()
    val highestMasteryChampionFlow = _highestMasteryChampionFlow.asSharedFlow()


    val champFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow,
        refreshFlow
    ) { query, filterPreferences, _ ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->

        val flow = withContext(dispatchers.io) {
            championUseCases.getChampionsUseCase(filterPreferences, query, navigatedFromOtherScreen, refresh)
        }
        navigatedFromOtherScreen = false
        flow
    }

    fun refresh() = viewModelScope.launch {
        updateRefresh(true)
        refreshFlow.value =  !refreshFlow.value
    }

    fun updateRefresh(state: Boolean) {
        refresh = state
    }
    fun updateQuery(query: String) = viewModelScope.launch {
        preferencesManager.updateQuery(query)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onShowRolesCompletedClick(
        showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ) = viewModelScope.launch {
        preferencesManager.updateShowRoles(showAll, showADC, showJungle, showTop, showMid, showSup)
    }

    fun onClickChamp(champ: ChampItem) = viewModelScope.launch {
        val action = ListChampFragmentDirections.actionListChampFragmentToChampScreenFragment(champ)
        _championListEventsFlow.emit(ChampListEvents.NavigateToChampScreen(action))
    }

    fun formatRankName(rank: String): String {
        return when (rank) {
            "IRON" -> "emblem_iron"
            "BRONZE" -> "bronze"
            "SILVER" -> "silver"
            "GOLD" -> "gold"
            "PLATINUM" -> "platinum"
            "DIAMOND" -> "diamond"
            "MASTER" -> "master"
            "GRANDMASTER" -> "grandmaster"
            "CHALLENGER" -> "challenger"
            else -> "@null"
        }
    }

    fun navigatedFromOtherScreen() {
        navigatedFromOtherScreen = true
    }

    fun getHighestMasteryChampion() =
        viewModelScope.launch(dispatchers.io) {
            _highestMasteryChampionFlow.emit(championUseCases.getHighestMasteryChampUseCase())
        }

    suspend fun getFormattedSplashName(id: Int): String = championUseCases.formatSplashNameUseCase(id)

    fun formatPhotoName(name: String): String {
        val filterPair =
            cleanChampionName(name)
        var photoName =
            filterPair.first

        photoName = when (photoName) {
            "NunuWillump" -> "nunu"
            "Wukong" -> "monkeyking"
            else -> photoName
        }
        return photoName.lowercase(Locale.ROOT)
    }

    fun floatingActionButtonClicked() = viewModelScope.launch {
        _championListEventsFlow.emit(ChampListEvents.GoTopOfList)
    }

    sealed class ChampListEvents {
        data class NavigateToChampScreen(val action: NavDirections) : ChampListEvents()
        object GoTopOfList : ChampListEvents()
    }

}

