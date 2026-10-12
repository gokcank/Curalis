package com.gokcank.curalis.core.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri

/**
 * Hatırlatmaların zamanında ve görünür biçimde gelmesi için gereken, uygulama içinden
 * verilemeyip sistem ayarlarından açılan izinler. Karşılama ve Hatırlatıcı Sorun Giderme
 * ekranları durumu buradan okur ve ilgili ayar ekranını buradan açar.
 */
object ReminderPermissions {

    /**
     * Android 13+ cihazlarda USE_EXACT_ALARM sayesinde kendiliğinden verilidir (Curalis
     * Play'de dağıtılmadığı için bu izni kullanabiliyor); Android 12'de kullanıcı onayı gerekir.
     */
    fun canScheduleExactAlarms(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    /** Android 14+ bu izni kapalı tutabiliyor; kapalıyken tam ekran hatırlatma sıradan bildirime döner. */
    fun canUseFullScreenIntent(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

    fun isIgnoringBatteryOptimizations(context: Context): Boolean =
        context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(packageIntent(context, Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }

    fun openFullScreenIntentSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.startActivity(packageIntent(context, Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT))
        }
    }

    /**
     * Doğrudan Android'in "pil optimizasyonundan muaf tutulsun mu?" penceresini açar. Play bu
     * pencerenin kullanımını kısıtlıyor; Curalis Play'de dağıtılmadığı ve hatırlatmalar için
     * gerçekten gerektiği için bilinçli olarak kullanılıyor.
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        runCatching {
            context.startActivity(packageIntent(context, Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS))
        }.onFailure {
            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    private fun packageIntent(context: Context, action: String) =
        Intent(action).apply { data = "package:${context.packageName}".toUri() }
}
