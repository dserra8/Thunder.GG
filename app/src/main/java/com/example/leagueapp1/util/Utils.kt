package com.example.leagueapp1.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leagueapp1.database.ChampionMastery
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