package com.example.leagueapp1.di

import com.example.leagueapp1.data.remote.BasicAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthenticatorModule {

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor() = BasicAuthInterceptor()
}