package dev.contratop.contracounter.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class CounterRepository private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("contracounter_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _countersFlow = MutableStateFlow<List<Counter>>(emptyList())
    val countersFlow: StateFlow<List<Counter>> = _countersFlow.asStateFlow()

    private val _colorThemeFlow = MutableStateFlow(AppColorTheme.MATERIAL_3)
    val colorThemeFlow: StateFlow<AppColorTheme> = _colorThemeFlow.asStateFlow()

    init {
        _countersFlow.value = loadCountersFromDisk()
        _colorThemeFlow.value = loadColorThemeFromDisk()
    }

    private fun loadCountersFromDisk(): List<Counter> {
        val json = prefs.getString(KEY_COUNTERS, null)
        if (json.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val type = object : TypeToken<List<Counter>>() {}.type
            gson.fromJson<List<Counter>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadColorThemeFromDisk(): AppColorTheme {
        val themeStr = prefs.getString(KEY_COLOR_THEME, AppColorTheme.MATERIAL_3.name)
        return try {
            AppColorTheme.valueOf(themeStr ?: AppColorTheme.MATERIAL_3.name)
        } catch (e: Exception) {
            AppColorTheme.MATERIAL_3
        }
    }

    fun loadCounters(): List<Counter> {
        return _countersFlow.value
    }

    fun saveCounters(counters: List<Counter>) {
        _countersFlow.value = counters
        val json = gson.toJson(counters)
        prefs.edit().putString(KEY_COUNTERS, json).apply()
    }

    fun loadColorTheme(): AppColorTheme {
        return _colorThemeFlow.value
    }

    fun saveColorTheme(theme: AppColorTheme) {
        _colorThemeFlow.value = theme
        prefs.edit().putString(KEY_COLOR_THEME, theme.name).apply()
    }

    companion object {
        private const val KEY_COUNTERS = "counters_json"
        private const val KEY_COLOR_THEME = "color_theme"

        @Volatile
        private var instance: CounterRepository? = null

        fun getInstance(context: Context): CounterRepository {
            return instance ?: synchronized(this) {
                instance ?: CounterRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
