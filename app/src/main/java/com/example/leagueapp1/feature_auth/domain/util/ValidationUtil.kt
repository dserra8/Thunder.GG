package com.example.leagueapp1.feature_auth.domain.util

import com.example.leagueapp1.feature_auth.presentation.util.AuthError
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.FIELD_EMPTY
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.MAX_SUMMONER_LENGTH
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.MIN_PASSWORD_LENGTH
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.MIN_SUMMONER_LENGTH
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.MIN_USERNAME_LENGTH
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.PASSWORD_REQUIREMENT
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.PASS_DONT_MATCH
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.PASS_SHORT
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.SUMM_LONG
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.SUMM_SHORT
import com.example.leagueapp1.feature_auth.presentation.util.AuthErrorConstants.USER_SHORT

object ValidationUtil {

    fun validateSummoner(name: String): String? {
        val trimmedName = name.trim()

        if(trimmedName.length < MIN_SUMMONER_LENGTH) return SUMM_SHORT
        if(trimmedName.length > MAX_SUMMONER_LENGTH) return SUMM_LONG
        if(trimmedName.isBlank()) return FIELD_EMPTY
        return null
    }

    fun validateUsername(username: String): String? {
        val trimmedUsername = username.trim()

        if(trimmedUsername.length < MIN_USERNAME_LENGTH) return USER_SHORT
        if(trimmedUsername.isBlank()) return FIELD_EMPTY
        return null
    }

    fun validatePassword(pass: String, repeatedPass: String): String? {
        val capitalLetters = pass.any { it.isUpperCase() }
        val number = pass.any{ it.isDigit() }

        if(!capitalLetters || !number) return PASSWORD_REQUIREMENT
        if(pass.length < MIN_PASSWORD_LENGTH) return PASS_SHORT
        if(pass.isBlank()) return FIELD_EMPTY
        if(pass != repeatedPass) return PASS_DONT_MATCH
        return null
    }
}