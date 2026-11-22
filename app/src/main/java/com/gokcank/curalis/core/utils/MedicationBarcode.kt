package com.gokcank.curalis.core.utils

import java.time.LocalDate
import java.time.YearMonth

/**
 * İlaç kutusundan okutulan ham koddan ürün numarasını (GTIN) çıkarır.
 *
 * İki biçim gelir:
 * - Düz barkod (EAN-13): kodun kendisi ürün numarasıdır (ör. "8699508010752").
 * - İTS karekodu (GS1 DataMatrix): "01" + 14 haneli GTIN, ardından seri numarası (21),
 *   son kullanma tarihi (17) ve parti numarası (10). Alanlar GS (\u001D) karakteriyle
 *   ayrılır; elle yazılmış/kopyalanmış hâlinde "(01)08699...(21)..." gibi parantezli
 *   olabilir. GTIN her zaman en başta ve sabit uzunlukta olduğu için yalnızca o okunur;
 *   seri numarası her kutuda farklı olduğundan saklanmaz. Son kullanma tarihi ayrıca
 *   [extractExpiryDate] ile okunur.
 *
 * TİTCK listesi ürün numaralarını 13 haneli EAN-13 olarak tutar; karekoddaki 14 haneli
 * GTIN'in başındaki 0 atılınca aynı numara elde edilir.
 */
object MedicationBarcode {

    private const val GS = '\u001D'
    private val SYMBOLOGY_PREFIXES = listOf("]d2", "]C1", "]Q3")

    /** Ürün numarasını döndürür; kod bir ilaç barkodu/karekodu olarak tanınmazsa null. */
    fun extractGtin(raw: String): String? {
        var code = raw.trim()
        SYMBOLOGY_PREFIXES.firstOrNull { code.startsWith(it) }?.let { code = code.removePrefix(it) }
        code = code.trimStart(GS).replace("(", "").replace(")", "")

        if (code.length in 8..14 && code.all(Char::isDigit)) return normalize(code)

        if (code.length >= 16 && code.startsWith("01")) {
            val gtin = code.substring(2, 16)
            if (gtin.all(Char::isDigit)) return normalize(gtin)
        }
        return null
    }

    private fun normalize(gtin: String): String =
        if (gtin.length == 14 && gtin.startsWith('0')) gtin.substring(1) else gtin

    /** Uzunluğu sabit (6 haneli tarih) GS1 alanları. */
    private val FIXED_DATE_AIS = setOf("11", "12", "13", "15", "16", "17")

    /** Uzunluğu değişken, GS ile ya da kodun sonuyla biten alanlar: parti ve seri numarası. */
    private val VARIABLE_AIS = setOf("10", "21")

    private val PARENTHESISED_EXPIRY = Regex("\\(17\\)(\\d{6})")

    /**
     * Karekoddaki son kullanma tarihini (alan 17, YYMMDD) okur; düz barkodda, tarih alanı
     * olmayan kodda ya da tarih güvenle bulunamıyorsa null. Seri numarası (21) değişken
     * uzunlukta olduğundan, ayırıcıları (GS) silinmiş bir kodda tarihin nerede başladığı
     * bilinemez; böyle bir kodda tahmin yapılmaz.
     */
    fun extractExpiryDate(raw: String, today: LocalDate = LocalDate.now()): LocalDate? {
        var code = raw.trim()
        SYMBOLOGY_PREFIXES.firstOrNull { code.startsWith(it) }?.let { code = code.removePrefix(it) }
        code = code.trimStart(GS)

        if (code.startsWith("(")) {
            return PARENTHESISED_EXPIRY.find(code)?.groupValues?.get(1)?.let { parseDate(it, today) }
        }

        if (code.length < 16 || !code.startsWith("01") || !code.substring(2, 16).all(Char::isDigit)) return null
        var pos = 16
        while (pos + 2 <= code.length) {
            if (code[pos] == GS) {
                pos++
                continue
            }
            val ai = code.substring(pos, pos + 2)
            when (ai) {
                in FIXED_DATE_AIS -> {
                    val value = code.substring(pos + 2, minOf(pos + 8, code.length))
                    if (value.length != 6 || !value.all(Char::isDigit)) return null
                    if (ai == "17") return parseDate(value, today)
                    pos += 8
                }
                in VARIABLE_AIS -> {
                    val end = code.indexOf(GS, pos + 2)
                    if (end < 0) return null
                    pos = end + 1
                }
                else -> return null
            }
        }
        return null
    }

    /**
     * GS1 tarih biçimi YYMMDD: gün "00" ise ayın son günü demektir. Yüzyıl, GS1 kuralına göre
     * bugünün yılına en yakın olacak şekilde seçilir (51–99 yıl ilerisi bir önceki yüzyıl).
     */
    private fun parseDate(yymmdd: String, today: LocalDate): LocalDate? {
        val yy = yymmdd.substring(0, 2).toInt()
        val month = yymmdd.substring(2, 4).toInt()
        val day = yymmdd.substring(4, 6).toInt()
        if (month !in 1..12) return null
        val currentYy = today.year % 100
        var century = today.year - currentYy
        val diff = yy - currentYy
        if (diff in 51..99) century -= 100
        if (diff in -99..-50) century += 100
        val yearMonth = YearMonth.of(century + yy, month)
        return when (day) {
            0 -> yearMonth.atEndOfMonth()
            in 1..yearMonth.lengthOfMonth() -> yearMonth.atDay(day)
            else -> null
        }
    }
}
