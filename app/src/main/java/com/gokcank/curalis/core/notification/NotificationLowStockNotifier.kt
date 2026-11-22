package com.gokcank.curalis.core.notification

import com.gokcank.curalis.domain.repository.LowStockNotifier
import javax.inject.Inject
import javax.inject.Singleton

/** Stok uyarısını "Stok uyarıları" bildirim kanalından gösterir. */
@Singleton
class NotificationLowStockNotifier @Inject constructor(
    private val notificationHelper: NotificationHelper
) : LowStockNotifier {
    override fun notifyLowStock(medicationName: String, remainingStock: Int) {
        notificationHelper.showRefillWarningNotification(medicationName, remainingStock)
    }
}
