package com.example.leagueapp1.champDetails

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnRepeat
import androidx.core.animation.doOnStart
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.leagueapp1.R
import com.example.leagueapp1.databinding.IntroChampLayoutBinding
import com.example.leagueapp1.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IntroChampFragment: Fragment(R.layout.intro_champ_layout) {

    private lateinit var binding: IntroChampLayoutBinding

    private lateinit var circularProgress: CircularProgressBar

    private lateinit var rankImg: ImageView

    private val args by navArgs<IntroChampFragmentArgs>()

    private val viewModel: IntroChampViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = IntroChampLayoutBinding.bind(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this



        circularProgress = binding.loadingProgressCircle
        rankImg = binding.imageView

        binding.imageView.setImageResource(R.drawable.emblem_iron)

        viewModel.apply {

            summonerFlow.observe(viewLifecycleOwner) {
                if (it != null) {
                    matchListForRecentBoost()
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    introChampEvents.collect { events ->
                        when (events) {
                            is IntroChampViewModel.IntroChampEvents.AnimationEnded -> {
                                val action = IntroChampFragmentDirections.actionIntroChampFragmentToChampScreenFragment(args.introChampionPicked)
                                findNavController().navigate(action)
                            }
                            is IntroChampViewModel.IntroChampEvents.Error -> {
                                Snackbar.make(requireView(), events.error, Snackbar.LENGTH_LONG)
                            }
                            is IntroChampViewModel.IntroChampEvents.InitBoostReady -> {
                                val lp = events.boost.rem(100)
                                val rankKey = events.boost.div(100)
                                updateChampionRank(lp = lp, rankKey = rankKey, champId = args.introChampionPicked.id)
                                initiateBoostAnimation(lp, rankKey)
                            }
                            is IntroChampViewModel.IntroChampEvents.RecentInitBoostDetermined -> {
                                recentBoostReady(args.introChampionPicked)
                            }
                        }.exhaustive

                    }
                }
            }
        }
    }

    private fun initiateBoostAnimation(leftOver: Int, repeatCount: Int) {
            viewModel.apply {
                var repeatNum = 0
              //  repeatCount = bonus.div(100) - 1
                val valueForAnimator = if(repeatCount - 1 >= 0) 100 else 0
                val animator: ValueAnimator = ValueAnimator.ofInt(0, valueForAnimator)
                animator.duration = 1500
                animator.startDelay = 1500
                animator.repeatCount = if (repeatCount - 1 >= 0) repeatCount - 1 else 0
                animator.addUpdateListener { animation ->
                    if (animation != null) {
                        updateLpText(animation.animatedValue.toString())
                        circularProgress.progress =
                            animation.animatedValue.toString().toFloat()
                    }
                }
                animator.doOnStart {
                    binding.constraintLayout2.transitionToEnd()
                    circularProgress.indeterminateMode = false
                }
                animator.doOnRepeat {
                    repeatNum++
                    rankImg.setImageResource(chooseRankImage(repeatNum))
                }

                animator.doOnEnd {
                    if (repeatNum < repeatCount) {
                        rankImg.setImageResource(chooseRankImage(repeatNum + 1))
                    }
                    val animatorEnd: ValueAnimator = ValueAnimator.ofInt(0, leftOver)
                    animatorEnd.duration = 2000
                    animatorEnd.addUpdateListener { animation ->
                        if (animation != null) {
                            updateLpText(animation.animatedValue.toString())
                            circularProgress.progress =
                                animation.animatedValue.toString().toFloat()
                        }
                    }
                    animatorEnd.start()
                    animatorEnd.doOnEnd {
                        animationEnded()
                    }
                }
                animator.start()
            }
    }
}