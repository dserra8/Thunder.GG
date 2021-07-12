package com.example.leagueapp1.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.leagueapp1.repository.Repository
import retrofit2.HttpException
import javax.inject.Inject

class RefreshChampionRolesWorker @Inject constructor(
    private val repository: Repository,
    appContext: Context, params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object{
        const val WORK_NAME = "RefreshChampionRolesWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            repository.refreshChampionRates()
            Result.success()
        } catch (e: HttpException){
            Result.retry()
        }
    }
}