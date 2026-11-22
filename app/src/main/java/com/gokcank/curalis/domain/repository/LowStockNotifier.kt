package com.gokcank.curalis.domain.repository

/** Stok eşiğin altına indiğinde kullanıcıyı uyarır — iş mantığı, uyarının nasıl (bildirim
 *  kanalı vb.) gösterildiğini bilmemesi için bu arayüz üzerinden çağırır. */
interface LowStockNotifier {
    fun notifyLowStock(medicationName: String, remainingStock: Int)
}
