package com.example.leagueapp1.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.leagueapp1.R
import com.example.leagueapp1.databinding.SettingsLayoutBinding
import com.example.leagueapp1.util.Constants.KEY_LOGGED_IN_EMAIL
import com.example.leagueapp1.util.Constants.KEY_LOGGED_IN_PASSWORD
import com.example.leagueapp1.util.Constants.NO_EMAIL
import com.example.leagueapp1.util.Constants.NO_PASSWORD
import com.example.leagueapp1.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var sharedPref: SharedPreferences

    private lateinit var binding: SettingsLayoutBinding

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsLayoutBinding.inflate(inflater)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this


        viewModel.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    settingEvents.collect { event ->
                        when (event) {
                            is SettingsViewModel.SettingsEvents.DeleteSummoner -> {
                                DeleteSummonerDialogFragment().show(
                                    childFragmentManager, DeleteSummonerDialogFragment.TAG
                                )
                            }
                            SettingsViewModel.SettingsEvents.Logout -> {
                                logout()
                            }
                        }.exhaustive
                    }
                }
            }
        }
        return binding.root
    }

    private fun logout() {
        sharedPref.edit().putString(KEY_LOGGED_IN_EMAIL, NO_EMAIL).apply()
        sharedPref.edit().putString(KEY_LOGGED_IN_PASSWORD, NO_PASSWORD).apply()

        findNavController().navigate(
            SettingsFragmentDirections.actionSettingsFragmentToAuthFragment()
        )
    }

}