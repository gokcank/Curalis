package com.gokcank.curalis.presentation.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gokcank.curalis.R

/**
 * Girilen hane sayısını noktalarla gösterir. Rakamların kendisi hiçbir zaman ekranda
 * görünmez; omuz üstünden bakan biri PIN'i okuyamasın diye.
 */
@Composable
fun PinDots(
    enteredCount: Int,
    pinLength: Int,
    isError: Boolean = false
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(pinLength) { index ->
            val filled = index < enteredCount
            val color = when {
                isError -> MaterialTheme.colorScheme.error
                filled -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .size(if (filled) 18.dp else 14.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

/**
 * Sayısal PIN tuş takımı. [onBiometricClick] verilmezse sol alt köşe boş bırakılır,
 * böylece rakamlar her durumda aynı ızgara konumunda kalır.
 */
@Composable
fun PinKeypad(
    onDigitClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit,
    enabled: Boolean = true,
    onBiometricClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(1..3, 4..6, 7..9).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { digit ->
                    KeypadButton(
                        label = digit.toString(),
                        enabled = enabled,
                        onClick = { onDigitClick(digit) }
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBiometricClick != null) {
                IconButton(
                    onClick = onBiometricClick,
                    enabled = enabled,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = stringResource(R.string.app_lock_use_biometric),
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            KeypadButton(
                label = "0",
                enabled = enabled,
                onClick = { onDigitClick(0) }
            )

            IconButton(
                onClick = onBackspaceClick,
                enabled = enabled,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_digit),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(72.dp)
    ) {
        // Ekran okuyucu düğmeyi içindeki metinden okur; ayrıca bir contentDescription
        // eklemek aynı rakamın iki kez seslendirilmesine yol açardı.
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
