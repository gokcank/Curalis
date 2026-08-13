package com.gokcank.curalis.core.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * İlaç fotoğraflarını yalnızca bu cihazda saklar — hiçbir sunucuya yüklenmez. Kamera veya
 * galeriden gelen görsel her zaman uygulamanın kendi özel dosya alanına kopyalanır; galeri
 * URI'sine doğrudan güvenilmez çünkü kullanıcı izni kalıcı olmayabilir veya kaynak dosya
 * silinebilir.
 */
@Singleton
class MedicationPhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val photosDir: File by lazy {
        File(context.filesDir, "medication_photos").apply { mkdirs() }
    }

    /**
     * Kamera uygulamasının fotoğrafı yazacağı boş bir dosya ve bu dosyaya paylaşılabilir
     * erişim sağlayan bir içerik URI'si hazırlar (bkz. res/xml/file_paths.xml).
     */
    fun createCaptureTarget(): Pair<File, Uri> {
        val file = File(photosDir, "${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }

    /** Galeriden seçilen fotoğrafın içeriğini yerel depolamaya kopyalar; başarısızsa null döner. */
    fun copyFromUri(sourceUri: Uri): String? {
        return try {
            val file = File(photosDir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            } ?: return null
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** Artık kullanılmayan bir fotoğraf dosyasını siler (ör. değiştirildi/kaldırıldı/ilaç silindi). */
    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}
