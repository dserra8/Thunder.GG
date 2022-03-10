package com.example.leagueapp1.feature_search_summoner.presentation.search_summoner

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.leagueapp1.core.presentation.main.MainViewModel
import com.example.leagueapp1.R
import com.example.leagueapp1.databinding.HomeBinding
import com.example.leagueapp1.core.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: HomeBinding

    private val viewModel: HomeViewModel by viewModels()

    @ExperimentalCoroutinesApi
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeBinding.inflate(inflater)

        //Initializing my view model factory and the view model
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.apply {

            summonersList.observe(viewLifecycleOwner) { summoners ->
                if(summoners.isNotEmpty() && summoners != null) {
                    val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, makeSummonerList(summoners))
                    binding.summonerNameTextView.setAdapter(arrayAdapter)
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    homeEvent.collect { event ->
                        when (event) {
                            is HomeViewModel.HomeEvents.SubmitClicked -> {
                                changeSummonerName(binding.summonerNameTextView.text.toString())
                                closeKeyboard()
                                submitIsClicked()
                            }
                            is HomeViewModel.HomeEvents.SummonerFound -> {

                            }
                            is HomeViewModel.HomeEvents.SummonerNotFound -> {
                                Snackbar.make(
                                    requireView(),
                                    event.error.message.toString(),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }.exhaustive
                    }
                }
            }
        }


        return binding.root
    }

    private fun closeKeyboard() {
        binding.summonerNameTextView.let {
            activity?.hideKeyboard(it)
        }
    }
    private fun Context.hideKeyboard(view: View){
        val inputMethodManager =  getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

