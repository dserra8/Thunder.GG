package com.example.leagueapp1.feature_champions.presentation.champ_profile


import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.leagueapp1.R
import com.example.leagueapp1.core.domain.models.update.UpdateEvent
import com.example.leagueapp1.core.presentation.main.MainViewModel
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.core.util.exhaustive
import com.example.leagueapp1.databinding.ChampScreenBinding
import com.example.leagueapp1.feature_champions.domain.models.Champion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChampScreenFragment @Inject constructor(
    private val glide: RequestManager
) : Fragment(R.layout.champ_screen) {

    private lateinit var binding: ChampScreenBinding

    private lateinit var gameOverviewView: View

    private lateinit var mainPopupWindow: PopupWindow

    private lateinit var fadeWindow: PopupWindow

    private lateinit var kdaTextView: TextView
    private lateinit var positionTextView: TextView
    private lateinit var lpTextView: TextView
    private lateinit var gameOverviewLayout: RelativeLayout

    private val viewModel: ChampScreenViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()


    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = ChampScreenBinding.bind(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        createPopUpWindows()

        viewModel.champObj?.let {
            mainViewModel.updateActionBarTitle(it.champName)
            viewModel.summonerReady(it.id)
        }

        viewModel.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        champScreenEvents.collect { events ->
                            when (events) {
                                is ChampScreenViewModel.ChampScreenEvents.ChampReady -> {
                                }
                                is ChampScreenViewModel.ChampScreenEvents.SplashReady -> {
                                    updateImgs(events.name)
                                }
                            }.exhaustive
                        }
                    }

                    launch {
                        champState.collectLatest { champ ->
                            if (champ != null) {
                                if (champ.updateEvents.isNotEmpty()) {
                                    updateShowGameOverview(champ.updateEvents.first())
                                } else {
                                    updateLpViews(champ)
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    private fun updateLpViews(champ: Champion){
        val lp = champ.rankInfo?.lp ?: 0
        viewModel.updateRankImg(champ)
        lpAnimation(lp)
        binding.circularProgressIndicator.setProgressWithAnimation(
            lp.toFloat(),
            1500
        )
        binding.constraintLayout.transitionToEnd()
    }

    private fun updateShowGameOverview(game: UpdateEvent) {
        viewModel.removeUpdateEvent()
        kdaTextView.text = getString(R.string.kda_text_format, game.kills, game.deaths, game.assists)
        positionTextView.text = game.position
        lpTextView.text = getString(R.string.lp_text_format, game.lpGained)
        if(game.outcome){
            gameOverviewLayout.setBackgroundResource(R.drawable.game_overview_gradient_win)
        } else {
            gameOverviewLayout.setBackgroundResource(R.drawable.game_overview_gradient_loss)
        }
        showGameOverview()
    }

    private fun updateImgs(name: String){
        val splashUrl = "${Constants.SPLASH_ART_URL}${name}_0.jpg"
        val loadingUrl = "${Constants.LOADING_SCREEN_URL}${name}_0.jpg"
        val img = gameOverviewView.findViewById<ImageView>(R.id.loadingImg)
        glide.load(splashUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(binding.champImg)
        glide.load(loadingUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(img)
    }

    private fun createPopUpWindows() {
        gameOverviewView = layoutInflater.inflate(R.layout.game_overview_popup_window, null)

        mainPopupWindow = PopupWindow(
            gameOverviewView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )

        //Setting Elevation
        mainPopupWindow.elevation = 50.0F

        //Set Slide Animation
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.START
        mainPopupWindow.enterTransition = slideIn

        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        mainPopupWindow.exitTransition = slideOut


        mainPopupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fadeView = layoutInflater.inflate(R.layout.fadepopup, null)

        fadeWindow = PopupWindow(
            fadeView, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, false
        )

        gameOverviewView.findViewById<View>(R.id.game_overview_root_layout).setOnClickListener {
            mainPopupWindow.dismiss()

        }
        mainPopupWindow.setOnDismissListener {
            if(viewModel.isUpdateEvents()){
                updateShowGameOverview(viewModel.getNextUpdateEvent())
            } else {
                fadeWindow.dismiss()
                viewModel.champState.value?.let {
                    updateLpViews(it)
                }
            }

        }

        //Initialize Text Views
        kdaTextView = gameOverviewView.findViewById(R.id.kda_text)
        positionTextView = gameOverviewView.findViewById(R.id.positionTitle)
        lpTextView = gameOverviewView.findViewById(R.id.lpGained)
        gameOverviewLayout = gameOverviewView.findViewById(R.id.relativeGameOverViewLayout)
    }


    private fun showGameOverview() {
        fadeWindow.showAtLocation(binding.constraintLayout, Gravity.NO_GRAVITY, 0, 0)
        mainPopupWindow.showAtLocation(binding.constraintLayout, Gravity.CENTER, 0, 0)
    }


    private fun lpAnimation(lp: Int) {
        val animator: ValueAnimator = ValueAnimator.ofInt(0, lp)
        animator.duration = 1500
        var value = 0
        animator.addUpdateListener { animation ->
            if (animation != null) {
                value = animation.animatedValue.toString().toInt()
                viewModel.updateLpText(value)
            }
        }
        animator.start()
    }
}


