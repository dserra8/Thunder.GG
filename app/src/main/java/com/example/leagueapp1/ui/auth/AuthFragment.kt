package com.example.leagueapp1.ui.auth

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.leagueapp1.R
import com.example.leagueapp1.data.remote.BasicAuthInterceptor
import com.example.leagueapp1.databinding.AuthLayoutBinding
import com.example.leagueapp1.ui.MainActivity
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.Constants.NO_EMAIL
import com.example.leagueapp1.util.Constants.NO_PASSWORD
import com.example.leagueapp1.util.Resource
import com.example.leagueapp1.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject

@InternalCoroutinesApi
@ExperimentalSerializationApi
@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.auth_layout) {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var binding: AuthLayoutBinding

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AuthLayoutBinding.bind(view)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        if (isLoggedIn()) {
            authenticateApi()
            redirectLogin()
        }
        collectEvents()
        subscribeToObservers()
    }

    private fun isLoggedIn(): Boolean {
        viewModel.email = sharedPref.getString(
            Constants.KEY_LOGGED_IN_EMAIL,
            NO_EMAIL
        ) ?: NO_EMAIL
        viewModel.password = sharedPref.getString(
            Constants.KEY_LOGGED_IN_PASSWORD,
            NO_PASSWORD
        ) ?: NO_PASSWORD
        return viewModel.email != NO_EMAIL && viewModel.password != NO_PASSWORD
    }

    private fun authenticateApi() {
        basicAuthInterceptor.email = viewModel.email
        basicAuthInterceptor.password = viewModel.password
    }

    private fun redirectLogin() {
        findNavController().navigate(
            AuthFragmentDirections.actionAuthFragmentToListChampFragment())
    }

    /**
     * TODO: Implement encrypted shared preferences or encrypted data-store
     */

    private fun subscribeToObservers() {
        viewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is Resource.Error -> {
                        binding.loginButton.visibility = VISIBLE
                        binding.loadingBarAuth.visibility = INVISIBLE
                        binding.passwordTextField.setText("")
                        Snackbar.make(
                            requireView(),
                            it.error?.message ?: "An unknown error occured",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is Resource.Loading -> {
                        binding.loginButton.visibility = INVISIBLE
                        binding.loadingBarAuth.visibility = VISIBLE
                    }
                    is Resource.Success -> {
                        Snackbar.make(
                            requireView(),
                            it.data?: "Successfully logged in",
                            Snackbar.LENGTH_LONG
                        ).show()
                        sharedPref.edit().putString(Constants.KEY_LOGGED_IN_EMAIL, viewModel.email).apply()
                        sharedPref.edit().putString(Constants.KEY_LOGGED_IN_PASSWORD, viewModel.password).apply()
                        authenticateApi()
                        viewModel.syncSummoner()
                    }
                }.exhaustive
            }
        }

        viewModel.registerStatus.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is Resource.Error -> {
                        binding.registerButton.visibility = VISIBLE
                        binding.loadingBarAuth.visibility = INVISIBLE
                        binding.registerPasswordTextField.setText("")
                        binding.repeatPasswordTextField.setText("")
                        Snackbar.make(
                            requireView(),
                            it.error?.message ?: "An unknown error occured",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is Resource.Loading -> {
                        binding.registerButton.visibility = INVISIBLE
                        binding.loadingBarAuth.visibility = VISIBLE
                    }
                    is Resource.Success -> {
                        Snackbar.make(
                            requireView(),
                            it.data ?: "Successfully logged in",
                            Snackbar.LENGTH_LONG
                        ).show()
                        authenticateApi()
                        redirectLogin()
                    }
                }.exhaustive
            }
        }
    }


    private fun collectEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apply {
                    authEvent.collect { event ->
                        when (event) {
                            is AuthViewModel.AuthEvents.LoginClicked -> {
                                setEmailPassword(binding.usernameTextField.text.toString(), binding.passwordTextField.text.toString())
                                login(email, password)
                            }
                            is AuthViewModel.AuthEvents.RegisterClicked -> {
                                setEmailPassword(binding.registerUsernameTextField.text.toString(), binding.registerPasswordTextField.text.toString())
                                register(email, password, binding.repeatPasswordTextField.text.toString(), binding.registerSummonerTextField.text.toString())
                            }
                            is AuthViewModel.AuthEvents.SyncingDone -> {
                                redirectLogin()
                            }
                        }.exhaustive

                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.hideUpButton()
    }
}