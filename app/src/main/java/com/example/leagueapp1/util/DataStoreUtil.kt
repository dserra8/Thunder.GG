package com.example.leagueapp1.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.leagueapp1.util.Constants.DATA
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@ExperimentalSerializationApi
class DataStoreUtil @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val security: SecurityUtil
) {

    private val securityKeyAlias = "data-store"
    private val bytesToStringSeparator = "|"

    private suspend inline fun <reified T> DataStore<Preferences>.secureEdit(
        value: T,
        crossinline editStore: (MutablePreferences, String) -> Unit
    ) {
        edit {
            val encryptedValue =
                security.encryptData(securityKeyAlias, Json.encodeToString(value))
            editStore.invoke(it, encryptedValue.joinToString(bytesToStringSeparator))
        }
    }

    private val json: Json = Json { encodeDefaults = true }

    private inline fun <reified T> Flow<Preferences>.secureMap(
        crossinline fetchValue: (value: Preferences) -> String
    ): Flow<T> {
        return map { it ->

            val decryptedValue = security.decryptData(
                securityKeyAlias,
                fetchValue(it)
                    .split(bytesToStringSeparator)
                    .map {
                        it.toByteOrNull() ?: "1".toByte()
                    }
                    .toByteArray()
            )
            json.decodeFromString(decryptedValue)
        }
    }

    suspend fun clearDataStore() {
        dataStore.edit {
            it.clear()
        }
    }

    suspend fun hasKey(key: Preferences.Key<*>) = dataStore.edit { it.contains(key) }

    suspend fun setSecuredData(value: String, KEY: Preferences.Key<String>) {
        dataStore.secureEdit(value) { prefs, encryptedValue ->
            prefs[KEY] = encryptedValue
        }
    }

    fun getSecuredData(KEY: Preferences.Key<String>) =
        dataStore.data.secureMap<String> { preferences ->
            preferences[KEY].orEmpty()
        }

    suspend fun setData(value: String) {
        dataStore.edit {
            it[DATA] = value
        }
    }

    fun getData() = dataStore.data
        .map { preferences ->
            preferences[DATA].orEmpty()
        }
}