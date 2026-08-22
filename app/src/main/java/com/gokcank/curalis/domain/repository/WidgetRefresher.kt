package com.gokcank.curalis.domain.repository

/** Ana ekran widget'ının içeriğini tazeler — veri katmanı, widget'ın nasıl (Glance,
 *  RemoteViews vb.) çizildiğini bilmemesi için bu arayüz üzerinden çağırır. */
interface WidgetRefresher {
    suspend fun refresh()
}
