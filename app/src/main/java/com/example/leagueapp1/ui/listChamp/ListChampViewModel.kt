package com.example.lBeagueapp1.ListChamp

import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.example.leagueapp1.adapters.ChampItem
import com.example.leagueapp1.database.ChampionMastery
import com.example.leagueapp1.database.PreferencesManager
import com.example.leagueapp1.database.SortOrder
import com.example.leagueapp1.ui.listChamp.ListChampFragmentDirections
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.util.filterChampionName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ListChampViewModel @Inject constructor(
    val repository: LeagueRepository,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle
) : ViewModel() {

    private var transitioned = false

    private var navigatedFromOtherScreen: Boolean = false

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val championListEventsChannel = Channel<ChampListEvents>()
    val championListEvents = championListEventsChannel.receiveAsFlow()

    private val _highestMasteryChampion = MutableLiveData<ChampionMastery?>()
    val highestMasteryChampion: LiveData<ChampionMastery?> = _highestMasteryChampion

    @ExperimentalCoroutinesApi
    private val champFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        var finalQuery = query
        if (query == "" && navigatedFromOtherScreen) {
            finalQuery = filterPreferences.query
        }
        navigatedFromOtherScreen = false
        repository.getChampions(
            finalQuery,
            filterPreferences.sortOrder,
            filterPreferences.showADC,
            filterPreferences.showSup,
            filterPreferences.showMid,
            filterPreferences.showJungle,
            filterPreferences.showTop,
            filterPreferences.showAll
        )
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

    @ExperimentalCoroutinesApi
    val championList = champFlow.asLiveData()

    fun onClickChamp(champ: ChampItem) = viewModelScope.launch {
            val champion =
                getCurrentSummoner()?.let { repository.getChampion(champ.id, summonerId = it.id) }
            val action = if (champion?.rankInfo?.rank ?: "NONE" == "NONE") {
                ListChampFragmentDirections.actionListChampFragmentToIntroChampFragment(champ)
            } else {
                ListChampFragmentDirections.actionListChampFragmentToChampScreenFragment(champ)
            }

            championListEventsChannel.send(ChampListEvents.NavigateToChampScreen(action))
    }

    fun formatRankName(rank: String): String {
        return when(rank){
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
        viewModelScope.launch {
            _highestMasteryChampion.value =  repository.getHighestMasteryChampion()
        }


    fun formatPhotoName(name: String): String {
        var filterPair =
            filterChampionName(name)
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
        championListEventsChannel.send(ChampListEvents.GoTopOfList)
    }

    private suspend fun getCurrentSummoner() =
        repository.getCurrentSummoner()

    sealed class ChampListEvents {
        data class NavigateToChampScreen(val action: NavDirections) : ChampListEvents()
        object GoTopOfList : ChampListEvents()
    }

}

data class ShowOptions(
    var showADC: Boolean,
    var showSup: Boolean,
    var showMid: Boolean,
    var showJungle: Boolean,
    var showTop: Boolean,
    var showAll: Boolean
)

