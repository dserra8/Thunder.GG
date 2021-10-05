package com.leagueapp1.data.responses

import com.example.leagueapp1.data.local.SummonerProperties
import com.example.leagueapp1.data.remote.requests.SummonerFromKtor

data class RegisterResponse(
        val summoner: SummonerFromKtor?,
        val successful: Boolean,
        val message: String
)
