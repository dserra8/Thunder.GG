package com.example.leagueapp1.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.leagueapp1.util.DispatcherProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SortOrder {
    BY_NAME,
    BY_MASTERY_POINTS
}

data class FilterPreferences(
    val sortOrder: SortOrder = SortOrder.BY_MASTERY_POINTS,
    val query: String = "",
    var showADC: Boolean = false,
    var showSup: Boolean = false,
    var showMid: Boolean = false,
    var showJungle: Boolean = false,
    var showTop: Boolean = false,
    var showAll: Boolean = true
)


@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext val context: Context,
    private val dispatcher: DispatcherProvider ){

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_preferences",
        scope = CoroutineScope(dispatcher.io)
    )
    val preferencesFlow = context.dataStore.data
        .catch { exception ->
            if(exception is IOException){
                Log.e(TAG, "Error reading preferences: ", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_MASTERY_POINTS.name
            )
            val showAll = preferences[PreferencesKeys.SHOWALL] ?: true
            val showADC = preferences[PreferencesKeys.SHOWADC] ?: false
            val showSup = preferences[PreferencesKeys.SHOWSUP] ?: false
            val showMid = preferences[PreferencesKeys.SHOWMID] ?: false
            val showJungle = preferences[PreferencesKeys.SHOWJUNGLE] ?: false
            val showTop = preferences[PreferencesKeys.SHOWTOP] ?: false
            val query = preferences[PreferencesKeys.QUERY] ?: ""

            FilterPreferences(sortOrder, query, showADC, showSup, showMid, showJungle, showTop, showAll)

        }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name

        }
    }

    suspend fun updateShowRoles(showAll: Boolean, showADC: Boolean, showJungle: Boolean, showTop: Boolean,
                                showMid: Boolean, showSup: Boolean){
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOWALL] = showAll
            preferences[PreferencesKeys.SHOWADC] = showADC
            preferences[PreferencesKeys.SHOWSUP] = showSup
            preferences[PreferencesKeys.SHOWMID] = showMid
            preferences[PreferencesKeys.SHOWJUNGLE] = showJungle
            preferences[PreferencesKeys.SHOWTOP] = showTop
        }
    }

    suspend fun updateQuery(query: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUERY] = query
        }
    }

    private object PreferencesKeys{
        val QUERY = stringPreferencesKey("query")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SHOWALL = booleanPreferencesKey("show_all")
        val SHOWADC = booleanPreferencesKey("show_adc")
        val SHOWSUP = booleanPreferencesKey("show_sup")
        val SHOWMID = booleanPreferencesKey("show_mid")
        val SHOWJUNGLE = booleanPreferencesKey("show_jungle")
        val SHOWTOP = booleanPreferencesKey("show_top")
    }
}