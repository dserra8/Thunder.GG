package com.example.leagueapp1.network

import android.app.Application
import androidx.room.Room
import com.example.leagueapp1.database.LeagueDatabase
import com.example.leagueapp1.repository.LeagueRepository
import com.example.leagueapp1.repository.Repository
import com.example.leagueapp1.util.Constants.URL
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()


    @Singleton
    @Provides
    fun provideMainRepository(
        api: RiotApiService,
        db: LeagueDatabase
    ) = Repository(api, db) as LeagueRepository

    @Provides
    @Singleton
    fun api(retrofit: Retrofit): RiotApiService =
        retrofit.create(RiotApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(app: Application): LeagueDatabase =
        Room.databaseBuilder(
            app,
            LeagueDatabase::class.java,
            "league_database"
        ).fallbackToDestructiveMigration().build()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope