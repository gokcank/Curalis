package com.gokcank.curalis.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.CuralisIconSize
import com.gokcank.curalis.core.theme.CuralisSpacing

/**
 * Ortak boş durum gösterimi.
 *
 * design-system.md "Empty States" her boş durumun üç soruyu cevaplamasını şart koşuyor:
 * neden burada bir şey yok, bu beklenen bir durum mu, sırada ne yapmalıyım.
 * Bu yüzden [description] ve (mümkünse) [actionLabel] zorunlu gibi ele alınmalıdır.
 *
 * "Sonuç yok" durumu boş durumdan farklıdır: koleksiyon doludur, yalnızca arama eşleşmemiştir.
 * O durum için [NoResultsState] kullanılır.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CuralisSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(CuralisIconSize.xl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = CuralisSpacing.md)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = CuralisSpacing.xs)
        )
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                modifier = Modifier.padding(top = CuralisSpacing.lg)
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * Arama/filtre sonucu boş döndüğünde kullanılır. Kullanıcıya "hiç kaydınız yok" demek
 * yanlış bilgi olur — kayıtlar vardır, yalnızca sorgu eşleşmemiştir.
 */
@Composable
fun NoResultsState(
    icon: ImageVector,
    query: String,
    modifier: Modifier = Modifier,
    onClearSearch: (() -> Unit)? = null
) {
    EmptyState(
        icon = icon,
        title = stringResource(R.string.no_results_found_title),
        description = stringResource(R.string.no_results_found_desc, query),
        actionLabel = onClearSearch?.let { stringResource(R.string.clear_search_button) },
        onAction = onClearSearch,
        modifier = modifier
    )
}
