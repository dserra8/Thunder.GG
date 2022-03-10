package com.example.leagueapp1.di

import android.content.Context
import androidx.room.Room
import com.example.leagueapp1.core.data.local.LeagueDatabase
import com.example.leagueapp1.util.AndroidTestDispatchers
import com.example.leagueapp1.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

//@Module
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [RepositoryModule::class]
//)
//object TestRepositoryModule {
//    @ExperimentalCoroutinesApi
//    @Singleton
//    @Provides
//    fun provideTestRepository(
//        dispatchers: DispatcherProvider,
//        champNamesManager: ChampNamesManager
//    ) = FakeRepositoryAndroidTest(dispatchers, champNamesManager) as LeagueRepository
//}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DispatcherModule::class]
)
object TestDispatcherModule {

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    fun provideTestDispatcherProvider(): DispatcherProvider {
        return AndroidTestDispatchers()
    }
}