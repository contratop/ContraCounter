package dev.contratop.contracounter.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.FileProvider
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.data.CounterHistoryEntry
import dev.contratop.contracounter.ui.theme.CounterAccents
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla que visualiza el historial de modificaciones del contador, agrupado por día,
 * con la puntuación sumada/restada y la hora exacta (HH:mm:ss).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    counter: Counter,
    history: List<CounterHistoryEntry>,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val accent = CounterAccents.getOrElse(counter.colorIndex) { CounterAccents[0] }

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Agrupar entradas por día (LocalDate), ordenados de más reciente a más antiguo
    val groupedEntries = remember(history) {
        history
            .groupBy { entry ->
                Instant.ofEpochMilli(entry.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .toSortedMap(compareByDescending { it })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(accent.iconTint)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Historial",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = counter.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        // Botón Exportar / Compartir Historial
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showExportDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Compartir o exportar historial",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Botón Borrar Historial
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showClearConfirmDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteSweep,
                                contentDescription = "Borrar historial",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (history.isEmpty()) {
                // ESTADO VACÍO ELEGANTE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Sin cambios registrados",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cuando modifiques la puntuación del contador y pasen 3 segundos sin tocar la pantalla, la suma o resta acumulada se guardará aquí automáticamente. También puedes añadir notas directas en cualquier momento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                // LISTADO DE ENTRADAS AGRUPADAS POR DÍA
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedEntries.forEach { (date, dayEntries) ->
                        // CABECERA DEL DÍA
                        item(key = "header_${date}") {
                            DayHeaderItem(
                                date = date,
                                entries = dayEntries,
                                isDark = isDark
                            )
                        }

                        // REGISTROS INDIVIDUALES DEL DÍA
                        items(
                            items = dayEntries,
                            key = { it.id }
                        ) { entry ->
                            HistoryEntryCard(
                                entry = entry,
                                isDark = isDark
                            )
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN PARA BORRAR HISTORIAL
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Borrar historial",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Seguro que quieres eliminar todos los registros del historial de \"${counter.title}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showClearConfirmDialog = false
                        onClearHistory()
                    }
                ) {
                    Text(
                        text = "Borrar",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // DIÁLOGO: EXPORTAR O COMPARTIR HISTORIAL
    if (showExportDialog) {
        ExportHistoryDialog(
            counter = counter,
            history = history,
            onDismiss = { showExportDialog = false }
        )
    }
}

/**
 * Cabecera que representa un día específico con la fecha formateada y el neto del día.
 */
@Composable
private fun DayHeaderItem(
    date: LocalDate,
    entries: List<CounterHistoryEntry>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val yearFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))

    val formattedDay = when (date) {
        today -> "Hoy — ${date.format(dayFormatter).replaceFirstChar { it.uppercase() }}"
        yesterday -> "Ayer — ${date.format(dayFormatter).replaceFirstChar { it.uppercase() }}"
        else -> if (date.year == today.year) {
            date.format(dayFormatter).replaceFirstChar { it.uppercase() }
        } else {
            date.format(yearFormatter).replaceFirstChar { it.uppercase() }
        }
    }

    val dayNetSum = entries.sumOf { it.delta }
    val isNetPositive = dayNetSum > 0
    val netSign = if (isNetPositive) "+" else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formattedDay,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Resumen neto del día
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (dayNetSum == 0L) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else if (isNetPositive) {
                if (isDark) Color(0xFF1B3B22) else Color(0xFFE8F5E9)
            } else {
                if (isDark) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else Color(0xFFFFEBEE)
            },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "Neto: $netSign$dayNetSum",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (dayNetSum == 0L) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else if (isNetPositive) {
                    if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                } else {
                    if (isDark) MaterialTheme.colorScheme.error else Color(0xFFC62828)
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Tarjeta de cada evento de modificación: muestra la puntuación sumada o restada,
 * la hora con hora:minuto:segundo, anotaciones de texto si las hay, y el valor final.
 */
@Composable
private fun HistoryEntryCard(
    entry: CounterHistoryEntry,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val hasNote = !entry.note.isNullOrBlank()
    val isPureNote = entry.delta == 0L && hasNote
    val isPositive = entry.delta > 0
    val sign = if (isPositive) "+" else ""

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val timeStr = Instant.ofEpochMilli(entry.timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(timeFormatter)

    val deltaBg = if (isPureNote) {
        if (isDark) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
    } else if (isPositive) {
        if (isDark) Color(0xFF1B3B22) else Color(0xFFE8F5E9)
    } else {
        if (isDark) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else Color(0xFFFFEBEE)
    }

    val deltaTextColor = if (isPureNote) {
        MaterialTheme.colorScheme.primary
    } else if (isPositive) {
        if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
    } else {
        if (isDark) MaterialTheme.colorScheme.error else Color(0xFFC62828)
    }

    val cardBg = if (isDark) Color(0xFF211F26) else MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) Color(0xFF33313B) else Color(0xFFE0E5E0)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // BLOQUE IZQUIERDO: Pill con delta o icono de nota
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = deltaBg,
                modifier = Modifier
                    .width(76.dp)
                    .height(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isPureNote) {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = "Anotación",
                            tint = deltaTextColor,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            text = "$sign${entry.delta}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            ),
                            color = deltaTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // BLOQUE CENTRAL: Hora, Anotación y Descripción
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isPureNote) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Anotación",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (hasNote) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = entry.note.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isPureNote) FontWeight.SemiBold else FontWeight.Normal,
                            fontStyle = if (!isPureNote) FontStyle.Italic else FontStyle.Normal
                        ),
                        color = if (isPureNote) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPureNote) "Valor en este momento:" else "Resultado tras el cambio:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // BLOQUE DERECHO: Puntuación resultante acumulada
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "${entry.resultingValue}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Diálogo modal para elegir entre exportar como texto o como archivo CSV.
 */
@Composable
private fun ExportHistoryDialog(
    counter: Counter,
    history: List<CounterHistoryEntry>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Exportar Historial",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Elige cómo deseas exportar los registros de \"${counter.title}\":",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Opción 1: Resumen de texto
                Surface(
                    onClick = {
                        exportHistoryAsText(context, counter, history)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF232128) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Resumen de texto plano",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ideal para WhatsApp, Telegram, correo o guardar en notas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Opción 2: Archivo CSV
                Surface(
                    onClick = {
                        exportHistoryAsCsv(context, counter, history)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF232128) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF1B3B22) else Color(0xFFE8F5E9),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.TableChart,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hoja de cálculo (.CSV)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Fichero estructurado para abrir en Excel o Google Sheets.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Comparte el historial como texto estructurado y legible.
 */
private fun exportHistoryAsText(
    context: Context,
    counter: Counter,
    history: List<CounterHistoryEntry>
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())

    val sb = StringBuilder()
    sb.appendLine("📊 Historial de ContraCounter: ${counter.title.uppercase()}")
    sb.appendLine("• Puntuación actual: ${counter.currentValue}")
    sb.appendLine("• Valor inicial: ${counter.initialValue}")
    if (counter.targetValue != null) sb.appendLine("• 🏆 Meta de Victoria: ${counter.targetValue}")
    if (counter.koValue != null) sb.appendLine("• 💀 Límite de K.O.: ${counter.koValue}")
    sb.appendLine("• Total de registros: ${history.size}")
    sb.appendLine()

    val grouped = history.groupBy { entry ->
        Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSortedMap(compareByDescending { it })

    for ((date, entries) in grouped) {
        sb.appendLine("📅 ${date.format(dateFormatter)}:")
        for (entry in entries.sortedByDescending { it.timestamp }) {
            val timeStr = Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault()).format(timeFormatter)
            if (entry.delta == 0L && !entry.note.isNullOrBlank()) {
                sb.appendLine("  [$timeStr] 📝 Anotación: \"${entry.note}\" (Valor: ${entry.resultingValue})")
            } else {
                val deltaStr = if (entry.delta > 0) "+${entry.delta}" else "${entry.delta}"
                val noteSuffix = if (!entry.note.isNullOrBlank()) " - \"${entry.note}\"" else ""
                sb.appendLine("  [$timeStr] $deltaStr -> Total: ${entry.resultingValue}$noteSuffix")
            }
        }
        sb.appendLine()
    }
    sb.appendLine("Generado con ContraCounter")

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Historial de ${counter.title}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(sendIntent, "Compartir historial de ${counter.title}"))
}

/**
 * Genera un archivo CSV en cacheDir y lo comparte mediante FileProvider.
 */
private fun exportHistoryAsCsv(
    context: Context,
    counter: Counter,
    history: List<CounterHistoryEntry>
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())

    val sb = StringBuilder()
    sb.appendLine("Timestamp,Fecha,Hora,Tipo,Delta,Valor_Resultante,Anotacion")

    val sorted = history.sortedBy { it.timestamp }
    for (entry in sorted) {
        val zdt = Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault())
        val dateStr = zdt.format(dateFormatter)
        val timeStr = zdt.format(timeFormatter)
        val type = if (entry.delta == 0L) "Anotacion" else if (entry.delta > 0) "Suma" else "Resta"
        val deltaStr = if (entry.delta > 0) "+${entry.delta}" else "${entry.delta}"
        val escapedNote = (entry.note ?: "").replace("\"", "\"\"")
        sb.appendLine("${entry.timestamp},$dateStr,$timeStr,$type,$deltaStr,${entry.resultingValue},\"$escapedNote\"")
    }

    try {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeTitle = counter.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "historial_${safeTitle}_${System.currentTimeMillis()}.csv"
        val file = File(exportDir, fileName)
        file.writeText(sb.toString(), Charsets.UTF_8)

        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Historial CSV de ${counter.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Exportar CSV de ${counter.title}"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
