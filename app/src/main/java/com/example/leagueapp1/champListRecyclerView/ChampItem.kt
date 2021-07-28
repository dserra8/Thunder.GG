package com.example.leagueapp1.champListRecyclerView

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChampItem(
    val champImageResource: Int,
    val champName: String,
    val id: Int,
    val masteryPoints: Int,
    val rankImageResource: Int,
    ): Parcelable

data class HeaderItem(
    val name: String,
    val summonerIconId: Int,
    val splashName: String
)
