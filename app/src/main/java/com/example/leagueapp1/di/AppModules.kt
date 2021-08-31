package com.example.leagueapp1.di

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.leagueapp1.R
import com.example.leagueapp1.ui.champDetails.ChampScreenFragment
import com.example.leagueapp1.database.LeagueDatabase
import com.example.leagueapp1.network.RiotApiService
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.repository.Repository
import com.example.leagueapp1.ui.MainActivity
import com.example.leagueapp1.ui.champDetails.IntroChampFragment
import com.example.leagueapp1.ui.home.HomeFragment
import com.example.leagueapp1.ui.listChamp.ListChampFragment
import com.example.leagueapp1.ui.settings.SettingsFragment
import com.example.leagueapp1.util.Constants
import com.example.leagueapp1.util.DispatcherProvider
import com.example.leagueapp1.util.StandardDispatchers
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitInstance {

    @Provides
    @Singleton
    fun retrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun api(retrofit: Retrofit): RiotApiService =
        retrofit.create(RiotApiService::class.java)


    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_error)
    )

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Singleton
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider {
        return StandardDispatchers()
    }
}

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

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideMainRepository(
        api: RiotApiService,
        db: LeagueDatabase,
        dispatchers: DispatcherProvider
    ) = Repository(api, db, dispatchers) as LeagueRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FragmentFactoryModule {

    @Binds
    @IntoMap
    @FragmentKey(ChampScreenFragment::class)
    abstract fun bindChampScreenFragment(fragment: ChampScreenFragment): Fragment

//    @Binds
//    @IntoMap
//    @FragmentKey(ListChampFragment::class)
//    abstract fun bindListScreenFragment(fragment: ListChampFragment): Fragment
//
//    @Binds
//    @IntoMap
//    @FragmentKey(HomeFragment::class)
//    abstract fun bindHomeFragment(fragment: HomeFragment): Fragment
//
//    @Binds
//    @IntoMap
//    @FragmentKey(SettingsFragment::class)
//    abstract fun bindSettingsFragment(fragment: SettingsFragment): Fragment
//
//    @Binds
//    @IntoMap
//    @FragmentKey(IntroChampFragment::class)
//    abstract fun bindIntroChampFragment(fragment: IntroChampFragment): Fragment
}

