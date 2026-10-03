package dev.contratop.contracounter.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class CounterRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("contracounter_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_COUNTERS = "counters_json"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }

    fun loadCounters(): List<Counter> {
        val json = prefs.getString(KEY_COUNTERS, null)
        if (json.isNullOrBlank()) {
            // Valores por defecto iniciales inspirados en la captura del usuario
            val defaults = listOf(
                Counter(title = "VIDA", currentValue = 20, initialValue = 20, step = 1, colorIndex = 1),
                Counter(title = "CONTADOR", currentValue = 0, initialValue = 0, step = 1, colorIndex = 0),
                Counter(title = "EJEMPLO", currentValue = 0, initialValue = 0, step = 1, colorIndex = 2)
            )
            saveCounters(defaults)
            return defaults
        }

        return try {
            val type = object : TypeToken<List<Counter>>() {}.type
            gson.fromJson<List<Counter>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCounters(counters: List<Counter>) {
        val json = gson.toJson(counters)
        prefs.edit().putString(KEY_COUNTERS, json).apply()
    }

    fun loadThemeMode(): AppThemeMode {
        val modeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(modeStr ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun saveThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun isDynamicColorEnabled(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }
}
