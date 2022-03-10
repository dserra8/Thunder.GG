package com.example.leagueapp1.feature_champions.presentation.game_overview

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.leagueapp1.R
import android.view.ViewGroup




class GameOverviewPopupWindow: AppCompatActivity() {

    private val viewModel: GameOverviewPopupWindowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0,0)
        setContentView(R.layout.game_overview_popup_window)

        //Update Data
        viewModel.setData(intent.extras)

        val root = window.decorView.rootView as ViewGroup

    }

    private fun setWindowFlag(activity: Activity, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        } else {
            winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        }
    }
}