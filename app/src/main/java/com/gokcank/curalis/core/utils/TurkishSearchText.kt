package com.gokcank.curalis.core.utils

/**
 * İlaç aramasında kullanılan metin biçimi: Türkçe büyük/küçük ve noktalı/noktasız i farkı
 * ile ş/ğ/ü/ö/ç ve şapkalı harfler düz karşılıklarına indirgenir, harfler küçültülür ve
 * boşluklar teke indirilir. Böylece "şurup", "ŞURUP" ve "Surup" aynı ürünleri bulur.
 *
 * İlaç sözlüğündeki searchName sütunu aynı kuralla üretilir
 * (tools/build_titck_dictionary.py, search_name); ikisi birebir aynı kalmalıdır.
 * MedicationDictionaryAssetTest sözlüğün bütün satırlarında bunu denetler.
 */
object TurkishSearchText {

    private val FOLD = mapOf(
        'İ' to 'i', 'I' to 'i', 'ı' to 'i', 'i' to 'i', 'Î' to 'i', 'î' to 'i',
        'Ş' to 's', 'ş' to 's', 'Ğ' to 'g', 'ğ' to 'g', 'Ü' to 'u', 'ü' to 'u', 'Û' to 'u', 'û' to 'u',
        'Ö' to 'o', 'ö' to 'o', 'Ç' to 'c', 'ç' to 'c', 'Â' to 'a', 'â' to 'a'
    )

    fun normalize(text: String): String {
        val folded = StringBuilder(text.length)
        for (c in text) {
            folded.append(FOLD[c] ?: if (c.isWhitespace()) ' ' else c.lowercaseChar())
        }
        return folded.split(' ').filter { it.isNotEmpty() }.joinToString(" ")
    }
}
