package com.example.leagueapp1.di

import android.app.Application
import androidx.room.Room
import com.example.leagueapp1.data.local.LeagueDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): LeagueDatabase =
        Room.databaseBuilder(
            app,
            LeagueDatabase::class.java,
            "league_database"
        ).fallbackToDestructiveMigration().build()
}
