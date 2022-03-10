package com.example.leagueapp1.feature_auth.presentation.util

object AuthErrorConstants {


    const val MIN_USERNAME_LENGTH = 3

    const val MIN_PASSWORD_LENGTH = 3

    const val MIN_SUMMONER_LENGTH = 3

    const val MAX_SUMMONER_LENGTH = 16

    const val PASS_SHORT = "Password is too short! At least $MIN_PASSWORD_LENGTH characters"

    const val USER_SHORT = "Username is too short! At least $MIN_USERNAME_LENGTH characters"

    const val SUMM_SHORT = "Summoner name is too short! At least $MIN_SUMMONER_LENGTH characters"

    const val SUMM_LONG = "Summoner name is too long! At most $MAX_SUMMONER_LENGTH characters"

    const val PASS_DONT_MATCH = "Passwords do not match!"

    const val FIELD_EMPTY = "A field is empty!"

    const val PASSWORD_REQUIREMENT = "Password needs to have at least one number and capital letter"



}