package com.gokcank.curalis.data.provider.local

import com.gokcank.curalis.core.utils.TurkishSearchText
import com.gokcank.curalis.data.local.entity.DrugNameRow

/**
 * Arama sonuç vermeyince "Bunu mu demek istediniz?" için yazımı yakın ilaç adları. Adın
 * harf içeren ilk sözcüğü (çoğunlukla marka adı) aranan ilk sözcükle karşılaştırılır;
 * kullanıcı sözcüğün yalnızca başını yazmış olabileceği için aynı uzunluktaki başıyla da
 * karşılaştırılır. Kısa sözcüklerde en çok 1, uzunlarda en çok 2 harf farkına izin verilir.
 */
object MedicationSpelling {

    private const val MIN_QUERY_LENGTH = 3

    fun suggest(query: String, names: List<DrugNameRow>, limit: Int = 3): List<String> {
        val queryWord = firstWordWithLetter(TurkishSearchText.normalize(query).split(' '))?.second ?: return emptyList()
        if (queryWord.length < MIN_QUERY_LENGTH) return emptyList()
        val maxDistance = if (queryWord.length <= 4) 1 else 2

        val best = HashMap<String, Pair<Int, String>>()
        for (row in names) {
            val (index, word) = firstWordWithLetter(row.searchName.split(' ')) ?: continue
            val distance = minOf(
                levenshtein(queryWord, word),
                if (word.length > queryWord.length) levenshtein(queryWord, word.substring(0, queryWord.length)) else Int.MAX_VALUE
            )
            if (distance > maxDistance) continue
            val display = row.drugName.split(Regex("\\s+")).filter { it.isNotEmpty() }.getOrNull(index) ?: continue
            val current = best[word]
            if (current == null || distance < current.first) best[word] = distance to display
        }
        return best.values
            .sortedWith(compareBy({ it.first }, { it.second }))
            .map { it.second }
            .take(limit)
    }

    private fun firstWordWithLetter(words: List<String>): Pair<Int, String>? =
        words.withIndex().firstOrNull { (_, w) -> w.any(Char::isLetter) }?.let { it.index to it.value }

    private fun levenshtein(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val current = IntArray(b.length + 1)
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
            }
            previous = current
        }
        return previous[b.length]
    }
}
