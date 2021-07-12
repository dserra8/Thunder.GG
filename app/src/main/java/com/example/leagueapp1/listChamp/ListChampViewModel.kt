package com.example.lBeagueapp1.ListChamp

import androidx.lifecycle.*
import com.example.leagueapp1.champListRecyclerView.ChampItem
import com.example.leagueapp1.database.ChampionMastery
import com.example.leagueapp1.database.PreferencesManager
import com.example.leagueapp1.database.SortOrder
import com.example.leagueapp1.repository.Repository
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
    private val repository: Repository,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle
) : ViewModel() {


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
        championListEventsChannel.send(ChampListEvents.NavigateToChampScreen(champ))
    }

//    fun makeChampItemList(data: List<ChampionMastery>, resources: Resources): List<ChampItem>{
//
//
//    }

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
        return photoName.toLowerCase(Locale.ROOT)
    }

    fun floatingActionButtonClicked() = viewModelScope.launch {
        championListEventsChannel.send(ChampListEvents.GoTopOfList)
    }

    suspend fun getCurrentSummoner() =
        repository.getCurrentSummoner()

    sealed class ChampListEvents {
        data class NavigateToChampScreen(val champ: ChampItem) : ChampListEvents()
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

