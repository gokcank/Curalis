package com.gokcank.curalis.core.utils

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object BugReportUtils {

    fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            "${manufacturer.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} $model"
        }
    }

    fun getOsVersion(): String {
        return Build.VERSION.RELEASE
    }

    fun getApiLevel(): String {
        return Build.VERSION.SDK_INT.toString()
    }

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Bilinmiyor"
        } catch (e: Exception) {
            "Bilinmiyor"
        }
    }

    suspend fun getLogcat(lines: Int = 200): String = withContext(Dispatchers.IO) {
        val logBuilder = java.lang.StringBuilder()
        try {
            val process = Runtime.getRuntime().exec("logcat -d -t $lines")
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                logBuilder.append(line).append("\n")
            }
        } catch (e: Exception) {
            logBuilder.append("Log okunamadı: ${e.localizedMessage}")
        }
        logBuilder.toString()
    }
}
