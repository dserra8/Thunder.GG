package com.example.leagueapp1.feature_auth.presentation.login_register

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.leagueapp1.R
import com.example.leagueapp1.core.presentation.main.MainActivity
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.core.util.Resource
import com.example.leagueapp1.databinding.AuthLayoutBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ExperimentalSerializationApi
@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.auth_layout) {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var binding: AuthLayoutBinding

//    @Inject
//    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AuthLayoutBinding.bind(view)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        if (viewModel.isLoggedIn()) {
            viewModel.authenticateApi()
            redirectLogin()
        }
        collectEvents()
        setButtonClickListeners()
        binding.loadingBarAuth.visibility = INVISIBLE
    }


    private fun setButtonClickListeners() {
        binding.loginButton.setOnClickListener {
            viewModel.onEvent(
                AuthViewModel.AuthEvents.LoginClicked(
                    username = binding.usernameTextField.text.toString(),
                    pass = binding.passwordTextField.text.toString()
                )
            )
        }
        binding.registerButton.setOnClickListener {
            viewModel.onEvent(
                AuthViewModel.AuthEvents.RegisterClicked(
                    username = binding.registerUsernameTextField.text.toString(),
                    pass = binding.registerPasswordTextField.text.toString(),
                    confirmedPass = binding.repeatPasswordTextField.text.toString(),
                    name = binding.registerSummonerTextField.text.toString()
                )
            )
        }
    }

    private fun redirectLogin() {
        findNavController().navigate(
            AuthFragmentDirections.actionAuthFragmentToListChampFragment()
        )
    }

    private fun collectEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apply {
                    launch {
                        loginFlow.collectLatest { event ->
                            when (event) {
                                is Resource.Error -> {
                                    binding.loginButton.visibility = VISIBLE
                                    binding.loadingBarAuth.visibility = INVISIBLE
                                    binding.passwordTextField.setText("")
                                    Snackbar.make(
                                        requireView(),
                                        event.error?.message ?: "Unknown Error",
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
                                        event.data ?: "Successfully logged in",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    sharedPref.edit()
                                        .putString(Constants.KEY_LOGGED_IN_EMAIL, viewModel.username)
                                        .apply()
                                    sharedPref.edit()
                                        .putString(Constants.KEY_LOGGED_IN_PASSWORD, viewModel.password)
                                        .apply()
                                    redirectLogin()
                                }
                            }
                        }
                    }
                    launch {
                        registerFlow.collectLatest { event ->
                            when (event) {
                                is Resource.Error -> {
                                    binding.registerButton.visibility = VISIBLE
                                    binding.loadingBarAuth.visibility = INVISIBLE
                                    Snackbar.make(
                                        requireView(),
                                        event.error?.message ?: "An unknown error occurred",
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
                                        event.data ?: "Successfully logged in",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    redirectLogin()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.hideUpButton()
    }
}