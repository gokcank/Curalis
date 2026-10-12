package com.gokcank.curalis.core.utils

/**
 * İlaç kutusundan okutulan ham koddan ürün numarasını (GTIN) çıkarır.
 *
 * İki biçim gelir:
 * - Düz barkod (EAN-13): kodun kendisi ürün numarasıdır (ör. "8699508010752").
 * - İTS karekodu (GS1 DataMatrix): "01" + 14 haneli GTIN, ardından seri numarası (21),
 *   son kullanma tarihi (17) ve parti numarası (10). Alanlar GS (\u001D) karakteriyle
 *   ayrılır; elle yazılmış/kopyalanmış hâlinde "(01)08699...(21)..." gibi parantezli
 *   olabilir. GTIN her zaman en başta ve sabit uzunlukta olduğu için yalnızca o okunur;
 *   seri numarası her kutuda farklı olduğundan saklanmaz.
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
}
