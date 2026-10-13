package com.gokcank.curalis.data.backup

import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/** Hilt bu fabrikayı enjekte eder; erişim anahtarı yalnızca kullanıcı Drive iznini verdikten
 * sonra elde edilen bir çalışma zamanı değeri olduğu için [GoogleDriveManager] enjekte edilemez. */
class GoogleDriveManagerFactory @Inject constructor() {
    fun create(accessToken: String): GoogleDriveManager = GoogleDriveManager(accessToken)
}

/**
 * Drive'daki gizli uygulama klasörüne (appDataFolder) yedek yazar ve okur. Erişim anahtarı
 * Google'ın Authorization API'siyle (bkz. DriveAuthorization) yalnızca drive.appdata izni için
 * alınır; kullanımdan kalkan Google Sign-In hesabına artık gerek yoktur.
 */
class GoogleDriveManager(accessToken: String) {

    private val driveService: Drive = Drive.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance()
    ) { request -> request.headers.authorization = "Bearer $accessToken" }
        .setApplicationName("Curalis")
        .build()

    /** İzni veren Google hesabının e-posta adresi; ekranda "Bağlı hesap" olarak gösterilir. */
    suspend fun fetchAccountEmail(): String? = withContext(Dispatchers.IO) {
        try {
            driveService.about().get().setFields("user(emailAddress)").execute().user?.emailAddress
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadBackupToDrive(jsonString: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if backup already exists
            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            var existingFileId: String? = null
            for (file in fileList.files) {
                if (file.name == fileName) {
                    existingFileId = file.id
                    break
                }
            }

            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = fileName
                parents = listOf("appDataFolder")
            }

            val mediaContent = InputStreamContent(
                "application/json",
                ByteArrayInputStream(jsonString.toByteArray(Charsets.UTF_8))
            )

            if (existingFileId != null) {
                // Update existing
                val updatedFileMetadata = com.google.api.services.drive.model.File()
                driveService.files().update(existingFileId, updatedFileMetadata, mediaContent).execute()
            } else {
                // Create new
                driveService.files().create(fileMetadata, mediaContent).execute()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun downloadBackupFromDrive(fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            var fileId: String? = null
            for (file in fileList.files) {
                if (file.name == fileName) {
                    fileId = file.id
                    break
                }
            }

            if (fileId != null) {
                val outputStream = ByteArrayOutputStream()
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                return@withContext String(outputStream.toByteArray(), Charsets.UTF_8)
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
