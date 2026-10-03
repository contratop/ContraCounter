package dev.contratop.contracounter.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Temas visuales internos de ContraCounter.
 */
enum class AppColorTheme(
    val title: String,
    val subtitle: String
) {
    MATERIAL_3(
        title = "Material 3 (Pixel)",
        subtitle = "Diseño nativo de Google con colores dinámicos del sistema"
    ),
    POKE(
        title = "Modo Poke 💕",
        subtitle = "Muy fancy chic, rosita pastel, tonos blush y glamour"
    ),
    CYBERPUNK(
        title = "Cyberpunk ⚡",
        subtitle = "Night City vibes, neón cian y acentos amarillo eléctrico"
    ),
    EMERALD(
        title = "Matcha Esmeralda 🌿",
        subtitle = "Tonos verdes calmados, salvia y estilo botánico moderno"
    )
}

class CounterRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("contracounter_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_COUNTERS = "counters_json"
        private const val KEY_COLOR_THEME = "color_theme"
    }

    fun loadCounters(): List<Counter> {
        val json = prefs.getString(KEY_COUNTERS, null)
        if (json.isNullOrBlank()) {
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

    fun loadColorTheme(): AppColorTheme {
        val themeStr = prefs.getString(KEY_COLOR_THEME, AppColorTheme.MATERIAL_3.name)
        return try {
            AppColorTheme.valueOf(themeStr ?: AppColorTheme.MATERIAL_3.name)
        } catch (e: Exception) {
            AppColorTheme.MATERIAL_3
        }
    }

    fun saveColorTheme(theme: AppColorTheme) {
        prefs.edit().putString(KEY_COLOR_THEME, theme.name).apply()
    }
}
