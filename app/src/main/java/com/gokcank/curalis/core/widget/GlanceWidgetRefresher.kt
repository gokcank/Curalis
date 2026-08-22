package com.gokcank.curalis.core.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.gokcank.curalis.domain.repository.WidgetRefresher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlanceWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetRefresher {
    override suspend fun refresh() {
        CuralisPillboxWidget().updateAll(context)
    }
}
