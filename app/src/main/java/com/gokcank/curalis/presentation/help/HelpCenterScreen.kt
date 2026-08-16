package com.gokcank.curalis.presentation.help

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gokcank.curalis.R

private data class FaqEntry(val question: String, val answer: String)

private val faqEntries = listOf(
    FaqEntry(
        "Hatırlatıcılar zamanında gelmiyor, ne yapmalıyım?",
        "Ayarlar > Bildirim ve Hatırlatıcı Ayarları > Sorun Giderme sihirbazını çalıştırın. En sık neden, telefon üreticinizin pil optimizasyonunun Curalis'i arka planda kapatmasıdır; sihirbaz cihazınıza özel adımları gösterir."
    ),
    FaqEntry(
        "Verilerim nerede saklanıyor?",
        "Tüm ilaç, randevu ve ölçüm verileriniz varsayılan olarak yalnızca bu cihazda, şifrelenmiş bir veritabanında saklanır. Curalis'in sunucusu yoktur; hiçbir veri otomatik olarak dışarı gönderilmez."
    ),
    FaqEntry(
        "Telefonumu değiştirirsem verilerim kaybolur mu?",
        "Ayarlar > Yedekleme & Geri Yükleme'den Google Drive'a şifreli bir yedek alabilirsiniz. Yeni telefonunuzda aynı hesapla oturum açıp yedeği geri yükleyerek kaldığınız yerden devam edebilirsiniz."
    ),
    FaqEntry(
        "Google Drive yedeği güvenli mi?",
        "Evet. Yedek, Drive'a yüklenmeden önce cihazınızda şifrelenir; yalnızca kendi Google hesabınıza ait özel bir alanda saklanır ve başka hiç kimse tarafından görüntülenemez."
    ),
    FaqEntry(
        "Bir ilacı yanlışlıkla sildim, geri getirebilir miyim?",
        "İlaçlarım ekranında bir ilacı sildiğinizde önce arşive taşıma seçeneği sunulur — arşivdeki ilaçlar 'Arşiv' sekmesinden tek dokunuşla geri alınabilir. Ancak 'Kalıcı Olarak Sil' seçeneğiyle silinen kayıtlar geri getirilemez."
    ),
    FaqEntry(
        "Bildirim izni vermedim, sonradan nasıl açarım?",
        "Telefonunuzun Ayarlar > Uygulamalar > Curalis > Bildirimler bölümünden izni daha sonra açabilirsiniz."
    ),
    FaqEntry(
        "Uygulama ücretsiz mi, reklam var mı?",
        "Curalis tamamen ücretsizdir, reklam içermez ve hesap oluşturmanızı gerektirmez."
    ),
    FaqEntry(
        "Bir hata veya öneri bildirmek istiyorum, nasıl yaparım?",
        "Ayarlar > Hakkında ekranındaki 'Hata Bildir' butonuyla, cihaz bilgileriniz otomatik eklenmiş bir e-posta taslağı oluşturabilirsiniz."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yardım Merkezi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sık Sorulan Sorular",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            faqEntries.forEach { entry ->
                FaqItem(entry)
            }
        }
    }
}

@Composable
private fun FaqItem(entry: FaqEntry) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.question,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Text(
                    text = entry.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
