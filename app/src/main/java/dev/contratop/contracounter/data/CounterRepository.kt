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
    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("contracounter_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _countersFlow = MutableStateFlow<List<Counter>>(emptyList())
    val countersFlow: StateFlow<List<Counter>> = _countersFlow.asStateFlow()

    private val _colorThemeFlow = MutableStateFlow(AppColorTheme.MATERIAL_3)
    val colorThemeFlow: StateFlow<AppColorTheme> = _colorThemeFlow.asStateFlow()

    private val _historyFlow = MutableStateFlow<List<CounterHistoryEntry>>(emptyList())
    val historyFlow: StateFlow<List<CounterHistoryEntry>> = _historyFlow.asStateFlow()

    private val _hapticsEnabledFlow = MutableStateFlow(true)
    val hapticsEnabledFlow: StateFlow<Boolean> = _hapticsEnabledFlow.asStateFlow()

    init {
        _countersFlow.value = loadCountersFromDisk()
        _colorThemeFlow.value = loadColorThemeFromDisk()
        _historyFlow.value = loadHistoryFromDisk()
        _hapticsEnabledFlow.value = loadHapticsFromDisk()
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

    private fun loadHistoryFromDisk(): List<CounterHistoryEntry> {
        val json = prefs.getString(KEY_HISTORY, null)
        if (json.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val type = object : TypeToken<List<CounterHistoryEntry>>() {}.type
            gson.fromJson<List<CounterHistoryEntry>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadHapticsFromDisk(): Boolean {
        return prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
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
        try {
            dev.contratop.contracounter.widget.ContraCounterWidgetProvider.updateAllWidgets(appContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCounterLimits(counterId: String, targetValue: Long?, koValue: Long?) {
        val current = _countersFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == counterId }
        if (index != -1) {
            current[index] = current[index].copy(targetValue = targetValue, koValue = koValue)
            saveCounters(current)
        }
    }

    fun addHistoryEntry(entry: CounterHistoryEntry) {
        val current = _historyFlow.value
        val updated = listOf(entry) + current // Más recientes primero
        _historyFlow.value = updated
        val json = gson.toJson(updated)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun addHistoryNote(counterId: String, note: String, currentValue: Long) {
        addHistoryEntry(
            CounterHistoryEntry(
                counterId = counterId,
                delta = 0L,
                resultingValue = currentValue,
                timestamp = System.currentTimeMillis(),
                note = note.trim()
            )
        )
    }

    fun clearHistoryForCounter(counterId: String) {
        val updated = _historyFlow.value.filter { it.counterId != counterId }
        _historyFlow.value = updated
        val json = gson.toJson(updated)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun loadColorTheme(): AppColorTheme {
        return _colorThemeFlow.value
    }

    fun saveColorTheme(theme: AppColorTheme) {
        _colorThemeFlow.value = theme
        prefs.edit().putString(KEY_COLOR_THEME, theme.name).apply()
    }

    fun saveHapticsEnabled(enabled: Boolean) {
        _hapticsEnabledFlow.value = enabled
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_COUNTERS = "counters_json"
        private const val KEY_COLOR_THEME = "color_theme"
        private const val KEY_HISTORY = "counter_history_json"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"

        @Volatile
        private var instance: CounterRepository? = null

        fun getInstance(context: Context): CounterRepository {
            return instance ?: synchronized(this) {
                instance ?: CounterRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
