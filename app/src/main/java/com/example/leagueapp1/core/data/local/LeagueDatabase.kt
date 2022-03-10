package com.example.leagueapp1.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.leagueapp1.data.local.ChampionsDao
import com.example.leagueapp1.core.domain.models.Summoner
import com.example.leagueapp1.data.local.SummonersDao
import com.example.leagueapp1.feature_champions.domain.models.Champion

@Database(entities = [Summoner::class, Champion::class], version = 1)
@TypeConverters(Converters::class)
abstract class LeagueDatabase : RoomDatabase() {
    abstract fun summonersDao(): SummonersDao
    abstract fun championsDao(): ChampionsDao
}

