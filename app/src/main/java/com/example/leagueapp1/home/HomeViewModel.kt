package com.example.leagueapp1.home


import android.widget.ArrayAdapter
import androidx.lifecycle.*
import com.example.leagueapp1.R
import com.example.leagueapp1.database.ChampionRoleRates
import com.example.leagueapp1.database.SummonerProperties
import com.example.leagueapp1.network.ErrorResponse
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.repository.Repository
import com.example.leagueapp1.util.DispatcherProvider
import com.example.leagueapp1.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LeagueRepository,
    ) : ViewModel() {

    //Live Data for getSummonerId call
    private val _summonerProperties = MutableLiveData<Resource<SummonerProperties>>()
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
            _summonerProperties.value = repository.checkAndReturnSummoner(_summonerName)
        }
    }

    /**
     * ViewModel functions to modify Summoner Properties
     */

    suspend fun summonerPropertiesReceived() {
        if (submitClicked) {
            submitClicked = false
            when (_summonerProperties.value) {
                is Resource.Success -> {
                    updatesDone = true
                    homeEventChannel.send(HomeEvents.SummonerFound)
                }
                is Resource.Error -> {
                    if (summonerProperties.value?.data != null) {
                        updatesDone = true
                        homeEventChannel.send(HomeEvents.SummonerFound)
                    } else {
                        homeEventChannel.send(HomeEvents.SummonerNotFound(summonerProperties.value?.error!!))
                    }
                }
                is Resource.Loading -> {
                    homeEventChannel.send(HomeEvents.SummonerNotFound(summonerProperties.value?.error!!))
                }
            }
        }
    }


    fun changeSummonerName(name: String) {
        _summonerName = name
    }

    /**
     * ViewModel functions related to champion roles
     */

    fun checkRoleList(list: List<ChampionRoleRates>?) {
        if( list == null || list.isEmpty()){
            refreshRoleList()
        }else{
            onGetRoleListEvent()
        }
    }
    private fun refreshRoleList() {
        viewModelScope.launch {
            when (val result = repository.refreshChampionRates()) {
                "Role List Success" -> {
                    onGetRoleListEvent()
                }
                else -> {
                    roleListFailed = true
                    homeEventChannel.send(HomeEvents.RoleListFailed(result))
                }
            }
        }
    }

    private fun onGetRoleListEvent() = viewModelScope.launch {
        roleListReady = true
        homeEventChannel.send(HomeEvents.RoleListReady)
    }

    fun roleListAndUpdatesReady() {
        if (updatesDone && roleListFailed) {
            roleListFailed = false
            refreshRoleList()
        } else if (roleListReady && updatesDone) {
            viewModelScope.launch {
                roleListReady = false
                updatesDone = false
                homeEventChannel.send(
                    HomeEvents.NavigateToListScreen(
                        summonerProperties.value?.data?.name!!,
                        summonerProperties.value?.data?.profileIconId?.toInt() ?: 1
                    )
                )
            }
        }
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