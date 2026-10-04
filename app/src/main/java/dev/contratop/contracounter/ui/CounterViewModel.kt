package dev.contratop.contracounter.ui

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.contratop.contracounter.BuildConfig
import dev.contratop.contracounter.data.AppColorTheme
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.data.CounterHistoryEntry
import dev.contratop.contracounter.data.CounterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateStatus {
    object Idle : UpdateStatus
    object Checking : UpdateStatus
    data class Available(val version: String, val releaseNotes: String, val downloadUrl: String) : UpdateStatus
    object UpToDate : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}

class CounterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CounterRepository.getInstance(application.applicationContext)

    val counters: StateFlow<List<Counter>> = repository.countersFlow
    val colorTheme: StateFlow<AppColorTheme> = repository.colorThemeFlow
    val historyFlow: StateFlow<List<CounterHistoryEntry>> = repository.historyFlow
    val hapticsEnabled: StateFlow<Boolean> = repository.hapticsEnabledFlow

    // Estado del comprobador de actualizaciones de GitHub
    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    // Acumulado neto de la racha actual de modificaciones por contador
    private val activeStreakDeltaMap = mutableMapOf<String, Long>()

    // Delta visible en vivo para cada contador
    val recentDeltas = mutableStateMapOf<String, Long>()

    // Si la insignia shadow delta está visible en este momento
    val isDeltaVisible = mutableStateMapOf<String, Boolean>()

    // Trabajos activos del temporizador de inactividad de 3 segundos
    private val timerJobs = mutableMapOf<String, Job>()

    fun setColorTheme(theme: AppColorTheme) {
        repository.saveColorTheme(theme)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        repository.saveHapticsEnabled(enabled)
    }

    fun addHistoryNote(counterId: String, note: String) {
        val trimmed = note.trim()
        if (trimmed.isEmpty()) return
        // Si hay una racha en curso, la consolidamos antes de registrar la nota
        commitStreakToHistory(counterId)
        timerJobs[counterId]?.cancel()

        val currentCounter = repository.countersFlow.value.firstOrNull { it.id == counterId }
        val currentValue = currentCounter?.currentValue ?: 0L
        repository.addHistoryNote(counterId, trimmed, currentValue)
    }

    fun clearHistory(counterId: String) {
        repository.clearHistoryForCounter(counterId)
    }

    fun increment(counterId: String) {
        val counter = repository.countersFlow.value.firstOrNull { it.id == counterId } ?: return
        applyDelta(counterId, counter.step)
    }

    fun decrement(counterId: String) {
        val counter = repository.countersFlow.value.firstOrNull { it.id == counterId } ?: return
        applyDelta(counterId, -counter.step)
    }

    fun modifyValue(counterId: String, delta: Long) {
        applyDelta(counterId, delta)
    }

    /**
     * Aplica un cambio numérico. La racha se mantiene acumulando sin importar cuánto tiempo
     * esté el usuario pulsando botones; solo al pasar 3 segundos completos de inactividad
     * se consolida y se guarda en el historial persistente.
     */
    private fun applyDelta(counterId: String, delta: Long) {
        val currentList = repository.countersFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == counterId }
        if (index == -1) return

        val counter = currentList[index]
        val updated = counter.copy(currentValue = counter.currentValue + delta)
        currentList[index] = updated
        repository.saveCounters(currentList)

        // Acumular delta en la racha activa (mantiene la suma sin corte de 5 segundos mientras se pulse)
        val currentStreak = (activeStreakDeltaMap[counterId] ?: 0L) + delta
        activeStreakDeltaMap[counterId] = currentStreak
        recentDeltas[counterId] = currentStreak
        isDeltaVisible[counterId] = true

        // Reiniciar el temporizador de inactividad: 3 segundos completos desde la ÚLTIMA pulsación
        timerJobs[counterId]?.cancel()
        timerJobs[counterId] = viewModelScope.launch {
            delay(3000L) // 3 segundos sin tocar nada
            commitStreakToHistory(counterId)
        }
    }

    /**
     * Guarda la racha acumulada en el historial persistente y limpia el shadow badge.
     */
    fun commitStreakToHistory(counterId: String) {
        val finalStreak = activeStreakDeltaMap[counterId] ?: 0L
        if (finalStreak != 0L) {
            val currentCounter = repository.countersFlow.value.firstOrNull { it.id == counterId }
            val resultingValue = currentCounter?.currentValue ?: 0L
            repository.addHistoryEntry(
                CounterHistoryEntry(
                    counterId = counterId,
                    delta = finalStreak,
                    resultingValue = resultingValue,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        activeStreakDeltaMap[counterId] = 0L
        recentDeltas[counterId] = 0L
        isDeltaVisible[counterId] = false
    }

    fun setDirectValue(counterId: String, newValue: Long) {
        commitStreakToHistory(counterId)
        timerJobs[counterId]?.cancel()

        val currentList = repository.countersFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == counterId }
        if (index != -1) {
            val previousValue = currentList[index].currentValue
            val diff = newValue - previousValue
            currentList[index] = currentList[index].copy(currentValue = newValue)
            repository.saveCounters(currentList)

            if (diff != 0L) {
                repository.addHistoryEntry(
                    CounterHistoryEntry(
                        counterId = counterId,
                        delta = diff,
                        resultingValue = newValue,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun resetCounter(counterId: String) {
        commitStreakToHistory(counterId)
        timerJobs[counterId]?.cancel()

        val currentList = repository.countersFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == counterId }
        if (index != -1) {
            val c = currentList[index]
            val previousValue = c.currentValue
            val diff = c.initialValue - previousValue
            currentList[index] = c.copy(currentValue = c.initialValue)
            repository.saveCounters(currentList)

            if (diff != 0L) {
                repository.addHistoryEntry(
                    CounterHistoryEntry(
                        counterId = counterId,
                        delta = diff,
                        resultingValue = c.initialValue,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun addCounter(
        title: String,
        initialValue: Long,
        step: Long,
        colorIndex: Int,
        targetValue: Long? = null,
        koValue: Long? = null
    ) {
        val newCounter = Counter(
            title = title,
            currentValue = initialValue,
            initialValue = initialValue,
            step = step,
            colorIndex = colorIndex,
            targetValue = targetValue,
            koValue = koValue
        )
        val updated = repository.countersFlow.value + newCounter
        repository.saveCounters(updated)
    }

    fun updateCounterLimits(counterId: String, targetValue: Long?, koValue: Long?) {
        repository.updateCounterLimits(counterId, targetValue, koValue)
    }

    fun deleteCounter(counterId: String) {
        val updated = repository.countersFlow.value.filterNot { it.id == counterId }
        repository.saveCounters(updated)

        timerJobs[counterId]?.cancel()
        timerJobs.remove(counterId)
        activeStreakDeltaMap.remove(counterId)
        recentDeltas.remove(counterId)
        isDeltaVisible.remove(counterId)
        repository.clearHistoryForCounter(counterId)
    }

    fun resetAllCounters() {
        val updated = repository.countersFlow.value.map { it.copy(currentValue = it.initialValue) }
        repository.saveCounters(updated)

        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
        activeStreakDeltaMap.clear()
        recentDeltas.clear()
        isDeltaVisible.clear()
    }

    override fun onCleared() {
        super.onCleared()
        activeStreakDeltaMap.keys.toList().forEach { counterId ->
            commitStreakToHistory(counterId)
        }
    }

    /**
     * Comprueba si existe una release más reciente en GitHub.
     */
    fun checkForUpdates() {
        _updateStatus.value = UpdateStatus.Checking
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/contratop/ContraCounter/releases/latest")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "ContraCounter-Android")
                    connectTimeout = 7000
                    readTimeout = 7000
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val tagName = json.optString("tag_name", "").trim()
                    val body = json.optString("body", "")
                    val htmlUrl = json.optString("html_url", "https://github.com/contratop/ContraCounter/releases")

                    val currentVersion = BuildConfig.VERSION_NAME
                    val isNewer = isNewerVersion(tagName, currentVersion)

                    withContext(Dispatchers.Main) {
                        if (isNewer) {
                            _updateStatus.value = UpdateStatus.Available(
                                version = tagName,
                                releaseNotes = body,
                                downloadUrl = htmlUrl
                            )
                        } else {
                            _updateStatus.value = UpdateStatus.UpToDate
                        }
                    }
                } else if (responseCode == 404) {
                    // Aún no hay ninguna release pública en el repositorio de GitHub
                    withContext(Dispatchers.Main) {
                        _updateStatus.value = UpdateStatus.UpToDate
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _updateStatus.value = UpdateStatus.Error("Servidor respondió con código $responseCode")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _updateStatus.value = UpdateStatus.Error(e.localizedMessage ?: "Fallo de conexión")
                }
            }
        }
    }

    private fun parseVersion(version: String): List<Int> {
        return version.removePrefix("v")
            .split(".", "-")
            .mapNotNull { part ->
                part.filter { it.isDigit() }.toIntOrNull()
            }
    }

    private fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
        val rParts = parseVersion(remoteTag)
        val cParts = parseVersion(currentVersion)
        if (rParts.isEmpty() || cParts.isEmpty()) {
            return remoteTag.removePrefix("v") > currentVersion.removePrefix("v")
        }
        val maxLen = maxOf(rParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
