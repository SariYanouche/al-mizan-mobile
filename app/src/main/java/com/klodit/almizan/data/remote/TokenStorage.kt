// data/remote/TokenStorage.kt
package com.klodit.almizan.data.remote

import android.content.Context
import androidx.core.content.edit

object TokenStorage {
    private const val PREFS = "auth_prefs"
    private const val KEY_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    fun save(context: Context, token: String, userId: String, refreshToken: String = "") {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, userId)
            if (refreshToken.isNotBlank()) putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    fun getRefreshToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_REFRESH_TOKEN, null)

    fun getToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)

    fun getUserId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USER_ID, null)

    fun isTokenValid(context: Context): Boolean {
        val token = getToken(context) ?: return false
        return try {
            val payload = token.split(".").getOrNull(1) ?: return false
            val padded  = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decoded = android.util.Base64.decode(padded, android.util.Base64.URL_SAFE)
            val json    = org.json.JSONObject(String(decoded))
            val exp     = json.optLong("exp", 0L)
            exp * 1000 > System.currentTimeMillis()
        } catch (e: Exception) { false }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { clear() }
    }
}