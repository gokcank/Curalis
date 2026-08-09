package com.gokcank.curalis.presentation.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.gokcank.curalis.BuildConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.launch
import com.gokcank.curalis.R
import com.gokcank.curalis.core.utils.BugReportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.app_version, "v${BuildConfig.VERSION_NAME}"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.developer, "gokcank"),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.database_date_label, com.gokcank.curalis.data.local.CuralisDatabase.DATABASE_DATE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.medical_disclaimer_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.medical_disclaimer_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showPrivacyDialog = true },
                    modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.privacy_policy_button))
                }

                Spacer(modifier = Modifier.height(20.dp))

                val uriHandler = LocalUriHandler.current
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                
                val bugReportSubject = stringResource(R.string.bug_report_subject)
                val bugReportBodyTemplate = stringResource(R.string.bug_report_body)
                val appVersion = BugReportUtils.getAppVersion(context)
                val deviceModel = BugReportUtils.getDeviceModel()
                val osVersion = BugReportUtils.getOsVersion()
                val apiLevel = BugReportUtils.getApiLevel()
                
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = "GitHub",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { uriHandler.openUri("https://github.com/gokcank/Curalis") },
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val logcatUri = BugReportUtils.getLogcatFileUri(context, 200)
                            val emailBody = String.format(
                                bugReportBodyTemplate,
                                deviceModel,
                                osVersion,
                                apiLevel,
                                appVersion
                            )

                            val mailtoUri = Uri.parse("mailto:destek.gokcank@gmail.com?subject=${Uri.encode(bugReportSubject)}")

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = mailtoUri
                                putExtra(Intent.EXTRA_SUBJECT, bugReportSubject)
                                putExtra(Intent.EXTRA_TEXT, emailBody)
                                logcatUri?.let { uri ->
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    clipData = ClipData.newRawUri("logcat", uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            }
                            
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "E-posta uygulaması bulunamadı.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.bug_report))
                }
            }

            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text(stringResource(R.string.privacy_policy_title)) },
                    text = {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = stringResource(R.string.privacy_policy_body),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                )
            }
        }
    }
}
