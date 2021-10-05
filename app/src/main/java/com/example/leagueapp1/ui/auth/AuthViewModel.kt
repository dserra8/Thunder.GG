package com.example.leagueapp1.ui.auth

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.ui.home.HomeViewModel
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.Constants.KEY_LOGGED_IN_EMAIL
import com.example.leagueapp1.util.Constants.KEY_LOGGED_IN_PASSWORD
import com.example.leagueapp1.util.Constants.NO_EMAIL
import com.example.leagueapp1.util.Constants.NO_PASSWORD
import com.example.leagueapp1.util.DataStoreUtil
import com.example.leagueapp1.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject

@ExperimentalSerializationApi
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: LeagueRepository,
    private val dataStore: DataStoreUtil,
//    private val sharedPref: SharedPreferences
): ViewModel() {

    var email: String = ""
    var password: String = ""

    private val authEventChannel = Channel<AuthEvents>()
    val authEvent = authEventChannel.receiveAsFlow()

    private val _registerStatus = MutableLiveData<Resource<String>>()
    val registerStatus: LiveData<Resource<String>> = _registerStatus

    private val _loginStatus = MutableLiveData<Resource<String>>()
    val loginStatus: LiveData<Resource<String>> = _loginStatus

    fun onLogin() = viewModelScope.launch {
        authEventChannel.send(AuthEvents.LoginClicked)
    }

    fun onRegister() = viewModelScope.launch {
        authEventChannel.send(AuthEvents.RegisterClicked)
    }

    fun login(email: String, password: String) {
        _loginStatus.postValue(Resource.Loading(null))
        if (email.isEmpty() || password.isEmpty()) {
            _loginStatus.postValue(Resource.Error(Throwable( "Please fill out all fields")))
            return
        }
        viewModelScope.launch {
            val result = repository.login(email, password)
            _loginStatus.postValue(result)
        }
    }
    fun register(email: String, password: String, confirmedPassword: String, summonerName: String) {
        _registerStatus.postValue(Resource.Loading(null))
        if (email.isEmpty() || password.isEmpty() || confirmedPassword.isEmpty() || summonerName.isEmpty()) {
            _registerStatus.postValue(Resource.Error(Throwable( "Please fill out all fields")))
            return
        }
        if(password != confirmedPassword) {
            _registerStatus.postValue(Resource.Error(Throwable("The passwords do not match!")))
        }
        viewModelScope.launch {
            val result = repository.register(email, password, summonerName)
            _registerStatus.postValue(result)
        }
    }

    fun setEmailPassword(e: String, p: String) {
        email = e
        password = p

    }

    fun secureData() {
//        viewModelScope.launch {
//            dataStore.setSecuredData(password, Constants.SECURED_PASS)
//            dataStore.setSecuredData(email, Constants.SECURED_EMAIL)
//        }
    }

    fun retrieveData() {
       viewModelScope.launch {
//            email = dataStore.getSecuredData(Constants.SECURED_EMAIL).first()
//            password = dataStore.getSecuredData(Constants.SECURED_PASS).first()
//            if(email != NO_EMAIL && email.isNotBlank() && password != NO_EMAIL && password.isNotBlank())
//                authEventChannel.send(AuthEvents.RedirectLogin)

      }

    }
    sealed class AuthEvents {
        object LoginClicked: AuthEvents()
        object RegisterClicked: AuthEvents()
        object RedirectLogin: AuthEvents()
    }


}