package com.example.leagueapp1.di

import com.example.leagueapp1.core.data.local.LeagueDatabase
import com.example.leagueapp1.core.data.remote.BasicAuthInterceptor
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.domain.use_case.SyncUseCase
import com.example.leagueapp1.core.util.ApiUtil
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.feature_champions.data.remote.ChampApi
import com.example.leagueapp1.feature_champions.data.repository.ChampionsRepository
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.example.leagueapp1.feature_champions.domain.use_case.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChampionModule {

    @Provides
    @Singleton
    fun provideChampApi(
        basicAuthInterceptor: BasicAuthInterceptor
    ): ChampApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.KTOR_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()
            .create(ChampApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChampionsRepository(
        db: LeagueDatabase,
        apiUtil: ApiUtil,
        champApi: ChampApi
    ): ChampionsRepositoryInterface {
        return ChampionsRepository(
            championsDao = db.championsDao(),
            apiUtil = apiUtil,
            champApi = champApi
        )
    }

    @Singleton
    @Provides
    fun provideChampionUseCases(
        getChampionsUseCase: GetChampionsUseCase,
        getHighestMasteryChampUseCase: GetHighestMasteryChampUseCase,
        formatSplashNameUseCase: FormatSplashNameUseCase,
        getChampionUpdatesUseCase: GetChampionUpdatesUseCase,
        updateEventListUseCase: UpdateEventListUseCase
    ): ChampionUseCases = ChampionUseCases(
        getHighestMasteryChampUseCase,
        getChampionsUseCase,
        formatSplashNameUseCase,
        getChampionUpdatesUseCase,
        updateEventListUseCase
    )

    @Provides
    @Singleton
    fun provideGetChampionsUseCase(
        coreRepo: CoreRepositoryInterface,
        champRepo: ChampionsRepositoryInterface,
        syncUseCase: SyncUseCase
    ): GetChampionsUseCase {
        return GetChampionsUseCase(
            coreRepo = coreRepo,
            champRepo = champRepo,
            syncUseCase = syncUseCase
        )
    }

    @Provides
    @Singleton
    fun provideGetHighestMasteryChampionUseCase(
        coreRepo: CoreRepositoryInterface,
        champRepo: ChampionsRepositoryInterface
    ): GetHighestMasteryChampUseCase = GetHighestMasteryChampUseCase(champRepo, coreRepo)

    @Provides
    @Singleton
    fun provideGetChampionUseCase(
        coreRepo: CoreRepositoryInterface,
        champRepo: ChampionsRepositoryInterface,
    ): GetChampionUseCase = GetChampionUseCase(coreRepo, champRepo)

    @Provides
    @Singleton
    fun provideFormatSplashNameUseCase(
        champRepo: ChampionsRepositoryInterface
    ): FormatSplashNameUseCase = FormatSplashNameUseCase(champRepo)

    @Provides
    @Singleton
    fun provideGetChampionUpdatesUseCase(
        champRepo: ChampionsRepositoryInterface,
     //   coreRepo: CoreRepositoryInterface
    ) : GetChampionUpdatesUseCase = GetChampionUpdatesUseCase(champRepo)

    @Provides
    @Singleton
    fun provideUpdateEventListUseCase(
        champRepo: ChampionsRepositoryInterface,
        coreRepo: CoreRepositoryInterface
    ) : UpdateEventListUseCase = UpdateEventListUseCase(champRepo, coreRepo)

}