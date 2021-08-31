package com.example.leagueapp1.ui.champDetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.leagueapp1.MainViewModel
import com.example.leagueapp1.R
import com.example.leagueapp1.database.ChampionMastery
import com.example.leagueapp1.databinding.ChampScreenBinding
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.exhaustive
import com.example.leagueapp1.util.formatSplashName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChampScreenFragment @Inject constructor(
    private val glide: RequestManager
) : Fragment(R.layout.champ_screen) {

    private lateinit var binding: ChampScreenBinding
    private val viewModel: ChampScreenViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = ChampScreenBinding.bind(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val args by navArgs<ChampScreenFragmentArgs>()
        val champObj = args.championPicked
        val splashName = formatSplashName(champObj.id)
        val splashArtUrl = "${Constants.SPLASH_ART_URL}${splashName}_0.jpg"

        glide.load(splashArtUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(binding.champImg)

        mainViewModel.updateActionBarTitle(champObj.champName, false)

        viewModel.apply {
            summonerFlow.observe(viewLifecycleOwner) {
                if (it != null) {
                    summonerReady(args.championPicked.id)
                }
            }


            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    champScreenEvents.collect { events ->
                        when (events) {
                            is ChampScreenViewModel.ChampScreenEvents.ChampReady -> {
                                updateRankImage(events.champ)
                                updateLpText(events.champ.rankInfo?.lp ?: 0)
                                binding.constraintLayout.transitionToEnd()
                            }
                        }.exhaustive
                    }
                }
            }
        }
    }

    private fun updateRankImage(champ: ChampionMastery) {
        binding.rankImg.setImageResource(
            when (champ.rankInfo?.rank) {
                Constants.Ranks.IRON.toString() -> R.drawable.emblem_iron
                Constants.Ranks.BRONZE.toString() -> R.drawable.bronze
                Constants.Ranks.SILVER.toString() -> R.drawable.silver
                Constants.Ranks.GOLD.toString() -> R.drawable.gold
                Constants.Ranks.PLATINUM.toString() -> R.drawable.platinum
                Constants.Ranks.DIAMOND.toString() -> R.drawable.diamond
                Constants.Ranks.MASTER.toString() -> R.drawable.master
                Constants.Ranks.GRANDMASTER.toString() -> R.drawable.grandmaster
                Constants.Ranks.CHALLENGER.toString() -> R.drawable.challenger
                else -> R.drawable.ic_error
            }
        )
    }
}


