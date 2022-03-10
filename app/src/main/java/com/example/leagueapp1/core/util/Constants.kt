package com.example.leagueapp1.core.util

object Constants {
    const val PROFILE_ICON_URL = "http://ddragon.leagueoflegends.com/cdn/12.3.1/img/profileicon/"

    const val SPLASH_ART_URL = "http://ddragon.leagueoflegends.com/cdn/img/champion/splash/"

    const val LOADING_SCREEN_URL = "http://ddragon.leagueoflegends.com/cdn/img/champion/loading/"

    const val KTOR_URL = "http://192.168.0.39:8001"

    const val DATABASE_NAME = "league_db"

    const val ENCRYPTED_SHARED_PREF_NAME = "enc_shared_pref"

    const val KEY_LOGGED_IN_EMAIL = "KEY_LOGGED_IN_EMAIL"

    const val KEY_LOGGED_IN_PASSWORD = "KEY_PASSWORD"

    const val NO_EMAIL = "NO_EMAIL"

    const val NO_PASSWORD = "NO_PASSWORD"

    val IGNORE_AUTH_URLS = listOf("/login", "/register")

    const val MILLI_SECONDS_DAY : Long = 86400000

    enum class Ranks {
        IRON,
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM,
        DIAMOND,
        MASTER,
        GRANDMASTER,
        CHALLENGER
    }

}