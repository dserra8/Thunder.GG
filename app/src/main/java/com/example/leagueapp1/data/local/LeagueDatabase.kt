package com.example.leagueapp1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SummonerProperties::class, ChampionMastery::class, ChampionRoleRates::class], version = 1)
abstract class LeagueDatabase : RoomDatabase() {
    abstract fun summonersDao(): SummonersDao
    abstract fun championsDao(): ChampionsDao
    abstract fun championRoleRatesDao(): ChampionRoleRatesDao
}

