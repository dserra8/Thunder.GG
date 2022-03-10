package com.example.leagueapp1.feature_search_summoner.data.repository

import com.example.leagueapp1.data.local.SummonersDao
import com.example.leagueapp1.feature_search_summoner.domain.repository.SearchSummonerRepositoryInterface

class SearchSummonerRepository(
   private val summonersDao: SummonersDao
) : SearchSummonerRepositoryInterface {


}