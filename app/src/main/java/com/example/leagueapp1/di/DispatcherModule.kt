package com.example.leagueapp1.di

import com.example.leagueapp1.util.DispatcherProvider
import com.example.leagueapp1.util.StandardDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Singleton
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider {
        return StandardDispatchers()
    }
}