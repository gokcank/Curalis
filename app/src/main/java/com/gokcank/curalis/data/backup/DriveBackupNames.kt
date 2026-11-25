package com.gokcank.curalis.data.backup

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Drive'daki yedek dosyalarının adları. Önceden tek bir dosya ([LEGACY_NAME]) her yedekte
 * üzerine yazılıyordu; artık otomatik ve elle alınan yedekler tarihli ayrı dosyalardır
 * (ADR-011). Eski dosya geri yüklemede listelenmeye devam eder.
 */
object DriveBackupNames {
    const val LEGACY_NAME = "curalis_drive_backup.json"
    const val MANUAL_PREFIX = "curalis_drive_manual_"

    enum class Kind { AUTOMATIC, MANUAL, LEGACY }

    data class Info(val id: String, val name: String, val kind: Kind, val takenAt: Long?)

    private fun stampFormat() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).apply { isLenient = false }

    fun manualName(now: Long): String = MANUAL_PREFIX + stampFormat().format(Date(now)) + AutoBackupPolicy.FILE_SUFFIX

    /** Curalis'in yedek dosyası değilse null. */
    fun describe(id: String, name: String): Info? {
        val kind = when {
            name == LEGACY_NAME -> return Info(id, name, Kind.LEGACY, null)
            AutoBackupPolicy.isAutoBackupFile(name) -> Kind.AUTOMATIC
            name.startsWith(MANUAL_PREFIX) && name.endsWith(AutoBackupPolicy.FILE_SUFFIX) -> Kind.MANUAL
            else -> return null
        }
        val prefix = if (kind == Kind.AUTOMATIC) AutoBackupPolicy.FILE_PREFIX else MANUAL_PREFIX
        val stamp = name.removePrefix(prefix).removeSuffix(AutoBackupPolicy.FILE_SUFFIX).take(15)
        val takenAt = try { stampFormat().parse(stamp)?.time } catch (e: ParseException) { null }
        return Info(id, name, kind, takenAt)
    }

    /** En yeni yedek başta; tarihi olmayan eski tek dosya en sonda. */
    fun listForRestore(files: Map<String, String>): List<Info> =
        files.mapNotNull { (name, id) -> describe(id, name) }
            .sortedWith(compareByDescending<Info> { it.takenAt ?: Long.MIN_VALUE }.thenBy { it.name })
}
