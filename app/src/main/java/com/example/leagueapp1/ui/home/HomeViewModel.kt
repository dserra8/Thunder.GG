package com.example.leagueapp1.ui.home


import androidx.lifecycle.*
import com.example.leagueapp1.data.local.ChampionRoleRates
import com.example.leagueapp1.data.local.SummonerProperties
import com.example.leagueapp1.data.remote.ErrorResponse
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LeagueRepository,
    ) : ViewModel() {

    //Live Data for getSummonerId call
    private val _summonerProperties = MutableLiveData<Resource<
            SummonerProperties>>()
    val summonerProperties: LiveData<Resource<SummonerProperties>> = _summonerProperties

    //Live Data for Error Response
    private val _errorResponse = MutableLiveData<ErrorResponse>()
    val errorResponse: LiveData<ErrorResponse> = _errorResponse


    //Live Data for Summoner Name
    private var _summonerName = ""
    private var roleListReady: Boolean = false
    private var updatesDone: Boolean = false
    private var roleListFailed: Boolean = false
    private var submitClicked: Boolean = false


    private val homeEventChannel = Channel<HomeEvents>()
    val homeEvent = homeEventChannel.receiveAsFlow()

    val roleList = repository.roleList

    val summonersList = repository.getAllSummoners().asLiveData()


    /**
     * ViewModel functions for when the submit button is clicked
     */

    fun makeSummonerList(summoners : List<SummonerProperties>) : ArrayList<String>  {
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
        data class NavigateToListScreen(val summonerName: String, val iconId: Int) : HomeEvents()
        object SummonerFound : HomeEvents()
        data class SummonerNotFound(val error: Throwable) : HomeEvents()
        object SubmitClicked : HomeEvents()
        object RoleListReady : HomeEvents()
        data class RoleListFailed(val errorMessage: String) : HomeEvents()
    }
}