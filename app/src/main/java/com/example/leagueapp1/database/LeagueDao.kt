package com.example.leagueapp1.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SummonersDao {

    @Query("SELECT * FROM summoners")
    fun getAllSummoners(): Flow<List<SummonerProperties>>

    @Update
    suspend fun update(summoner: SummonerProperties)

    @Query("SELECT * FROM summoners WHERE current=:current")
    fun getSummonerFlow(current: Boolean): Flow<SummonerProperties?>

    @Query("SELECT * FROM summoners WHERE current=1")
    suspend fun getSummoner(): SummonerProperties

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummoner(summoner: SummonerProperties)

    @Update
    suspend fun replaceSummoner(summoner: SummonerProperties)

    @Query("DELETE FROM summoners")
    suspend fun deleteAllSummoners()

    @Query("DELETE FROM summoners WHERE current=1")
    suspend fun deleteCurrentSummoner()

    @Query("SELECT COUNT(*) FROM summoners WHERE name=:summonerName AND timeReceived >=:time")
    suspend fun isFreshSummoner(summonerName: String, time: Long): Int

    @Query("SELECT * FROM summoners WHERE name=:summonerName")
    suspend fun getSummonerByName(summonerName: String): SummonerProperties?


}

@Dao
interface ChampionsDao {
    fun getChampions(
        query: String,
        sortOrder: SortOrder,
        id: String,
        showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<List<ChampionMastery>> =
        when (sortOrder) {
            SortOrder.BY_NAME -> getChampionsByName(id, query, showADC, showSup, showMid, showJungle, showTop, showAll)
            SortOrder.BY_MASTERY_POINTS -> getChampionsByMasteryPoints(id, query, showADC, showSup, showMid, showJungle, showTop, showAll)
        }


    @Query("SELECT * FROM summonerChampions WHERE ((`ALL`=:showAll AND `ALL`=1) OR (BOTTOM=:showADC AND BOTTOM=1) OR (UTILITY=:showSup AND UTILITY=1) OR (JUNGLE=:showJungle AND JUNGLE=1) OR (MIDDLE=:showMid AND MIDDLE=1) OR (TOP=:showTop AND TOP=1)) AND summonerId=:id AND champName LIKE :searchQuery || '%' ORDER by champName")
    fun getChampionsByName(
        id: String, searchQuery: String, showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<List<ChampionMastery>>

    @Query("SELECT * FROM summonerChampions WHERE ((`ALL`=:showAll AND `ALL`=1) OR (BOTTOM=:showADC AND BOTTOM=1) OR (UTILITY=:showSup AND UTILITY=1) OR (JUNGLE=:showJungle AND JUNGLE=1) OR (MIDDLE=:showMid AND MIDDLE=1) OR (TOP=:showTop AND TOP=1)) AND summonerId=:id AND champName LIKE :searchQuery || '%' ORDER by championPoints DESC, championPoints")
    fun getChampionsByMasteryPoints(
        id: String, searchQuery: String, showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<List<ChampionMastery>>

    @Query("SELECT * FROM summonerChampions WHERE summonerId=:id ORDER BY championPoints DESC LIMIT 1")
    suspend fun getHighestMasteryChampion(id: String): ChampionMastery?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChampion(champion: ChampionMastery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChampionList(championList: List<ChampionMastery>)

    @Query("DELETE FROM summonerChampions")
    suspend fun deleteAllChampions()

    @Query("DELETE FROM summonerChampions WHERE summonerId=:id")
    suspend fun deleteSummonerChampions(id: String)

    @Query("SELECT COUNT(*) FROM summonerChampions WHERE summonerId=:summonerId AND timeReceived >=:time")
    suspend fun isFreshSummonerChampions(summonerId: String, time: Long): Int

    @Query("UPDATE summonerChampions SET recentBoost=:boost WHERE summonerId=:summonerId AND championId=:champId")
    suspend fun updateChampionRecentBoost(summonerId: String, champId: Int, boost: Int)

    @Query("UPDATE summonerChampions SET experienceBoost=:boost WHERE summonerId=:summonerId AND championId=:champId")
    suspend fun updateChampionExperienceBoost(summonerId: String, champId: Int, boost: Int)

    @Query( "UPDATE summonerChampions SET lp=:lp, rank=:rank WHERE summonerId=:summonerId AND championId=:champId")
    suspend fun updateChampionRank(summonerId: String, champId: Int, lp: Int, rank: String)

    @Update
    suspend fun updateChampion(champion: ChampionMastery)

    @Query("SELECT * FROM summonerChampions WHERE summonerId=:summonerId AND championId=:champId")
    suspend fun getChampion(champId: Int, summonerId: String): ChampionMastery

    @Query("SELECT * FROM summonerChampions WHERE summonerId=:summonerId AND championId=:champId")
    fun getChampionFlow(champId: Int, summonerId: String): Flow<ChampionMastery>

}

@Dao
interface ChampionRoleRatesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: List<ChampionRoleRates>)

    @Query("SELECT * FROM trueChampionRoles")
    fun getList(): LiveData<List<ChampionRoleRates>>

    @Query("SELECT * FROM trueChampionRoles WHERE id=:id")
    suspend fun getChampRole(id: Int): ChampionRoleRates?
}