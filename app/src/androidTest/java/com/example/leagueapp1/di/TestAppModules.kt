package com.example.leagueapp1.di

import android.content.Context
import androidx.room.Room
import com.example.leagueapp1.database.LeagueDatabase
import com.example.leagueapp1.network.RiotApiService
import com.example.leagueapp1.repository.FakeRepositoryAndroidTest
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.repository.Repository
import com.example.leagueapp1.util.AndroidTestDispatchers
import com.example.leagueapp1.util.DispatcherProvider
import com.example.leagueapp1.util.StandardDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Named
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestAppModules {

    @Provides
    fun provideInMemoryDb(@ApplicationContext context: Context) =
        Room.inMemoryDatabaseBuilder(context, LeagueDatabase::class.java)
            .allowMainThreadQueries()
            .build()
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {
    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    fun provideTestRepository(
        dispatchers: AndroidTestDispatchers
    ) = FakeRepositoryAndroidTest(dispatchers) as LeagueRepository
}

@Module
@InstallIn(SingletonComponent::class)
object TestDispatcherModule {

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    fun provideTestDispatcherProvider(): AndroidTestDispatchers {
        return AndroidTestDispatchers()
    }
}