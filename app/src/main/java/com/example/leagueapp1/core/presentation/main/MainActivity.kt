package com.example.leagueapp1.core.presentation.main


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
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
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.leagueapp1.R
import com.example.leagueapp1.databinding.ActivityMainBinding
import com.example.leagueapp1.core.presentation.fragment_factory.DefaultFragmentFactoryEntryPoint
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.core.util.exhaustive
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var headerView: View

    private var shouldCheckDrawer: Boolean = true

    @Inject
    lateinit var glide: RequestManager

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {

        val entryPoint = EntryPointAccessors.fromActivity(
            this,
            DefaultFragmentFactoryEntryPoint::class.java
        )

        supportFragmentManager.fragmentFactory = entryPoint.getFragmentFactory()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = binding.myToolbar
        toolbar.title = "Thunder.GG"
        setSupportActionBar(toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        drawerLayout = binding.drawerLayout
        binding.navigationView.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.listChampFragment, R.id.settingsFragment),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navigationView: NavigationView = binding.navigationView
        headerView = navigationView.inflateHeaderView(R.layout.navigation_header)

        val menu = navigationView.menu
        val pickChampionItem = menu.findItem(R.id.listChampFragment)
        pickChampionItem.isVisible = true
        val summonerName = headerView.findViewById<TextView>(R.id.navigationSummonerName)
        val iconImg = headerView.findViewById<ImageView>(R.id.navigationSummonerIcon)
        val splashArt = headerView.findViewById<ImageView>(R.id.navigationSplashArt)

        viewModel.headerInfo.observe(this) { headerInfo ->
            if (headerInfo != null) {
                summonerName.text = headerInfo.name
                val profileIconUrl = "${Constants.PROFILE_ICON_URL}${headerInfo.summonerIconId}.png"
                val splashArtUrl = "${Constants.SPLASH_ART_URL}${headerInfo.splashName}_0.jpg"
                glide.load(profileIconUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iconImg)
                glide.load(splashArtUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(splashArt)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainActivityEvents.collect { event ->
                    when (event) {
                        is MainViewModel.MainActivityEvents.ChangeActionBarTitle -> {
                            supportActionBar?.setTitle(event.name)
                        }
                    }.exhaustive
                }
            }
        }

        window.setFormat(PixelFormat.RGBA_8888)

        drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                //Called when a drawer's position changes.
                if (slideOffset > 0.0f && shouldCheckDrawer) {
                    shouldCheckDrawer = false
                    lifecycleScope.launch(Dispatchers.Main) {
//                        viewModel.collectPreferencesFlow()
//                        pickChampionItem.isVisible = viewModel.isActive
                    }
                }
            }

            override fun onDrawerOpened(drawerView: View) {
                //Called when a drawer has settled in a completely open state.
                //The drawer is interactive at this point.
                // If you have 2 drawers (left and right) you can distinguish
                // them by using id of the drawerView. int id = drawerView.getId();
                // id will be your layout's id: for example R.id.left_drawer
            }

            override fun onDrawerClosed(drawerView: View) {
                // Called when a drawer has settled in a completely closed state.
                shouldCheckDrawer = true
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
            }
        })
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
                            val imm =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

    fun hideUpButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}