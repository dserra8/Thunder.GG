package com.example.leagueapp1.ui.listChamp

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
import com.example.lBeagueapp1.ListChamp.ListChampViewModel
import com.example.leagueapp1.adapters.ChampItem
import com.example.leagueapp1.adapters.ChampionListAdapterNoHeader
import com.example.leagueapp1.MainViewModel
import com.example.leagueapp1.R
import com.example.leagueapp1.database.ChampionMastery
import com.example.leagueapp1.database.SortOrder
import com.example.leagueapp1.databinding.ListChampsBinding
import com.example.leagueapp1.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListChampFragment : Fragment() {

    private lateinit var binding: ListChampsBinding

    val viewModel: ListChampViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var searchView: SearchView? = null

    private lateinit var lastHighlighted: MenuItem

    private lateinit var listAdapter: ChampionListAdapterNoHeader

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = ListChampsBinding.inflate(inflater)

        mainViewModel.updateActionBarTitle("Champions", false)
        mainViewModel.isSummonerActive(true)

        viewModel.navigatedFromOtherScreen()

        listAdapter = ChampionListAdapterNoHeader() {
            viewModel.onClickChamp(it)
        }

        binding.goToStartOfListButton.setOnClickListener {
            viewModel.floatingActionButtonClicked()
        }

        binding.recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            if(binding.recyclerView.getCurrentPosition() >= 1){
                binding.goToStartOfListButton.visibility = VISIBLE
            }
            else
                binding.goToStartOfListButton.visibility = INVISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.championListEvents.collect { event ->
                    when (event) {
                        is ListChampViewModel.ChampListEvents.NavigateToChampScreen -> {
                            findNavController().navigate(event.action)
                        }
                        is ListChampViewModel.ChampListEvents.GoTopOfList -> {
                            val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                            layoutManager.smoothScrollToPosition(binding.recyclerView, null, 0)
                        }
                        else -> {}
                    }.exhaustive
                }
            }
        }

        binding.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
        }


        viewModel.championList.observe(viewLifecycleOwner) { result ->
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

        viewModel.highestMasteryChampion.observe(viewLifecycleOwner){champion ->
            val splashName = formatSplashName(champion?.championId ?: 99)
            mainViewModel.triggerHeaderChannel(splashName)

        }
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun showChampList(result: List<ChampionMastery>){
        setupAdapter(result)
        binding.progressBar.visibility = INVISIBLE
        binding.textViewError.visibility = INVISIBLE
    }

    private fun setupAdapter(data: List<ChampionMastery>) {
        val list = mutableListOf<ChampItem>()

        for (champion in data) {
            val name: String = champion.champName
            val photoName = viewModel.formatPhotoName(name)
            val imageId: Int =
                resources.getIdentifier(photoName, "drawable", activity?.packageName)
            val rankImgName = viewModel.formatRankName(champion.rankInfo?.rank ?: "NONE")
            val rankImgId = resources.getIdentifier(rankImgName, "drawable", activity?.packageName)

            list.add(
                ChampItem(
                    imageId,
                    name,
                    champion.championId,
                    champion.championPoints.toInt(),
                    rankImgId
                )
            )
        }
        listAdapter.submitList(list)
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