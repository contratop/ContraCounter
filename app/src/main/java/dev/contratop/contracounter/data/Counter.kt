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
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Evento de cambio para calcular el shadow delta en la ventana temporal.
 */
data class DeltaEntry(
    val delta: Long,
    val timestamp: Long = System.currentTimeMillis()
)
