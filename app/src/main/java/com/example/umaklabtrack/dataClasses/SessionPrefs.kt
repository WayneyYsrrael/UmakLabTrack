package com.example.umaklabtrack.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

data class SimpleUser(
    val userId: String?,
    val name: String?,
    val email: String?,
    val cNum: String?
)

object UserPrefsKeys {
    val USER_ID = stringPreferencesKey("user_id")   // ✅ FIXED (was intPreferencesKey)
    val NAME = stringPreferencesKey("name")
    val EMAIL = stringPreferencesKey("email")
    val CNUM = stringPreferencesKey("cnum")
}

class SessionPreferences(private val context: Context) {

    suspend fun saveSession(
        userId: String,
        name: String,
        email: String,
        cNum: String?
    ) {
        context.userPrefs.edit { prefs ->
            prefs[UserPrefsKeys.USER_ID] = userId
            prefs[UserPrefsKeys.NAME] = name
            prefs[UserPrefsKeys.EMAIL] = email
            if (cNum != null) prefs[UserPrefsKeys.CNUM] = cNum
        }
    }

    suspend fun loadSession(): SimpleUser {
        val prefs = context.userPrefs.data.first()
        return SimpleUser(
            userId = prefs[UserPrefsKeys.USER_ID],          // String?
            name = prefs[UserPrefsKeys.NAME],
            email = prefs[UserPrefsKeys.EMAIL],
            cNum = prefs[UserPrefsKeys.CNUM]
        )
    }

    suspend fun clearSession() {
        context.userPrefs.edit { it.clear() }
    }

    suspend fun isLoggedIn(): Boolean {
        val prefs = context.userPrefs.data.first()
        return prefs[UserPrefsKeys.USER_ID] != null
    }

    // Optional helper
    suspend fun getUserId(): String? {
        val prefs = context.userPrefs.data.first()
        return prefs[UserPrefsKeys.USER_ID]
    }
}
