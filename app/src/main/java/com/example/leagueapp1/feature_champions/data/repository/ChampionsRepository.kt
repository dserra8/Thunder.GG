package com.example.leagueapp1.feature_champions.data.repository


import com.example.leagueapp1.core.domain.models.SummonerFromKtor
import com.example.leagueapp1.core.domain.models.update.UpdateChampsRanks
import com.example.leagueapp1.core.domain.models.update.UpdateEvent
import com.example.leagueapp1.core.util.ApiUtil
import com.example.leagueapp1.core.util.NoLoadResource
import com.example.leagueapp1.data.local.ChampionsDao
import com.example.leagueapp1.data.local.FilterPreferences
import com.example.leagueapp1.feature_champions.data.remote.ChampApi
import com.example.leagueapp1.feature_champions.domain.models.ChampRankInfo
import com.example.leagueapp1.feature_champions.domain.models.Champion
import com.example.leagueapp1.feature_champions.domain.repository.ChampionsRepositoryInterface

class ChampionsRepository(
    private val championsDao: ChampionsDao,
    private val apiUtil: ApiUtil,
    private val champApi: ChampApi
) : ChampionsRepositoryInterface {


    override fun getChampions(
        filterPreferences: FilterPreferences,
        query: String,
        id: String
    ) = championsDao.getChampions(
        query = query,
        sortOrder = filterPreferences.sortOrder,
        id = id,
        showADC = filterPreferences.showADC,
        showJungle = filterPreferences.showJungle,
        showMid = filterPreferences.showMid,
        showTop = filterPreferences.showTop,
        showSup = filterPreferences.showSup,
        showAll = filterPreferences.showAll
    )

    override suspend fun getHighestMasteryChampion(id: String): Champion? = championsDao.getHighestMasteryChampion(id)

    override suspend fun insertChampions(champs: HashMap<Int,Champion>) {
        champs.forEach {
            championsDao.insertChampion(
                it.value.apply {
                    rankInfo = rankInfo ?: ChampRankInfo()
                }
            )
        }
    }

    override suspend fun getChampion(champId: Int, summonerId: String) = championsDao.getChampion(champId, summonerId)
    //Possible Errors?
    override suspend fun getAllChamps(id: String): List<Champion> = championsDao.getAllChampions(id)

    override suspend fun getChampName(champId: Int) = championsDao.getChampionName(champId)

    override suspend fun updateChampsRanks(): NoLoadResource<UpdateChampsRanks>? {
        val apiResult = apiUtil.safeApiCall { champApi.updateChampRanks() }
        var result: NoLoadResource<UpdateChampsRanks>? = null
        apiResult.onSuccess { update ->
            result = NoLoadResource.Success(update)
        }
        apiResult.onFailure {
            result = NoLoadResource.Error(it)
        }
        return result
    }

    override suspend fun updateUpdateEventList(
        list: MutableList<UpdateEvent>,
        summonerId: String,
        champId: Int
    ) {
        championsDao.updateUpdateEventsList(summonerId, champId, list)
    }

    override suspend fun updateChampion(champ: Champion) = championsDao.updateChampion(champ)
}