package com.example.leagueapp1.util

import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    const val CHAMPION_RATES_URL =
        "http://cdn.merakianalytics.com/riot/lol/resources/latest/en-US/championrates.json"

    const val PROFILE_ICON_URL = "http://ddragon.leagueoflegends.com/cdn/11.16.1/img/profileicon/"

    const val SPLASH_ART_URL = "http://ddragon.leagueoflegends.com/cdn/img/champion/splash/"

    const val KTOR_URL = "http://192.168.0.28:8001"

    const val KTOR_SUMMONER = "/getSummoner"

    const val KTOR_REGISTER = "/register"

    const val KTOR_LOGIN = "/login"

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

    val champMap = hashMapOf<Int?, String?>(
        1 to "Annie ",
        2 to "Olaf ",
        3 to "Galio ",
        4 to "Twisted Fate ",
        5 to "Xin Zhao ",
        6 to "Urgot ",
        7 to "LeBlanc ",
        8 to "Vladimir ",
        9 to "Fiddlesticks ",
        10 to "Kayle ",
        11 to "Master Yi ",
        12 to "Alistar ",
        13 to "Ryze ",
        14 to "Sion ",
        15 to "Sivir ",
        16 to "Soraka ",
        17 to "Teemo ",
        18 to "Tristana ",
        19 to "Warwick ",
        20 to "Nunu & Willump ",
        21 to "Miss Fortune ",
        22 to "Ashe ",
        23 to "Tryndamere ",
        24 to "Jax ",
        25 to "Morgana ",
        26 to "Zilean ",
        27 to "Singed ",
        28 to "Evelynn ",
        29 to "Twitch ",
        30 to "Karthus ",
        31 to "Cho'Gath",
        32 to "Amumu ",
        33 to "Rammus ",
        34 to "Anivia ",
        35 to "Shaco ",
        36 to "Dr. Mundo ",
        37 to "Sona ",
        38 to "Kassadin ",
        39 to "Irelia ",
        40 to "Janna ",
        41 to "Gangplank ",
        42 to "Corki ",
        43 to "Karma ",
        44 to "Taric ",
        45 to "Veigar ",
        48 to "Trundle ",
        50 to "Swain ",
        51 to "Caitlyn ",
        53 to "Blitzcrank ",
        54 to "Malphite ",
        55 to "Katarina ",
        56 to "Nocturne ",
        57 to "Maokai ",
        58 to "Renekton ",
        59 to "Jarvan IV ",
        60 to "Elise ",
        61 to "Orianna ",
        62 to "Wukong ",
        63 to "Brand ",
        64 to "Lee Sin ",
        67 to "Vayne ",
        68 to "Rumble ",
        69 to "Cassiopeia ",
        72 to "Skarner ",
        74 to "Heimerdinger ",
        75 to "Nasus ",
        76 to "Nidalee ",
        77 to "Udyr ",
        78 to "Poppy ",
        79 to "Gragas ",
        80 to "Pantheon ",
        81 to "Ezreal ",
        82 to "Mordekaiser ",
        83 to "Yorick ",
        84 to "Akali ",
        85 to "Kennen ",
        86 to "Garen ",
        89 to "Leona ",
        90 to "Malzahar ",
        91 to "Talon ",
        92 to "Riven ",
        96 to "Kog'Maw",
        98 to "Shen ",
        99 to "Lux ",
        101 to "Xerath ",
        102 to "Shyvana ",
        103 to "Ahri ",
        104 to "Graves ",
        105 to "Fizz ",
        106 to "Volibear ",
        107 to "Rengar ",
        110 to "Varus ",
        111 to "Nautilus ",
        112 to "Viktor ",
        113 to "Sejuani ",
        114 to "Fiora ",
        115 to "Ziggs ",
        117 to "Lulu ",
        119 to "Draven ",
        120 to "Hecarim ",
        121 to "Kha'Zix",
        122 to "Darius ",
        126 to "Jayce ",
        127 to "Lissandra ",
        131 to "Diana ",
        133 to "Quinn ",
        134 to "Syndra ",
        136 to "Aurelion Sol ",
        141 to "Kayn ",
        142 to "Zoe ",
        143 to "Zyra ",
        145 to "Kai'Sa",
        147 to "Seraphine ",
        150 to "Gnar ",
        154 to "Zac ",
        157 to "Yasuo ",
        161 to "Vel'Koz",
        163 to "Taliyah ",
        164 to "Camille ",
        201 to "Braum ",
        202 to "Jhin ",
        203 to "Kindred ",
        222 to "Jinx ",
        223 to "Tahm Kench ",
        234 to "Viego ",
        235 to "Senna ",
        236 to "Lucian ",
        238 to "Zed ",
        240 to "Kled ",
        245 to "Ekko ",
        246 to "Qiyana ",
        254 to "Vi ",
        266 to "Aatrox ",
        267 to "Nami ",
        268 to "Azir ",
        350 to "Yuumi ",
        360 to "Samira ",
        412 to "Thresh ",
        420 to "Illaoi ",
        421 to "Rek'Sai",
        427 to "Ivern ",
        429 to "Kalista ",
        432 to "Bard ",
        497 to "Rakan ",
        498 to "Xayah ",
        516 to "Ornn ",
        517 to "Sylas ",
        518 to "Neeko ",
        523 to "Aphelios ",
        526 to "Rell ",
        555 to "Pyke ",
        777 to "Yone ",
        875 to "Sett ",
        876 to "Lillia ",
        887 to "Gwen "
    )

}