package com.chrona.mobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chrona.shared.UserPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("chrona_prefs")

@Singleton
class PrefsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val FAVS = stringSetPreferencesKey("favourites")
        val AUTO_ACTIVE = booleanPreferencesKey("auto_active")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val THEME = stringPreferencesKey("theme")
    }

    val prefs: Flow<UserPrefs> = context.dataStore.data.map { p ->
        UserPrefs(
            favouriteIds = p[Keys.FAVS] ?: emptySet(),
            autoSetActive = p[Keys.AUTO_ACTIVE] ?: true,
            wifiOnlyLargeFaces = p[Keys.WIFI_ONLY] ?: false,
            themeMode = p[Keys.THEME] ?: "system"
        )
    }

    suspend fun toggleFavourite(id: String) = context.dataStore.edit { p ->
        val cur = p[Keys.FAVS] ?: emptySet()
        p[Keys.FAVS] = if (id in cur) cur - id else cur + id
    }
    suspend fun setAutoActive(v: Boolean) = context.dataStore.edit { it[Keys.AUTO_ACTIVE] = v }
    suspend fun setWifiOnly(v: Boolean) = context.dataStore.edit { it[Keys.WIFI_ONLY] = v }
    suspend fun setTheme(v: String) = context.dataStore.edit { it[Keys.THEME] = v }
}
