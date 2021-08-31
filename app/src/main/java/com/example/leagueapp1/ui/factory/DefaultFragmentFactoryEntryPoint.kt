package com.example.leagueapp1.ui.factory

import com.example.leagueapp1.ui.DefaultFragmentFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface DefaultFragmentFactoryEntryPoint {
    fun getFragmentFactory(): DefaultFragmentFactory
}