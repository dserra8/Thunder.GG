package com.example.leagueapp1

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.leagueapp1.home.HomeFragmentDirections
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.exhaustive
import com.example.leagueapp1.work.RefreshChampionRolesWorker
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var listener: NavController.OnDestinationChangedListener

    private lateinit var headerView: View

    private var isActive: Boolean = false

    @ExperimentalCoroutinesApi
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment, R.id.listChampFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

//        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
//        supportActionBar?.setCustomView(R.layout.action_bar_layout)

        val navigationView: NavigationView = findViewById(R.id.navigationView)
        headerView = navigationView.inflateHeaderView(R.layout.navigation_header)
        val summonerName = headerView.findViewById<TextView>(R.id.navigationSummonerName)
        val iconImg = headerView.findViewById<ImageView>(R.id.navigationSummonerIcon)
        val splashArt = headerView.findViewById<ImageView>(R.id.navigationSplashArt)

        viewModel.headerInfo.observe(this){ headerInfo ->
            if(headerInfo != null){
                viewModel.changeNavigationHeader("Kayle")
                summonerName.text = headerInfo.name
                val profileIconUrl = "${Constants.PROFILE_ICON_URL}${headerInfo.summonerIconId}.png"
                val splashArtUrl = "${Constants.SPLASH_ART_URL}${headerInfo.splashName}_0.jpg"
                Glide.with(iconImg)
                    .load(profileIconUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iconImg)
                Glide.with(splashArt)
                    .load(splashArtUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(splashArt)
            }
        }



        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainActivityEvents.collect { event ->
                    when (event) {
                        is MainViewModel.MainActivityEvents.FirstTime -> {
                            //supportActionBar?.setCustomView(R.layout.action_bar_in_champ_list)
                            supportActionBar?.setTitle(event.name)
                        }
                        is MainViewModel.MainActivityEvents.ChangeActionBarHome -> {
                            supportActionBar?.setTitle("Thunder.GG")
                        }
                        is MainViewModel.MainActivityEvents.ChangeActionBarOther -> {
//                            val customBar = supportActionBar?.customView
//                            val text = customBar?.findViewById<TextView>(R.id.nameInBar)!!
                            supportActionBar?.setTitle(event.name)
                        }
                    }.exhaustive
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            val data = viewModel.preferencesFlow.first()
            isActive = data.isSummonerActive

            if(isActive)
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToListChampFragment())
        }

        delayedInit()

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val ret = super.dispatchTouchEvent(ev)
        ev?.let { event ->
            if (event.action == MotionEvent.ACTION_UP) {
                currentFocus?.let { view ->
                    if (view is EditText) {
                        val touchCoordinates = IntArray(2)
                        view.getLocationOnScreen(touchCoordinates)
                        val x: Float = event.rawX + view.getLeft() - touchCoordinates[0]
                        val y: Float = event.rawY + view.getTop() - touchCoordinates[1]
                        //If the touch position is outside the EditText then we hide the keyboard
                        if (x < view.getLeft() || x >= view.getRight() || y < view.getTop() || y > view.getBottom()) {
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.windowToken, 0)
                            view.clearFocus()
                        }
                    }
                }
            }
        }
        return ret
    }

    override fun onSupportNavigateUp(): Boolean {
      //  val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun delayedInit() {
        applicationScope.launch {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(true)
                .setRequiresDeviceIdle(true)
                .build()

            val repeatingRequest = PeriodicWorkRequestBuilder<RefreshChampionRolesWorker>(
                1,
                TimeUnit.DAYS
            ).setConstraints(constraints).build()

            WorkManager.getInstance().enqueueUniquePeriodicWork(
                RefreshChampionRolesWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                repeatingRequest
            )
        }
    }
}