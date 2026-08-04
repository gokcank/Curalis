package com.gokcank.curalis.core.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
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

    suspend fun getLogcatFileUri(context: Context, lines: Int = 200): Uri? = withContext(Dispatchers.IO) {
        try {
            val logsDir = File(context.cacheDir, "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            val logFile = File(logsDir, "logcat.txt")
            val logContent = getLogcat(lines)
            logFile.writeText(logContent)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                logFile
            )
        } catch (e: Exception) {
            null
        }
    }
}
