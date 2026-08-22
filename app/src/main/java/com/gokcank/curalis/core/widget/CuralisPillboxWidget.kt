package com.gokcank.curalis.core.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.presentation.main.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val reminderIdKey = ActionParameters.Key<String>("reminder_id")

/**
 * Ana ekrandaki "Bugünün Kutusu" widget'ı — uygulama içi Home ekranındaki pillbox
 * şeridinin (bkz. HomeScreen.PillboxStrip) küçük, bağımsız bir yansıması. Uygulamayı
 * açmadan bugünün dozlarını görüp "Aldım" olarak işaretlemeyi sağlar.
 */
class CuralisPillboxWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, CuralisWidgetEntryPoint::class.java)

        val dayCal = Calendar.getInstance()
        val startOfDay = (dayCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = (dayCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val reminders = entryPoint.getRemindersBetweenDatesUseCase()(startOfDay, endOfDay).first()
        val medications = entryPoint.getMedicationsUseCase()().first()
        val medsMap = medications.associateBy { it.id }
        val items = reminders.sortedBy { it.timeInMillis }

        provideContent {
            GlanceTheme {
                WidgetContent(items = items, medsMap = medsMap)
            }
        }
    }
}

@Composable
private fun WidgetContent(items: List<Reminder>, medsMap: Map<String, Medication>) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM", Locale("tr")) }
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "Bugünün Kutusu",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = dateFormat.format(Date()),
                style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant)
            )
        }

        if (items.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bugün için planlanmış doz yok.",
                    style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurfaceVariant)
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                items(items, itemId = { it.id.hashCode().toLong() }) { reminder ->
                    PillboxRow(reminder = reminder, medication = medsMap[reminder.medicationId])
                }
            }
        }
    }
}

@Composable
private fun PillboxRow(reminder: Reminder, medication: Medication?) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val isTaken = reminder.state == ReminderState.TAKEN

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(28.dp)
                .background(if (isTaken) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant)
                .clickable(
                    actionRunCallback<TakeDoseAction>(actionParametersOf(reminderIdKey to reminder.id))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isTaken) "✓" else "",
                style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onPrimary, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = medication?.name ?: "İlaç",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GlanceTheme.colors.onSurface),
                maxLines = 1
            )
            Text(
                text = timeFormat.format(Date(reminder.timeInMillis)),
                style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}
