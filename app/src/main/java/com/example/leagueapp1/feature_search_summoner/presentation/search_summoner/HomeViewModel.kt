package com.example.leagueapp1.feature_search_summoner.presentation.search_summoner


import androidx.lifecycle.*
import com.example.leagueapp1.core.util.Resource
import com.example.leagueapp1.core.domain.models.Summoner
import com.example.leagueapp1.feature_search_summoner.domain.use_case.SearchSummonerUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchSummonerUseCases: SearchSummonerUseCases
) : ViewModel() {

    //Live Data for getSummonerId call
    private val _summonerProperties = MutableLiveData<Resource<Summoner>>()
    val summoner: LiveData<Resource<Summoner>> = _summonerProperties


    //Live Data for Summoner Name
    private var _summonerName = ""
    private var submitClicked: Boolean = false


    private val homeEventChannel = Channel<HomeEvents>()
    val homeEvent = homeEventChannel.receiveAsFlow()

    val summonersList = searchSummonerUseCases.getAllSummonersUseCase().asLiveData()

    /**
     * ViewModel functions for when the submit button is clicked
     */

    fun makeSummonerList(summoners: List<Summoner>): ArrayList<String> {
        val array = arrayListOf<String>()
        for (summoner in summoners) {
            array.add(summoner.name)
        }
        return array
    }

    fun onSubmit() {
        viewModelScope.launch {
            submitClicked = true
            homeEventChannel.send(HomeEvents.SubmitClicked)
        }
    }

    fun submitIsClicked() {
        viewModelScope.launch {

        }
    }

    fun changeSummonerName(name: String) {
        _summonerName = name
    }

    /**
     * Other Utility functions related to events
     */

    sealed class HomeEvents {
        object SummonerFound : HomeEvents()
        data class SummonerNotFound(val error: Throwable) : HomeEvents()
        object SubmitClicked : HomeEvents()
    }
}