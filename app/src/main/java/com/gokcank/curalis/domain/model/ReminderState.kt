package com.gokcank.curalis.domain.model

enum class ReminderState {
    /** Alarm henüz tetiklenmedi. */
    SCHEDULED,
    /** Alarm tetiklendi ve bildirim gösterildi; kullanıcı henüz yanıt vermedi.
     *  Bu, "alarm hiç çalmadı" (SCHEDULED kalır) ile "çaldı ama yanıtlanmadı" ayrımını sağlar. */
    DELIVERED,
    TAKEN,
    SKIPPED,
    SNOOZED,
    MISSED,
    CANCELLED
}
