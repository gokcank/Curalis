package com.gokcank.curalis.domain.model

enum class SkipReason(val displayNameTr: String, val displayNameEn: String) {
    FORGOT("Unuttum", "I forgot"),
    OVERSLEPT("Uyuya Kaldım", "I overslept"),
    NOT_NEEDED("Bu dozu almama gerek yok", "I don't need this dose"),
    OTHER("Diğer", "Other")
}
