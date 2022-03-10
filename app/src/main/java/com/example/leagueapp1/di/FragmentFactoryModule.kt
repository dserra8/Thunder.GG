package com.example.leagueapp1.di

import androidx.fragment.app.Fragment
import com.example.leagueapp1.feature_champions.presentation.champ_profile.ChampScreenFragment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
@InstallIn(SingletonComponent::class)
abstract class FragmentFactoryModule {

    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @FragmentKey(ChampScreenFragment::class)
    abstract fun bindChampScreenFragment(fragment: ChampScreenFragment): Fragment

}

