package dev.contratop.contracounter.ui

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.contratop.contracounter.data.AppColorTheme
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.data.CounterRepository
import dev.contratop.contracounter.data.DeltaEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CounterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CounterRepository(application.applicationContext)

    private val _counters = MutableStateFlow<List<Counter>>(emptyList())
    val counters: StateFlow<List<Counter>> = _counters.asStateFlow()

    private val _colorTheme = MutableStateFlow(AppColorTheme.MATERIAL_3)
    val colorTheme: StateFlow<AppColorTheme> = _colorTheme.asStateFlow()

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
        applyDelta(counterId, isIncrement = true)
    }

    fun decrement(counterId: String) {
        applyDelta(counterId, isIncrement = false)
    }

    private fun applyDelta(counterId: String, isIncrement: Boolean) {
        val currentList = _counters.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == counterId }
        if (index == -1) return

        val counter = currentList[index]
        val step = counter.step
        val delta = if (isIncrement) step else -step
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
}
