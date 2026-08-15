package com.gokcank.curalis.core.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Bazı OEM'ler (Xiaomi/MIUI, Huawei, Samsung, Oppo, Vivo...) kendi pil yönetimi
 * katmanlarında "otomatik başlatma" iznini kapalı tutuyor; bu durumda AlarmManager'a
 * doğru şekilde kayıtlı bir alarm bile arka planda öldürülüp hiç çalmayabiliyor. Android'in
 * bunu sorgulayan ortak bir genel API'si yok — yalnızca her üreticinin kendi ayar ekranına
 * (varsa) yönlendirilebiliriz; ekran cihazda yoksa (farklı ROM sürümü vb.) sessizce
 * başarısız olur ve arayan taraf genel Uygulama Bilgisi ekranına düşer.
 */
object ManufacturerAutostartHelper {

    enum class KnownManufacturer { XIAOMI, HUAWEI, SAMSUNG, OPPO, VIVO, ONEPLUS, OTHER }

    fun detect(): KnownManufacturer {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") -> KnownManufacturer.XIAOMI
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> KnownManufacturer.HUAWEI
            manufacturer.contains("samsung") -> KnownManufacturer.SAMSUNG
            manufacturer.contains("oppo") -> KnownManufacturer.OPPO
            manufacturer.contains("vivo") -> KnownManufacturer.VIVO
            manufacturer.contains("oneplus") -> KnownManufacturer.ONEPLUS
            else -> KnownManufacturer.OTHER
        }
    }

    /** Üreticiye özel otomatik başlatma/pil yönetimi ekranını dener; başarılıysa true döner. */
    fun tryOpenAutostartSettings(context: Context): Boolean {
        val candidates = when (detect()) {
            KnownManufacturer.XIAOMI -> listOf(
                ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            )
            KnownManufacturer.HUAWEI -> listOf(
                ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
            )
            KnownManufacturer.SAMSUNG -> listOf(
                ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
            )
            KnownManufacturer.OPPO -> listOf(
                ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")
            )
            KnownManufacturer.VIVO -> listOf(
                ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            )
            KnownManufacturer.ONEPLUS -> listOf(
                ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
            )
            KnownManufacturer.OTHER -> emptyList()
        }

        for (component in candidates) {
            val intent = Intent().apply {
                setComponent(component)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
        }
        return false
    }
}
