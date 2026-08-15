package com.gokcank.curalis.domain.model

enum class SkipReason(val displayNameTr: String, val displayNameEn: String) {
    FORGOT("Unuttum", "I forgot"),
    OVERSLEPT("Uyuya Kaldım", "I overslept"),
    NOT_NEEDED("Bu dozu almama gerek yok", "I don't need this dose"),
    SIDE_EFFECT("Yan etki / rahatsızlık hissettim", "I felt a side effect"),
    OUT_OF_STOCK("İlacım kalmadı", "I ran out of medication"),
    COST("Maliyet nedeniyle", "Because of the cost"),
    OTHER("Diğer", "Other")
}
