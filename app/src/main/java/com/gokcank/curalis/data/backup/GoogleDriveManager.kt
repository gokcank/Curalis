package com.gokcank.curalis.data.backup

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/** Hilt bu fabrikayı enjekte eder; hesap yalnızca kullanıcı giriş yaptığında elde edilen bir
 * çalışma zamanı değeri olduğu için [GoogleDriveManager]'ın kendisi enjekte edilemez. */
class GoogleDriveManagerFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun create(account: GoogleSignInAccount): GoogleDriveManager = GoogleDriveManager(context, account)
}

class GoogleDriveManager(private val context: Context, private val account: GoogleSignInAccount) {

    private val driveService: Drive

    init {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Curalis")
            .build()
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
