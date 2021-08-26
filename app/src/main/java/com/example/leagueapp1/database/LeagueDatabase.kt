package com.example.leagueapp1.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.leagueapp1.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [SummonerProperties::class, ChampionMastery::class, ChampionRoleRates::class], version = 1)
abstract class LeagueDatabase : RoomDatabase() {
    abstract fun summonersDao(): SummonersDao
    abstract fun championsDao(): ChampionsDao
    abstract fun championRoleRatesDao(): ChampionRoleRatesDao

    class Callback @Inject constructor(
        private val database: Provider<LeagueDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().summonersDao()
            applicationScope.launch {
                dao.insertSummoner(
                    SummonerProperties(
                        "", "", "", "",
                        0.0, 0.0, 0.0,
                    false,0)
                )
            }
        }
    }


}

