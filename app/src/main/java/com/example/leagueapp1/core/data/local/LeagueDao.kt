package com.example.leagueapp1.data.local

import androidx.room.*
import com.example.leagueapp1.core.domain.models.Summoner
import com.example.leagueapp1.core.domain.models.update.UpdateEvent
import com.example.leagueapp1.feature_champions.domain.models.Champion
import kotlinx.coroutines.flow.Flow

@Dao
interface SummonersDao {

    @Query("SELECT * FROM summoners")
    fun getAllSummoners(): Flow<List<Summoner>>

    @Query("SELECT * FROM summoners WHERE isMainSummoner=1")
    suspend fun getSummoner(): Summoner?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummoner(summoner: Summoner)

    @Query("DELETE FROM summoners")
    suspend fun deleteAllSummoners()

    @Query("DELETE FROM summoners")
    suspend fun deleteCurrentSummoner()

    @Query("SELECT COUNT(*) FROM summoners WHERE name=:summonerName AND timeReceived >=:time")
    suspend fun isFreshSummoner(summonerName: String, time: Long): Int

    @Query("SELECT * FROM summoners WHERE name=:summonerName")
    suspend fun getSummonerByName(summonerName: String): Summoner?


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
    ): Flow<List<Champion>> =
        when (sortOrder) {
            SortOrder.BY_NAME -> getChampionsByName(id, query, showADC, showSup, showMid, showJungle, showTop, showAll)
            SortOrder.BY_MASTERY_POINTS -> getChampionsByMasteryPoints(id, query, showADC, showSup, showMid, showJungle, showTop, showAll)
        }


    @Query("SELECT * FROM summonerChampions WHERE ((`all`=:showAll AND `all`=1) OR (bottom=:showADC AND bottom=1) OR (utility=:showSup AND utility=1) OR (jungle=:showJungle AND jungle=1) OR (middle=:showMid AND middle=1) OR (top=:showTop AND top=1)) AND summonerId=:id AND champName LIKE :searchQuery || '%' ORDER by champName")
    fun getChampionsByName(
        id: String, searchQuery: String, showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<List<Champion>>

    @Query("SELECT * FROM summonerChampions WHERE ((`all`=:showAll AND `all`=1) OR (bottom=:showADC AND bottom=1) OR (utility=:showSup AND utility=1) OR (jungle=:showJungle AND jungle=1) OR (middle=:showMid AND middle=1) OR (top=:showTop AND top=1)) AND summonerId=:id AND champName LIKE :searchQuery || '%' ORDER by championPoints DESC, championPoints")
    fun getChampionsByMasteryPoints(
        id: String, searchQuery: String, showADC: Boolean,
        showSup: Boolean,
        showMid: Boolean,
        showJungle: Boolean,
        showTop: Boolean,
        showAll: Boolean
    ): Flow<List<Champion>>

    @Query("SELECT * FROM summonerChampions WHERE summonerId=:id ORDER by championPoints DESC, championPoints")
    suspend fun getAllChampions(
        id: String
    ): List<Champion>



    @Query("SELECT * FROM summonerChampions WHERE summonerId=:id ORDER BY championPoints DESC LIMIT 1")
    suspend fun getHighestMasteryChampion(id: String): Champion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChampion(champion: Champion)

    @Query("DELETE FROM summonerChampions")
    suspend fun deleteAllChampions()

    @Query("DELETE FROM summonerChampions WHERE summonerId=:id")
    suspend fun deleteSummonerChampions(id: String)

    @Query( "UPDATE summonerChampions SET lp=:lp, rank=:rank WHERE summonerId=:summonerId AND championId=:champId")
    suspend fun updateChampionRank(summonerId: String, champId: Int, lp: Int, rank: String)

    @Query("UPDATE summonerChampions SET updateEvents=:list WHERE summonerId=:summonerID AND championId=:champId")
    suspend fun updateUpdateEventsList(summonerID: String, champId: Int, list: MutableList<UpdateEvent>)

    @Query("SELECT * FROM summonerChampions WHERE summonerId=:summonerId AND championId=:champId")
    suspend fun getChampion(champId: Int, summonerId: String): Champion?

    @Query("SELECT champName FROM summonerChampions WHERE championId=:champID LIMIT 1")
    suspend fun getChampionName(champID: Int): String

    @Update
    suspend fun updateChampion(champ: Champion)
}
