package com.example.leagueapp1.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.leagueapp1.databinding.SettingsLayoutBinding
import com.example.leagueapp1.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

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
                        }.exhaustive
                    }
                }
            }
        }
        return binding.root
    }
}