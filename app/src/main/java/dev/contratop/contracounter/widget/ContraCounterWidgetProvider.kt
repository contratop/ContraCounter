package dev.contratop.contracounter.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.RemoteViews
import dev.contratop.contracounter.FullscreenCounterActivity
import dev.contratop.contracounter.MainActivity
import dev.contratop.contracounter.R
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.data.CounterHistoryEntry
import dev.contratop.contracounter.data.CounterRepository

/**
 * AppWidgetProvider para la pantalla de inicio de Android.
 * Permite visualizar la puntuación en vivo, cambiar entre contadores y sumar/restar
 * directamente desde el launcher sin necesidad de abrir la aplicación.
 */
class ContraCounterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = CounterRepository.getInstance(context)
        val counters = repo.loadCounters()

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, counters)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val repo = CounterRepository.getInstance(context)

        when (action) {
            ACTION_WIDGET_INCREMENT -> {
                val counterId = intent.getStringExtra(EXTRA_COUNTER_ID)
                if (counterId != null) {
                    modifyCounterValue(context, repo, counterId, isIncrement = true)
                }
            }

            ACTION_WIDGET_DECREMENT -> {
                val counterId = intent.getStringExtra(EXTRA_COUNTER_ID)
                if (counterId != null) {
                    modifyCounterValue(context, repo, counterId, isIncrement = false)
                }
            }

            ACTION_WIDGET_CYCLE -> {
                val appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    cycleWidgetCounter(context, repo, appWidgetId)
                }
            }

            else -> {
                super.onReceive(context, intent)
            }
        }
    }

    private fun modifyCounterValue(
        context: Context,
        repo: CounterRepository,
        counterId: String,
        isIncrement: Boolean
    ) {
        val counters = repo.loadCounters().toMutableList()
        val index = counters.indexOfFirst { it.id == counterId }
        if (index == -1) return

        val counter = counters[index]
        val step = counter.step
        val delta = if (isIncrement) step else -step
        val newValue = counter.currentValue + delta

        counters[index] = counter.copy(currentValue = newValue)
        repo.saveCounters(counters)

        // Registrar entrada en el historial
        repo.addHistoryEntry(
            CounterHistoryEntry(
                counterId = counter.id,
                delta = delta,
                resultingValue = newValue,
                timestamp = System.currentTimeMillis()
            )
        )

        // Vibración háptica si está habilitada
        triggerHaptic(context, repo)

        // Actualizar todos los widgets
        updateAllWidgets(context)
    }

    private fun cycleWidgetCounter(context: Context, repo: CounterRepository, appWidgetId: Int) {
        val counters = repo.loadCounters()
        if (counters.isEmpty()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCounterId = prefs.getString(getWidgetKey(appWidgetId), null)
        val currentIndex = counters.indexOfFirst { it.id == currentCounterId }

        val nextIndex = if (currentIndex == -1 || currentIndex >= counters.size - 1) 0 else currentIndex + 1
        val nextCounter = counters[nextIndex]

        prefs.edit().putString(getWidgetKey(appWidgetId), nextCounter.id).apply()

        triggerHaptic(context, repo)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateWidget(context, appWidgetManager, appWidgetId, counters)
    }

    private fun triggerHaptic(context: Context, repo: CounterRepository) {
        if (!repo.hapticsEnabledFlow.value) return

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30L)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_INCREMENT = "dev.contratop.contracounter.ACTION_WIDGET_INCREMENT"
        const val ACTION_WIDGET_DECREMENT = "dev.contratop.contracounter.ACTION_WIDGET_DECREMENT"
        const val ACTION_WIDGET_CYCLE = "dev.contratop.contracounter.ACTION_WIDGET_CYCLE"

        const val EXTRA_COUNTER_ID = "extra_counter_id"
        const val EXTRA_WIDGET_ID = "extra_widget_id"

        private const val PREFS_NAME = "contra_counter_widget_prefs"
        private fun getWidgetKey(widgetId: Int) = "widget_${widgetId}_counter_id"

        /**
         * Actualiza un widget individual con el contador seleccionado o el primero disponible.
         */
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            counters: List<Counter>? = null
        ) {
            val list = counters ?: CounterRepository.getInstance(context).loadCounters()
            val views = RemoteViews(context.packageName, R.layout.widget_contra_counter)

            if (list.isEmpty()) {
                // Estado vacío si no hay contadores
                views.setTextViewText(R.id.widget_counter_title, "CONTRACOUNTER")
                views.setTextViewText(R.id.widget_counter_value, "0")
                views.setViewVisibility(R.id.widget_counter_limits, View.GONE)
                views.setTextViewText(R.id.widget_btn_plus, "+ 1")
                views.setTextViewText(R.id.widget_btn_minus, "- 1")

                val mainIntent = Intent(context, MainActivity::class.java)
                val pendingMain = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_value_container, pendingMain)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // Seleccionar el contador asignado a este widget
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedCounterId = prefs.getString(getWidgetKey(appWidgetId), null)
            val selectedCounter = list.find { it.id == savedCounterId } ?: list.first()

            // Asignar datos del contador
            views.setTextViewText(R.id.widget_counter_title, selectedCounter.title.uppercase())
            views.setTextViewText(R.id.widget_counter_value, selectedCounter.currentValue.toString())
            views.setTextViewText(R.id.widget_btn_plus, "+ ${selectedCounter.step}")
            views.setTextViewText(R.id.widget_btn_minus, "- ${selectedCounter.step}")

            // Mostrar metas o K.O. si están configurados
            val limitsSb = StringBuilder()
            if (selectedCounter.targetValue != null) {
                limitsSb.append("🏆 Meta: ${selectedCounter.targetValue} ")
            }
            if (selectedCounter.koValue != null) {
                limitsSb.append("💀 K.O.: ${selectedCounter.koValue}")
            }
            val limitsText = limitsSb.toString().trim()
            if (limitsText.isNotEmpty()) {
                views.setTextViewText(R.id.widget_counter_limits, limitsText)
                views.setViewVisibility(R.id.widget_counter_limits, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_counter_limits, View.GONE)
            }

            // Click en el número gigante abre FullscreenCounterActivity para ese contador
            val fullscreenIntent = Intent(context, FullscreenCounterActivity::class.java).apply {
                putExtra(FullscreenCounterActivity.EXTRA_COUNTER_ID, selectedCounter.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingFullscreen = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 1,
                fullscreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_value_container, pendingFullscreen)

            // Click en botón Sumar (+)
            val incIntent = Intent(context, ContraCounterWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_INCREMENT
                putExtra(EXTRA_COUNTER_ID, selectedCounter.id)
                putExtra(EXTRA_WIDGET_ID, appWidgetId)
            }
            val pendingInc = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 2,
                incIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_plus, pendingInc)

            // Click en botón Restar (-)
            val decIntent = Intent(context, ContraCounterWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_DECREMENT
                putExtra(EXTRA_COUNTER_ID, selectedCounter.id)
                putExtra(EXTRA_WIDGET_ID, appWidgetId)
            }
            val pendingDec = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 3,
                decIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_minus, pendingDec)

            // Click en botón Cambiar/Ciclar contador (⇄)
            val cycleIntent = Intent(context, ContraCounterWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_CYCLE
                putExtra(EXTRA_WIDGET_ID, appWidgetId)
            }
            val pendingCycle = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 4,
                cycleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_cycle, pendingCycle)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Actualiza todos los widgets activos en la pantalla de inicio.
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val component = ComponentName(context, ContraCounterWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component) ?: return

            if (appWidgetIds.isNotEmpty()) {
                val counters = CounterRepository.getInstance(context).loadCounters()
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id, counters)
                }
            }
        }
    }
}
