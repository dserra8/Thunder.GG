package com.example.leagueapp1.di

import android.app.Application
import android.content.Context
import com.example.leagueapp1.data.local.LeagueDatabase
import com.example.leagueapp1.data.remote.RiotApiService
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.repository.Repository
import com.example.leagueapp1.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideMainRepository(
        api: RiotApiService,
        db: LeagueDatabase,
        dispatchers: DispatcherProvider,
        context: Application
    ) = Repository(api, db, dispatchers, context) as LeagueRepository
}
