package com.lmstaskmanager.app.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {

    private const val PREFS_NAME = "lms_secure_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_TOKEN_EXPIRY = "token_expiry"

    private fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveTokens(context: Context, accessToken: String, refreshToken: String, expiryMs: Long) {
        getEncryptedPrefs(context).edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryMs)
            .apply()
    }

    fun getAccessToken(context: Context): String? =
        getEncryptedPrefs(context).getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(context: Context): String? =
        getEncryptedPrefs(context).getString(KEY_REFRESH_TOKEN, null)

    fun isTokenValid(context: Context): Boolean {
        val expiry = getEncryptedPrefs(context).getLong(KEY_TOKEN_EXPIRY, 0L)
        return expiry > System.currentTimeMillis()
    }

    fun clearTokens(context: Context) {
        getEncryptedPrefs(context).edit().clear().apply()
    }
}