package com.example.leagueapp1.core.presentation.util

import com.example.leagueapp1.core.util.Event

sealed class UiEvent: Event() {
    data class ShowSnackBar(val message: String): UiEvent()
    object Navigate: UiEvent()
}
