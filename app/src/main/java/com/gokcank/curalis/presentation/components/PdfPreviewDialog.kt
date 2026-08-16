package com.gokcank.curalis.presentation.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gokcank.curalis.core.theme.LocalCuralisColors
import com.gokcank.curalis.core.utils.ReportSummary
import com.gokcank.curalis.presentation.analytics.AdherencePercentageCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Rapor dosyasını paylaşmadan önce kullanıcıya gösteren tam ekran önizleme.
 *
 * design-system.md "Long Running Operations" / genel güven ilkesi: kullanıcı, cihazından
 * çıkacak bir belgeyi (PDF sağlık raporu) görmeden doğrudan paylaşım akışına düşürülmemeli.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewDialog(
    file: File,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    summary: ReportSummary? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var pages by remember(file) { mutableStateOf<List<Bitmap>>(emptyList()) }
        var isLoading by remember(file) { mutableStateOf(true) }

        LaunchedEffect(file) {
            pages = withContext(Dispatchers.IO) { renderPdfPages(file) }
            isLoading = false
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Rapor Önizleme") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kapat")
                        }
                    },
                    actions = {
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = "Paylaş")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (summary != null) {
                            item {
                                val semantic = LocalCuralisColors.current
                                AdherencePercentageCard(
                                    title = "Rapor Özeti",
                                    percentage = summary.adherencePercentage,
                                    subtitle = "Toplam ${summary.totalCount} vakit · Alınan ${summary.takenCount} · Atlanan ${summary.skippedCount} · Kaçırılan ${summary.missedCount}",
                                    modifier = Modifier.fillMaxWidth(),
                                    color = semantic.success
                                )
                            }
                        }
                        items(pages) { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun renderPdfPages(file: File): List<Bitmap> {
    val bitmaps = mutableListOf<Bitmap>()
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
        PdfRenderer(fd).use { renderer ->
            for (i in 0 until renderer.pageCount) {
                renderer.openPage(i).use { page ->
                    // Ekranda net görünmesi için sayfa boyutunun 2 katı çözünürlükte render ediyoruz.
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                }
            }
        }
    }
    return bitmaps
}
