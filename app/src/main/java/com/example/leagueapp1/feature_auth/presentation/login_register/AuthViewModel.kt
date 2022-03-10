package com.example.leagueapp1.feature_auth.presentation.login_register

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leagueapp1.core.data.remote.BasicAuthInterceptor
import com.example.leagueapp1.core.domain.use_case.CoreUseCases
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.core.util.Resource
import com.example.leagueapp1.feature_auth.domain.use_case.AuthUseCases
import com.example.leagueapp1.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject

@ExperimentalSerializationApi
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val coreUseCases: CoreUseCases,
    private val dispatchers: DispatcherProvider,
    private var basicAuthInterceptor: BasicAuthInterceptor,
    private var sharedPref: SharedPreferences
) : ViewModel() {


    var username: String = ""
    var password: String = ""

    private val _loginFlow = MutableSharedFlow<Resource<String>>()
    val loginFlow = _loginFlow.asSharedFlow()

    private val _registerFlow = MutableSharedFlow<Resource<String>>()
    val registerFlow = _registerFlow.asSharedFlow()

    val registerPasswordState = MutableStateFlow("")
    val repeatPasswordState = MutableStateFlow("")

    private fun resetRegisterPassword() {
        registerPasswordState.value = ""
        repeatPasswordState.value = ""
    }

    fun onEvent(event: AuthEvents) {
        when (event) {
            is AuthEvents.LoginClicked -> {
                setEmailPassword(event.username, event.pass)
                login()
            }
            is AuthEvents.RegisterClicked -> {
                setEmailPassword(event.username, event.pass)
                register(event.confirmedPass, event.name)
            }
        }
    }

    fun authenticateApi() {
        basicAuthInterceptor.email = username
        basicAuthInterceptor.password = password
    }

    fun isLoggedIn(): Boolean {
        username = sharedPref.getString(
            Constants.KEY_LOGGED_IN_EMAIL,
            Constants.NO_EMAIL
        ) ?: Constants.NO_EMAIL
        password = sharedPref.getString(
            Constants.KEY_LOGGED_IN_PASSWORD,
            Constants.NO_PASSWORD
        ) ?: Constants.NO_PASSWORD
        return username != Constants.NO_EMAIL && password != Constants.NO_PASSWORD
    }

    private fun login() {
        viewModelScope.launch {
            _loginFlow.emit(Resource.Loading())
            val loginResult = authUseCases.loginUseCase(username, password)

            when {
                loginResult.passwordError != null -> {
                    _loginFlow.emit(Resource.Error(Throwable(loginResult.passwordError)))
                }
                loginResult.usernameError != null -> {
                    _loginFlow.emit(Resource.Error(Throwable(loginResult.usernameError)))
                }
                else -> {
                    when (val result = loginResult.result) {
                        is NoLoadResource.Error -> {
                            _loginFlow.emit(
                                Resource.Error(
                                    throwable = result.error ?: Throwable("Unknown Error")
                                )
                            )
                        }
                        is NoLoadResource.Success -> {
                            withContext(dispatchers.io) {
                                authUseCases.changeMainSummonerUseCase(result.data!!)
                                    .onSuccess {
                                        authenticateApi()
                                        coreUseCases.syncUseCase()
                                            .onSuccess {
                                                _loginFlow.emit(Resource.Success("Successfully logged in"))
                                            }.onFailure {
                                                _loginFlow.emit(Resource.Error(it))
                                            }
                                    }.onFailure {
                                        _loginFlow.emit(Resource.Error(it))
                                    }
                            }

                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun register(confirmedPassword: String, summonerName: String) {
        viewModelScope.launch {
            _registerFlow.emit(Resource.Loading())
            val registerResult = withContext(dispatchers.io) {
                authUseCases.registerUseCase(
                    username = username,
                    password = password,
                    repeatedPass = confirmedPassword,
                    summonerName = summonerName
                )
            }

            when {
                registerResult.passwordError != null -> {
                    resetRegisterPassword()
                    _registerFlow.emit(Resource.Error(Throwable(registerResult.passwordError)))
                }
                registerResult.usernameError != null -> {
                    resetRegisterPassword()
                    _registerFlow.emit(Resource.Error(Throwable(registerResult.usernameError)))
                }
                registerResult.summonerError != null -> {
                    resetRegisterPassword()
                    _registerFlow.emit(Resource.Error(Throwable(registerResult.summonerError)))
                }
                else -> {
                    when (val result = registerResult.result) {
                        is NoLoadResource.Error -> {
                            resetRegisterPassword()
                            _registerFlow.emit(
                                Resource.Error(
                                    throwable = result.error ?: Throwable("Unknown Error")
                                )
                            )
                        }
                        is NoLoadResource.Success -> {
                            withContext(dispatchers.io) {
                                authenticateApi()
                                coreUseCases.insertSummonerAndChampionsUseCase(result.data)
                                    .onSuccess {
                                        authUseCases.changeMainSummonerUseCase(result.data!!.puuid)
                                            .onSuccess {
                                                _registerFlow.emit(Resource.Success("Successfully registered"))
                                            }
                                    }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setEmailPassword(e: String, p: String) {
        username = e
        password = p

    }

    sealed class AuthEvents {
        data class LoginClicked(val username: String, val pass: String) : AuthEvents()
        data class RegisterClicked(
            val username: String,
            val pass: String,
            val confirmedPass: String,
            val name: String
        ) : AuthEvents()
    }
}