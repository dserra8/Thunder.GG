package com.example.leagueapp1.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leagueapp1.data.local.Rank
import com.example.leagueapp1.data.local.Status
import com.example.leagueapp1.data.local.SummonerProperties
import java.util.*

val <T> T.exhaustive: T
    get() = this

fun RecyclerView.getCurrentPosition(): Int {
    return (this.layoutManager as? LinearLayoutManager?)?.findFirstVisibleItemPosition() ?: 0
}

fun filterChampionName(name: String?): Pair<String, Boolean> {
    var weirdSyntax = false
    val champName = name?.filter { char ->
        when (char) {
            ' ' -> false
            '&' -> {
                weirdSyntax = true
                false
            }
            39.toChar() -> {
                weirdSyntax = true
                false
            }
            '.' -> false
            else -> true
        }
    } ?: "Akali"
    return Pair(champName, weirdSyntax)
}


fun formatSplashName(id: Int): String {
    var filterPair =
        filterChampionName(Constants.champMap[id])
    var splashName = filterPair.first
    if (filterPair.second) {
        splashName = splashName.lowercase(Locale.ROOT)
        splashName = splashName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
    return when (splashName) {
        "Nunuwillump" -> "Nunu"
        "Wukong" -> "MonkeyKing"
        "LeBlanc" -> "Leblanc"
        else -> splashName
    }
}

fun createSummonerProperties(
    id : String = "0",
    accountId : String = "0",
    puuid : String = "0",
    name : String = "",
    profileIconId : Int = 0,
    revisionDate : Long = 0.0.toLong(),
    summonerLevel : Long = 0.0.toLong(),
    isMainSummoner: Boolean = false,
    timeReceived : Long = 0,
    initBoostCalculated : Boolean = false,
    rank : Rank? = null,
) = SummonerProperties(
    id = id,
    accountId = accountId,
    puuid = puuid,
    name = name,
    profileIconId = profileIconId,
    revisionDate = revisionDate,
    summonerLevel = summonerLevel,
    isMainSummoner = isMainSummoner,
    timeReceived = timeReceived,
    initBoostCalculated = initBoostCalculated,
    rank = rank
)