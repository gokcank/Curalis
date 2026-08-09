package com.gokcank.curalis.domain.model

enum class FrequencyType {
    DAILY,
    SPECIFIC_DAYS,
    INTERVAL,
    /** Aktif gün / dinlenme günü döngüsü (örn. 21 gün kullan, 7 gün ara). */
    CYCLIC,
    AS_NEEDED
}
