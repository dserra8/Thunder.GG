package com.example.leagueapp1.home

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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.leagueapp1.MainViewModel
import com.example.leagueapp1.R
import com.example.leagueapp1.databinding.HomeBinding
import com.example.leagueapp1.home.HomeFragmentDirections
import com.example.leagueapp1.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: HomeBinding

    private val viewModel: HomeViewModel by viewModels()

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

        mainViewModel.updateActionBarTitle(getString(R.string.app_name), true)

        viewModel.apply {

            summonersList.observe(viewLifecycleOwner) { summoners ->
                if(summoners.isNotEmpty() && summoners != null) {
                    val array = arrayListOf<String>()
                    for (summoner in summoners) {
                        array.add(summoner.name)
                    }
                    val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, array)
                    binding.summonerNameTextView.setAdapter(arrayAdapter)
                }

            }

            roleList.observe(viewLifecycleOwner){ list ->
                if( list == null || list.isEmpty()){
                    refreshRoleList()
                }else{
                    onGetRoleListEvent()
                }
            }

            summonerProperties.observe(viewLifecycleOwner){
                viewLifecycleOwner.lifecycleScope.launch {
                    summonerPropertiesReceived()
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
                                roleListAndUpdatesReady()
                            }
                            is HomeViewModel.HomeEvents.SummonerNotFound -> {
                                Snackbar.make(
                                    requireView(),
                                    event.error.message.toString(),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            is HomeViewModel.HomeEvents.RoleListReady -> {
                                roleListAndUpdatesReady()
                            }
                            is HomeViewModel.HomeEvents.NavigateToListScreen -> {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionHomeFragmentToListChampFragment()
                                )
                            }
                            is HomeViewModel.HomeEvents.RoleListFailed -> {
                                Snackbar.make(
                                    requireView(),
                                    event.errorMessage,
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

