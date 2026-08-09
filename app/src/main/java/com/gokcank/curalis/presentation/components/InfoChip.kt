package com.gokcank.curalis.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.gokcank.curalis.core.theme.CuralisIconSize
import com.gokcank.curalis.core.theme.CuralisRadius
import com.gokcank.curalis.core.theme.CuralisSpacing

/**
 * Kart içlerinde durum/özet bilgisi gösteren küçük rozet.
 *
 * design-system.md "Color Independence": bilgi asla yalnızca renge dayanmaz — her rozet
 * ikon + metin + renk üçlüsünü birlikte taşır, bu yüzden ikon zorunlu parametredir.
 */
@Composable
fun InfoChip(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(CuralisRadius.sm),
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = CuralisSpacing.sm,
                vertical = CuralisSpacing.xs
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CuralisSpacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(CuralisIconSize.sm)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
