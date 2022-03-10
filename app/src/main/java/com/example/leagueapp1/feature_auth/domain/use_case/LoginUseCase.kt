package com.example.leagueapp1.feature_auth.domain.use_case

import com.example.leagueapp1.feature_auth.domain.models.LoginResult
import com.example.leagueapp1.feature_auth.domain.repository.AuthRepositoryInterface
import com.example.leagueapp1.feature_auth.presentation.util.AuthError
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.FIELD_EMPTY

class LoginUseCase(
    private val repository: AuthRepositoryInterface
) {
    suspend operator fun invoke(username: String, password: String): LoginResult {
        val usernameError = if(username.isBlank()) FIELD_EMPTY else null
        val passwordError = if(password.isBlank()) FIELD_EMPTY else null

        if(usernameError != null || passwordError != null)
            return LoginResult(usernameError, passwordError)

        return LoginResult(result = repository.login(username, password))
    }
}