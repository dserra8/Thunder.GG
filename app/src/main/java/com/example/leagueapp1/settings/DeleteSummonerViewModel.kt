package com.example.leagueapp1.settings

import androidx.lifecycle.ViewModel
import com.example.leagueapp1.network.ApplicationScope
import com.example.leagueapp1.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteSummonerViewModel @Inject constructor(
    private val repository: Repository,
    @ApplicationScope private val applicationScope: CoroutineScope
) :  ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        //repository.deleteSummoner()
    }
}