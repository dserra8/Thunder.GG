package com.example.leagueapp1.feature_champions.presentation.game_overview

import android.os.Bundle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class GameOverviewPopupWindowViewModel: ViewModel() {

    private val _kdaText = MutableStateFlow("")
    val kdaText = _kdaText.asStateFlow()

    private val _positionText = MutableStateFlow("")
    val positionText = _positionText.asStateFlow()

    private val _lpText = MutableStateFlow("")
    val lpText = _lpText.asStateFlow()

    fun setData(bundle: Bundle?){
        _kdaText.value = bundle?.getString("kdaText", "KDA") ?: ""
        _positionText.value = bundle?.getString("positionText", "Position") ?: ""
        _lpText.value = bundle?.getString("lpText", "LP") ?: ""
    }

}