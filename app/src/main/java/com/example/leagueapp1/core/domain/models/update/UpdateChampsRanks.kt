package com.example.leagueapp1.core.domain.models.update

data class UpdateChampsRanks(
    val newUpdate: Boolean = false,
    val champs: HashMap<Int, UpdateChamp> = hashMapOf(),
    val errorMessage: String? = null
)
