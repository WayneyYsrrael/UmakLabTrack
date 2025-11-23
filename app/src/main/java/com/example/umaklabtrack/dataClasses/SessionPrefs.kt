package com.example.umaklabtrack.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

data class SimpleUser(
    val userId: String?,
    val name: String?,
    val email: String?,
    val cNum: String?,
    val role: String?
)

object UserPrefsKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val NAME = stringPreferencesKey("name")
    val EMAIL = stringPreferencesKey("email")
    val CNUM = stringPreferencesKey("cnum")
    val ROLE = stringPreferencesKey("role")

    // ✅ ADDED: Key for the profile picture
    val PROFILE_IMAGE = stringPreferencesKey("profile_image")
}

class SessionPreferences(private val context: Context) {

    // (This assumes you have the extension 'val Context.userPrefs' defined elsewhere in your project)

    suspend fun saveSession(
        userId: String,
        name: String,
        email: String,
        cNum: String?,
        role: String
    ) {
        context.userPrefs.edit { prefs ->
            prefs[UserPrefsKeys.USER_ID] = userId
            prefs[UserPrefsKeys.NAME] = name
            prefs[UserPrefsKeys.EMAIL] = email
            if (cNum != null) prefs[UserPrefsKeys.CNUM] = cNum
            prefs[UserPrefsKeys.ROLE] = role
        }
    }

    suspend fun loadSession(): SimpleUser {
        val prefs = context.userPrefs.data.first()
        return SimpleUser(
            userId = prefs[UserPrefsKeys.USER_ID],
            name = prefs[UserPrefsKeys.NAME],
            email = prefs[UserPrefsKeys.EMAIL],
            cNum = prefs[UserPrefsKeys.CNUM],
            role = prefs[UserPrefsKeys.ROLE]
        )
    }

    suspend fun clearSession() {
        context.userPrefs.edit { it.clear() }
    }

    suspend fun isLoggedIn(): Boolean {
        val prefs = context.userPrefs.data.first()
        return prefs[UserPrefsKeys.USER_ID] != null
    }

    // ✅ ADDED: Function to save the image URI
    suspend fun saveProfileImage(uri: String) {
        context.userPrefs.edit { prefs ->
            prefs[UserPrefsKeys.PROFILE_IMAGE] = uri
        }
    }

    // ✅ ADDED: Function to get the image URI
    suspend fun getProfileImage(): String? {
        val prefs = context.userPrefs.data.first()
        return prefs[UserPrefsKeys.PROFILE_IMAGE]
    }
}