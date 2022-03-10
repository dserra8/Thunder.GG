package com.example.leagueapp1.feature_auth.domain.use_case

import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.feature_auth.domain.models.RegisterResult
import com.example.leagueapp1.feature_auth.domain.repository.AuthRepositoryInterface
import com.example.leagueapp1.feature_auth.domain.util.ValidationUtil

class RegisterUseCase(
    private val repository: AuthRepositoryInterface
) {
    suspend operator fun invoke(username: String, password: String, repeatedPass: String, summonerName: String): RegisterResult {
        val usernameError = ValidationUtil.validateUsername(username)
        val passwordError = ValidationUtil.validatePassword(password, repeatedPass)
        val summonerError = ValidationUtil.validateSummoner(summonerName)

        if(usernameError != null || passwordError != null || summonerError != null)
            return RegisterResult(
                usernameError = usernameError,
                passwordError = passwordError,
                summonerError = summonerError
            )

        val result : NoLoadResource<SummonerFromKtor> = when(val registerResult = repository.register(username.trim(), password, summonerName.trim())){
            is NoLoadResource.Success -> {

                val summoner = registerResult.data!!

                NoLoadResource.Success(summoner)
            }
            is NoLoadResource.Error -> {
                NoLoadResource.Error(registerResult.error ?: Throwable("Unknown error"))
            }
        }

        return RegisterResult(
            result = result
        )
    }
}