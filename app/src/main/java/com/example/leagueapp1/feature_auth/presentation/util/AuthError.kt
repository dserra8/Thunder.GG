package com.example.leagueapp1.feature_auth.presentation.util

import com.example.leagueapp1.core.util.Error

sealed class AuthError : Error() {
    data class InvalidUsername(val message: String): AuthError()
    data class InvalidPassword(val message: String) : AuthError()
    data class InvalidSummoner(val message: String): AuthError()
}
