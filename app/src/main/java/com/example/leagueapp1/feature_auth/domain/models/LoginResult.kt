package com.example.leagueapp1.feature_auth.domain.models

import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.core.util.SimpleResource
import com.example.leagueapp1.feature_auth.presentation.util.AuthError

data class LoginResult(
    val usernameError: String? = null,
    val passwordError: String? = null,
    val result: NoLoadResource<String>? = null
)
