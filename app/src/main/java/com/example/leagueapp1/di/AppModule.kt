package com.example.leagueapp1.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.leagueapp1.R
import com.example.leagueapp1.core.data.local.LeagueDatabase
import com.example.leagueapp1.core.data.remote.BasicAuthInterceptor
import com.example.leagueapp1.core.data.remote.CoreApi
import com.example.leagueapp1.core.data.repository.CoreRepository
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.domain.use_case.*
import com.example.leagueapp1.core.util.ApiUtil
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface
import com.example.leagueapp1.feature_champions.domain.use_case.GetChampionUpdatesUseCase
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCoreApi(
        basicAuthInterceptor: BasicAuthInterceptor
    ): CoreApi {
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
            .create(CoreApi::class.java)
    }


    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        context: Application
    ): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            Constants.ENCRYPTED_SHARED_PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_error)
    )

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideCoreRepository(
        coreApi: CoreApi,
        apiUtil: ApiUtil,
        context: Application,
        db: LeagueDatabase
    ): CoreRepositoryInterface {
        return CoreRepository(
            apiUtil = apiUtil,
            summonersDao = db.summonersDao(),
            coreApi = coreApi,
            context = context
        )
    }

    @Provides
    @Singleton
    fun provideCoreUseCases(
        syncUseCase: SyncUseCase,
        insertSummonerAndChampionsUseCase: InsertSummonerAndChampionsUseCase,
        getHeaderInfoUseCase: GetHeaderInfoUseCase
    ): CoreUseCases {
        return CoreUseCases(
            insertSummonerAndChampionsUseCase = insertSummonerAndChampionsUseCase,
            syncUseCase = syncUseCase,
            getHeaderInfoUseCase = getHeaderInfoUseCase
        )
    }

    @Provides
    @Singleton
    fun provideSyncUseCase(
        coreRepo: CoreRepositoryInterface,
        champRepo: ChampionsRepositoryInterface,
        insertSummonerAndChampionsUseCase: InsertSummonerAndChampionsUseCase,
        getChampionUpdatesUseCase: GetChampionUpdatesUseCase
    ): SyncUseCase {
        return SyncUseCase(
            coreRepo = coreRepo,
            champRepo = champRepo,
            insertSummonerAndChampionsUseCase = insertSummonerAndChampionsUseCase,
            getChampionUpdatesUseCase = getChampionUpdatesUseCase
        )
    }


    @Provides
    @Singleton
    fun provideGetHeaderInfoUseCase(repository: CoreRepositoryInterface): GetHeaderInfoUseCase {
        return GetHeaderInfoUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideInsertSummonerAndChampionsUseCase(
        coreRepo: CoreRepositoryInterface,
        champRepo: ChampionsRepositoryInterface
    ): InsertSummonerAndChampionsUseCase {
        return InsertSummonerAndChampionsUseCase(coreRepo, champRepo)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope