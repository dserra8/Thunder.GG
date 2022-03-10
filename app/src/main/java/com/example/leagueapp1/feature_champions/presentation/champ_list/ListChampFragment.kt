package com.example.leagueapp1.feature_champions.presentation.champ_list

import android.os.Bundle
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leagueapp1.R
import com.example.leagueapp1.adapters.ChampItem
import com.example.leagueapp1.adapters.ChampionListAdapterNoHeader
import com.example.leagueapp1.core.presentation.main.MainViewModel
import com.example.leagueapp1.core.util.Resource
import com.example.leagueapp1.core.util.exhaustive
import com.example.leagueapp1.core.util.getCurrentPosition
import com.example.leagueapp1.data.local.SortOrder
import com.example.leagueapp1.databinding.ListChampsBinding
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ListChampFragment : Fragment() {

    private lateinit var binding: ListChampsBinding

    val viewModel: ListChampViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var searchView: SearchView? = null

    private lateinit var lastHighlighted: MenuItem

    private lateinit var listAdapter: ChampionListAdapterNoHeader


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = ListChampsBinding.inflate(inflater)

        subscribeToObservers()

        viewModel.navigatedFromOtherScreen()

        listAdapter = ChampionListAdapterNoHeader {
            viewModel.onClickChamp(it)
        }

        binding.goToStartOfListButton.setOnClickListener {
            viewModel.floatingActionButtonClicked()
        }

        binding.recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            if (binding.recyclerView.getCurrentPosition() >= 1) {
                binding.goToStartOfListButton.visibility = VISIBLE
            } else
                binding.goToStartOfListButton.visibility = INVISIBLE
        }

        binding.refreshContainer.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.apply {
            recyclerView.apply {
                listAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                adapter = listAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.highestMasteryChampionFlow.collectLatest { champion ->
                        val splashName = viewModel.getFormattedSplashName(champion?.championId ?: 99)
                        mainViewModel.triggerHeaderChannel(splashName)
                    }
                }

                launch {
                    viewModel.championListEventsFlow.collect { event ->
                        when (event) {
                            is ListChampViewModel.ChampListEvents.NavigateToChampScreen -> {
                                findNavController().navigate(event.action)
                            }
                            is ListChampViewModel.ChampListEvents.GoTopOfList -> {
                                val layoutManager =
                                    binding.recyclerView.layoutManager as LinearLayoutManager
                                layoutManager.smoothScrollToPosition(binding.recyclerView, null, 0)
                            }
                        }.exhaustive
                    }
                }

                launch {
                    viewModel.champFlow.collectLatest { result ->
                        when (result) {
                            is Resource.Success -> {
                                viewModel.getHighestMasteryChampion()
                                showChampList(result = result.data!!)
                            }
                            is Resource.Loading -> {
                                binding.progressBar.visibility = VISIBLE
                            }
                            is Resource.Error -> {
                                binding.textViewError.text = result.error?.message ?: "Error"
                                binding.textViewError.visibility = VISIBLE
                                result.data?.let {
                                    viewModel.getHighestMasteryChampion()
                                    showChampList(result = result.data)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showChampList(result: List<Champion>) {
        binding.progressBar.visibility = INVISIBLE
        binding.textViewError.visibility = INVISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            setupAdapter(result)
        }
    }

    private suspend fun setupAdapter(data: List<Champion>) {
        val list = mutableListOf<ChampItem>()


        for (champion in data) {
            val name: String = champion.champName
            val photoName = viewModel.formatPhotoName(name)
            val imageId: Int =
                resources.getIdentifier(photoName, "drawable", activity?.packageName)
            val rankImgName = viewModel.formatRankName(champion.rankInfo?.rank ?: "NONE")
            val rankImgId = resources.getIdentifier(rankImgName, "drawable", activity?.packageName)

            val splash = if (imageId == 0) {
               viewModel.getFormattedSplashName(champion.championId)
            } else null

            list.add(
                ChampItem(
                    champImageResource = imageId,
                    champName = name,
                    id = champion.championId,
                    masteryPoints = champion.championPoints.toInt(),
                    rankImageResource = rankImgId,
                    formattedName = splash,
                    isUpdate = champion.updateEvents.isNotEmpty()
                )
            )
        }
        listAdapter.submitList(list)
        binding.refreshContainer.isRefreshing = false
        viewModel.updateRefresh(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_champion_list, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView


        searchView?.onQueryTextChange {
            // update search query
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {

            val initials = viewModel.preferencesFlow.first()

            val pendingQuery = initials.query

            if (pendingQuery.isNotEmpty()) {
                searchItem.expandActionView()
                searchView?.setQuery(pendingQuery, false)
            }

            when (initials.sortOrder.name) {
                "BY_NAME" -> {
                    val subMenu = menu.getItem(1).subMenu
                    val item1 = subMenu[1]
                    lastHighlighted = subMenu[0]
                    item1.setIcon(R.drawable.ic_auto_awesome)
                }
                "BY_MASTERY_POINTS" -> {
                    val item = menu.getItem(1).subMenu[0]
                    lastHighlighted = menu.getItem(1).subMenu[1]
                    item.setIcon(R.drawable.ic_auto_awesome)
                }
            }

            when {
                initials.showADC -> menu.findItem(R.id.action_show_adc).isChecked = initials.showADC
                initials.showTop -> menu.findItem(R.id.action_show_top).isChecked = initials.showTop
                initials.showJungle -> menu.findItem(R.id.action_show_jungle).isChecked =
                    initials.showJungle
                initials.showMid -> menu.findItem(R.id.action_show_mid).isChecked = initials.showMid
                initials.showSup -> menu.findItem(R.id.action_show_Support).isChecked =
                    initials.showSup
                else -> menu.findItem(R.id.action_show_all).isChecked = initials.showAll
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_all -> {
                item.isChecked = !item.isChecked
                viewModel.onShowRolesCompletedClick(
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    item.isChecked,
                )
                true
            }
            R.id.action_show_Support -> {
                item.isChecked = !item.isChecked
                viewModel.onShowRolesCompletedClick(
                    !item.isChecked,
                    item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                )
                true
            }
            R.id.action_show_top -> {
                item.isChecked = !item.isChecked
                viewModel.onShowRolesCompletedClick(
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    item.isChecked,
                    !item.isChecked,
                )
                true
            }
            R.id.action_show_adc -> {
                item.isChecked = !item.isChecked
                viewModel.onShowRolesCompletedClick(
                    item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                )
                true
            }
            R.id.action_show_jungle -> {
                item.isChecked = !item.isChecked
                viewModel.onShowRolesCompletedClick(
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                )
                true
            }
            R.id.action_show_mid -> {
                item.isChecked = !item.isChecked
                viewModel.onShowRolesCompletedClick(
                    !item.isChecked,
                    !item.isChecked,
                    item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                    !item.isChecked,
                )

                true
            }
            R.id.action_sort_Name -> {
                lastHighlighted.setIcon(R.drawable.ic_auto_awesome)
                lastHighlighted = item
                item.setIcon(R.drawable.ic_pin)
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_mastery_points -> {
                lastHighlighted.setIcon(R.drawable.ic_auto_awesome)
                lastHighlighted = item
                item.setIcon(R.drawable.ic_pin)
                viewModel.onSortOrderSelected(SortOrder.BY_MASTERY_POINTS)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.updateQuery(viewModel.searchQuery.value ?: "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView?.setOnQueryTextListener(null)
    }
}