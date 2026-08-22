package com.gokcank.curalis.core.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.gokcank.curalis.domain.model.ReminderState
import dagger.hilt.android.EntryPointAccessors

/** Widget'taki bir doz satırına dokunulduğunda çalışır — dozu "Alındı" olarak işaretler
 *  ve widget'ı hemen tazeler (bkz. CuralisPillboxWidget.reminderIdKey). */
class TakeDoseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val reminderId = parameters[reminderIdKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(context, CuralisWidgetEntryPoint::class.java)
        entryPoint.acknowledgeReminderUseCase()(reminderId, ReminderState.TAKEN, takenAtMillis = System.currentTimeMillis())
        CuralisPillboxWidget().updateAll(context)
    }
}
