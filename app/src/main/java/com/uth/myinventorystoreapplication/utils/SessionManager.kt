package com.uth.myinventorystoreapplication.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "pulperia_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
    }

    fun createLoginSession(username: String, fullName: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(): String {
        return prefs.getString(KEY_USERNAME, "admin") ?: "admin"
    }

    fun getFullName(): String {
        return prefs.getString(KEY_FULL_NAME, "Administrador") ?: "Administrador"
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
