package com.example.leagueapp1.feature_champions.util

fun cleanChampionName(name: String?): Pair<String, Boolean> {
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