package com.example.leagueapp1.di

import androidx.room.PrimaryKey
import com.example.leagueapp1.core.data.remote.BasicAuthInterceptor
import com.example.leagueapp1.core.domain.repository.CoreRepositoryInterface
import com.example.leagueapp1.core.util.ApiUtil
import com.example.leagueapp1.feature_auth.data.remote.AuthApi
import com.example.leagueapp1.core.util.Constants
import com.example.leagueapp1.feature_auth.data.repository.AuthRepository
import com.example.leagueapp1.feature_auth.domain.repository.AuthRepositoryInterface
import com.example.leagueapp1.feature_auth.domain.use_case.AuthUseCases
import com.example.leagueapp1.feature_auth.domain.use_case.ChangeMainSummonerUseCase
import com.example.leagueapp1.feature_auth.domain.use_case.LoginUseCase
import com.example.leagueapp1.feature_auth.domain.use_case.RegisterUseCase
import com.example.leagueapp1.util.DispatcherProvider
import com.example.leagueapp1.util.StandardDispatchers
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
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApi(
        basicAuthInterceptor: BasicAuthInterceptor
    ): AuthApi {
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
            .create(AuthApi::class.java)
    }

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor() = BasicAuthInterceptor()

    @Provides
    @Singleton
    fun provideApiUtil(dispatchers: DispatcherProvider) = ApiUtil(dispatchers)

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApi,
        apiUtil: ApiUtil
    ): AuthRepositoryInterface {
        return AuthRepository(api, apiUtil)
    }

    @Provides
    @Singleton
    fun provideAuthUseCases(
        authRepository: AuthRepositoryInterface,
        coreRepository: CoreRepositoryInterface
    ): AuthUseCases {
        return AuthUseCases(
            loginUseCase = LoginUseCase(authRepository),
            registerUseCase = RegisterUseCase(authRepository),
            changeMainSummonerUseCase = ChangeMainSummonerUseCase(coreRepository)
        )
    }
}