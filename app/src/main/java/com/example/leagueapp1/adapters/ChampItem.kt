package com.example.leagueapp1.adapters

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChampItem(
    val champImageResource: Int,
    val champName: String,
    val id: Int,
    val masteryPoints: Int,
    val rankImageResource: Int,
    val formattedName: String? = null,
    val isUpdate: Boolean = false
    ): Parcelable

data class HeaderItem(
    val name: String,
    val summonerIconId: Int,
    val splashName: String
)
