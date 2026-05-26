package com.kippu.trace.utils

import android.content.Context

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }

    fun themeModeLabel(mode: ThemeMode, context: Context): String = when (mode) {
        ThemeMode.SYSTEM -> context.getString(com.kippu.trace.R.string.follow_system)
        ThemeMode.LIGHT -> context.getString(com.kippu.trace.R.string.light_mode)
        ThemeMode.DARK -> context.getString(com.kippu.trace.R.string.dark_mode)
    }
}
