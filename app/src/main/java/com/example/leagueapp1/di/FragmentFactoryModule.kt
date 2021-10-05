package com.example.leagueapp1.di

import androidx.fragment.app.Fragment
import com.example.leagueapp1.ui.champDetails.ChampScreenFragment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class FragmentFactoryModule {

    @Binds
    @IntoMap
    @FragmentKey(ChampScreenFragment::class)
    abstract fun bindChampScreenFragment(fragment: ChampScreenFragment): Fragment

}

