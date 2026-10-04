package dev.contratop.contracounter.data

import java.util.UUID

/**
 * Representa un contador individual dentro de ContraCounter.
 */
data class Counter(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val currentValue: Long,
    val initialValue: Long = 0,
    val step: Long = 1,
    val colorIndex: Int = 0, // Paleta de acento Material 3
    val createdAt: Long = System.currentTimeMillis(),
    val targetValue: Long? = null, // Meta de victoria (ej. 20, 40, 100)
    val koValue: Long? = null      // Límite de derrota / K.O. (ej. 0)
)

/**
 * Evento de cambio para calcular el shadow delta en la ventana temporal.
 */
data class DeltaEntry(
    val delta: Long,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Registro individual en el historial de un contador.
 * Se guarda tras 3 segundos de inactividad agrupando toda la racha de cambios.
 */
data class CounterHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val counterId: String,
    val delta: Long,              // Puntuación neta sumada o restada en la racha (+5, -3, etc.), o 0 si es anotación
    val resultingValue: Long,     // Valor en el que quedó el contador tras el cambio
    val timestamp: Long = System.currentTimeMillis(), // Momento exacto del commit
    val note: String? = null      // Anotación opcional del usuario
)
