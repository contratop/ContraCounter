package dev.contratop.contracounter.ui

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.contratop.contracounter.BuildConfig
import dev.contratop.contracounter.data.AppColorTheme
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.data.CounterRepository
import dev.contratop.contracounter.data.DeltaEntry
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
    private val repository = CounterRepository(application.applicationContext)

    private val _counters = MutableStateFlow<List<Counter>>(emptyList())
    val counters: StateFlow<List<Counter>> = _counters.asStateFlow()

    private val _colorTheme = MutableStateFlow(AppColorTheme.MATERIAL_3)
    val colorTheme: StateFlow<AppColorTheme> = _colorTheme.asStateFlow()

    // Estado del comprobador de actualizaciones de GitHub
    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    // Historial temporal de cambios en los últimos 5 segundos por contador
    private val deltaHistoryMap = mutableMapOf<String, MutableList<DeltaEntry>>()

    // Delta visible actual para cada contador
    val recentDeltas = mutableStateMapOf<String, Long>()

    // Si la insignia shadow está visible en este momento para cada contador
    val isDeltaVisible = mutableStateMapOf<String, Boolean>()

    // Trabajos activos de temporizador para ocultar el shadow tras 3 segundos de inactividad
    private val timerJobs = mutableMapOf<String, Job>()

    init {
        loadData()
    }

    private fun loadData() {
        _counters.value = repository.loadCounters()
        _colorTheme.value = repository.loadColorTheme()
    }

    fun setColorTheme(theme: AppColorTheme) {
        _colorTheme.value = theme
        repository.saveColorTheme(theme)
    }

    fun increment(counterId: String) {
        val counter = _counters.value.firstOrNull { it.id == counterId } ?: return
        applyDelta(counterId, counter.step)
    }

    fun decrement(counterId: String) {
        val counter = _counters.value.firstOrNull { it.id == counterId } ?: return
        applyDelta(counterId, -counter.step)
    }

    fun modifyValue(counterId: String, delta: Long) {
        applyDelta(counterId, delta)
    }

    private fun applyDelta(counterId: String, delta: Long) {
        val currentList = _counters.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == counterId }
        if (index == -1) return

        val counter = currentList[index]
        val updated = counter.copy(currentValue = counter.currentValue + delta)
        currentList[index] = updated
        _counters.value = currentList
        repository.saveCounters(currentList)

        // Registrar en ventana de los últimos 5 segundos
        val now = System.currentTimeMillis()
        val history = deltaHistoryMap.getOrPut(counterId) { mutableListOf() }
        history.add(DeltaEntry(delta = delta, timestamp = now))

        // Eliminar entradas más viejas de 5000 ms (5 segundos)
        val cutoff = now - 5000L
        history.removeAll { it.timestamp < cutoff }

        // Calcular suma neta en la ventana de 5s
        val netDelta = history.sumOf { it.delta }
        recentDeltas[counterId] = netDelta
        isDeltaVisible[counterId] = true

        // Reiniciar el temporizador de visibilidad: debe verse durante 3 segundos tras la última pulsación
        timerJobs[counterId]?.cancel()
        timerJobs[counterId] = viewModelScope.launch {
            delay(3000L) // 3 segundos visible tras la última pulsación
            isDeltaVisible[counterId] = false
            // Limpiamos historial tras desaparecer
            history.clear()
            recentDeltas[counterId] = 0L
        }
    }

    fun setDirectValue(counterId: String, newValue: Long) {
        val currentList = _counters.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == counterId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(currentValue = newValue)
            _counters.value = currentList
            repository.saveCounters(currentList)

            // Limpiar shadow delta para evitar confusión con el cambio directo
            timerJobs[counterId]?.cancel()
            isDeltaVisible[counterId] = false
            deltaHistoryMap[counterId]?.clear()
            recentDeltas[counterId] = 0L
        }
    }

    fun resetCounter(counterId: String) {
        val currentList = _counters.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == counterId }
        if (index != -1) {
            val c = currentList[index]
            currentList[index] = c.copy(currentValue = c.initialValue)
            _counters.value = currentList
            repository.saveCounters(currentList)

            // Limpiar shadow delta
            timerJobs[counterId]?.cancel()
            isDeltaVisible[counterId] = false
            deltaHistoryMap[counterId]?.clear()
            recentDeltas[counterId] = 0L
        }
    }

    fun addCounter(title: String, initialValue: Long, step: Long, colorIndex: Int) {
        val newCounter = Counter(
            title = title,
            currentValue = initialValue,
            initialValue = initialValue,
            step = step,
            colorIndex = colorIndex
        )
        val updated = _counters.value + newCounter
        _counters.value = updated
        repository.saveCounters(updated)
    }

    fun deleteCounter(counterId: String) {
        val updated = _counters.value.filterNot { it.id == counterId }
        _counters.value = updated
        repository.saveCounters(updated)

        timerJobs[counterId]?.cancel()
        timerJobs.remove(counterId)
        deltaHistoryMap.remove(counterId)
        recentDeltas.remove(counterId)
        isDeltaVisible.remove(counterId)
    }

    fun resetAllCounters() {
        val updated = _counters.value.map { it.copy(currentValue = it.initialValue) }
        _counters.value = updated
        repository.saveCounters(updated)

        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
        deltaHistoryMap.clear()
        recentDeltas.clear()
        isDeltaVisible.clear()
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
