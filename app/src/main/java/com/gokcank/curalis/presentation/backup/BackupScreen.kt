package com.gokcank.curalis.presentation.backup

import android.accounts.Account
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gokcank.curalis.data.backup.DriveBackupNames
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Switch
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gokcank.curalis.BuildConfig
import com.gokcank.curalis.R
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Compose ile ilgisi olmayan aktivite sonuç callback'leri içinde stringResource()
    // çağrılamaz (yalnızca composition sırasında çalışabilir) — bu yüzden şablonlar
    // burada, composable gövdesinde önceden çözülüp callback içinde String.format ile kullanılır.
    val googleSignInErrorOauthTemplate = stringResource(R.string.google_signin_error_oauth)
    val googleSignInErrorGenericTemplate = stringResource(R.string.google_signin_error_generic)
    val unknownErrorText = stringResource(R.string.unknown_error)
    val signInErrorGenericTemplate = stringResource(R.string.signin_error_generic)

    // Bekleyen şifre isteyen işlem: hangi eylemin şifre girildikten sonra çalıştırılacağını tutar.
    var pendingPasswordAction by remember { mutableStateOf<PendingPasswordAction?>(null) }
    val autoBackup by viewModel.autoBackup.collectAsState()
    val driveBackups by viewModel.driveBackups.collectAsState()
    val drivePasswordRequest by viewModel.drivePasswordRequest.collectAsState()
    LifecycleResumeEffect(Unit) {
        viewModel.refreshAutoBackupState()
        onPauseOrDispose { }
    }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingLocalExportPassword by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val password = pendingLocalExportPassword
        if (uri != null && password != null) {
            viewModel.exportDataToUri(context, uri, password)
        }
        pendingLocalExportPassword = null
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            pendingPasswordAction = PendingPasswordAction.RestoreLocal
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // Google Drive izni: kullanımdan kalkan Google Sign-In yerine Authorization API. Yalnızca
    // Drive'daki gizli uygulama klasörü (drive.appdata) izni istenir; her Drive işleminden önce
    // aynı hesap için taze bir erişim anahtarı alınır. İzin daha önce verildiyse ekran açılmaz.
    val driveAccountEmail by viewModel.driveAccountEmail.collectAsState()
    val authorizationClient = remember { Identity.getAuthorizationClient(context) }
    val driveScopes = remember { listOf(Scope(DriveScopes.DRIVE_APPDATA)) }
    // İzin ekranı açıldıysa, kullanıcı onayladıktan sonra hangi işlemin süreceğini tutar.
    var pendingDriveOperation by remember { mutableStateOf<DriveOperation?>(null) }

    fun showDriveError(e: Exception) {
        val errorMsg = if (e is ApiException) {
            if (e.statusCode == 10) {
                String.format(googleSignInErrorOauthTemplate, e.statusCode)
            } else {
                String.format(googleSignInErrorGenericTemplate, e.statusCode, e.localizedMessage ?: unknownErrorText)
            }
        } else {
            String.format(signInErrorGenericTemplate, e.localizedMessage ?: unknownErrorText)
        }
        coroutineScope.launch { snackbarHostState.showSnackbar(errorMsg) }
    }

    fun runDriveOperation(operation: DriveOperation, accessToken: String) {
        when (operation) {
            DriveOperation.Connect -> viewModel.connectDrive(accessToken)
            DriveOperation.Backup -> viewModel.uploadToGoogleDrive(accessToken)
            DriveOperation.ListForRestore -> viewModel.loadDriveBackups(accessToken)
        }
    }

    val driveAuthorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val operation = pendingDriveOperation
        pendingDriveOperation = null
        if (operation == null || result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        try {
            val token = authorizationClient.getAuthorizationResultFromIntent(result.data).accessToken
            if (token != null) runDriveOperation(operation, token)
        } catch (e: Exception) {
            showDriveError(e)
        }
    }

    fun withDriveAccess(operation: DriveOperation) {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(driveScopes)
            .apply {
                val email = driveAccountEmail
                if (operation == DriveOperation.Connect || email == null) {
                    // Bağlanırken hangi Google hesabının kullanılacağı her zaman sorulur.
                    setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
                } else {
                    setAccount(Account(email, GOOGLE_ACCOUNT_TYPE))
                }
            }
            .build()
        authorizationClient.authorize(request)
            .addOnSuccessListener { result: AuthorizationResult ->
                val pendingIntent = result.pendingIntent
                val token = result.accessToken
                if (result.hasResolution() && pendingIntent != null) {
                    pendingDriveOperation = operation
                    driveAuthorizationLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                } else if (token != null) {
                    runDriveOperation(operation, token)
                }
            }
            .addOnFailureListener { e -> showDriveError(e) }
    }

    fun disconnectDrive() {
        val email = driveAccountEmail
        viewModel.disconnectDrive()
        if (email != null) {
            // Verilen Drive iznini de geri al; başarısız olsa bile uygulama bağlantıyı unutur.
            authorizationClient.revokeAccess(
                RevokeAccessRequest.builder()
                    .setAccount(Account(email, GOOGLE_ACCOUNT_TYPE))
                    .setScopes(driveScopes)
                    .build()
            )
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is BackupUiState.Success -> {
                snackbarHostState.showSnackbar((uiState as BackupUiState.Success).message)
                viewModel.resetState()
            }
            is BackupUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as BackupUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_restore_title), color = androidx.compose.material3.MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.backup_screen_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AutoBackupCard(
                state = autoBackup,
                onToggle = { enable -> if (enable) viewModel.enableAutoBackup() else viewModel.disableAutoBackup() },
                onEncryptToggle = { on ->
                    if (on) pendingPasswordAction = PendingPasswordAction.EnableEncryption else viewModel.disableEncryption()
                },
                onChangePassword = { pendingPasswordAction = PendingPasswordAction.ChangeBackupPassword },
                onBackupNow = { viewModel.backupNow() }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.backup_export_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.backup_export_card_desc),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = { pendingPasswordAction = PendingPasswordAction.BackupLocal },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is BackupUiState.Loading
                    ) {
                        Text(stringResource(R.string.manual_backup_button))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.backup_import_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.backup_import_card_desc),
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is BackupUiState.Loading,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(stringResource(R.string.select_and_load_backup_button))
                    }
                }
            }

            if (uiState is BackupUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text((uiState as BackupUiState.Loading).message)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Google Drive Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.google_drive_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.google_drive_card_desc),
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (driveAccountEmail == null) {
                        Button(
                            onClick = { withDriveAccess(DriveOperation.Connect) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text(stringResource(R.string.sign_in_with_google_button))
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.connected_account_label, driveAccountEmail ?: stringResource(R.string.unknown_account)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { withDriveAccess(DriveOperation.Backup) },
                                modifier = Modifier.weight(1f),
                                enabled = uiState !is BackupUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(stringResource(R.string.backup_to_drive_button))
                            }
                            OutlinedButton(
                                onClick = { withDriveAccess(DriveOperation.ListForRestore) },
                                modifier = Modifier.weight(1f),
                                enabled = uiState !is BackupUiState.Loading,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(stringResource(R.string.restore_from_drive_button))
                            }
                        }
                        TextButton(
                            onClick = { disconnectDrive() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.sign_out_button))
                        }
                    }
                }
            }
        }
    }

    driveBackups?.let { backups ->
        DriveBackupChooser(
            backups = backups,
            onDismiss = { viewModel.dismissDriveBackups() },
            onSelect = { backup ->
                viewModel.dismissDriveBackups()
                viewModel.restoreDriveBackup(backup.id)
            }
        )
    }

    // Şifreli Drive yedeği ve telefonda onu açan şifre yok (ör. yeni telefon): bir kez sorulur.
    drivePasswordRequest?.let { fileId ->
        BackupPasswordDialog(
            isSettingPassword = false,
            onDismiss = viewModel::cancelDrivePasswordRequest,
            onConfirm = { password -> viewModel.restoreDriveBackup(fileId, password) }
        )
    }

    pendingPasswordAction?.let { action ->
        val isAutoBackup = action is PendingPasswordAction.EnableEncryption || action is PendingPasswordAction.ChangeBackupPassword
        val isSettingPassword = action is PendingPasswordAction.BackupLocal || isAutoBackup
        BackupPasswordDialog(
            isSettingPassword = isSettingPassword,
            descriptionRes = if (isAutoBackup) R.string.auto_backup_password_desc else R.string.backup_password_set_desc,
            onDismiss = {
                pendingPasswordAction = null
                pendingImportUri = null
            },
            onConfirm = { password ->
                when (action) {
                    PendingPasswordAction.BackupLocal -> {
                        pendingLocalExportPassword = password
                        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        exportLauncher.launch("curalis_backup_$timeStamp.json")
                    }
                    PendingPasswordAction.RestoreLocal -> {
                        pendingImportUri?.let { uri -> viewModel.importDataFromUri(context, uri, password) }
                        pendingImportUri = null
                    }
                    PendingPasswordAction.EnableEncryption -> viewModel.enableEncryption(password)
                    PendingPasswordAction.ChangeBackupPassword -> viewModel.changeBackupPassword(password)
                }
                pendingPasswordAction = null
            }
        )
    }
}

private sealed class PendingPasswordAction {
    data object BackupLocal : PendingPasswordAction()
    data object RestoreLocal : PendingPasswordAction()
    data object EnableEncryption : PendingPasswordAction()
    data object ChangeBackupPassword : PendingPasswordAction()
}

@Composable
private fun BackupPasswordDialog(
    isSettingPassword: Boolean,
    descriptionRes: Int = R.string.backup_password_set_desc,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isSettingPassword) stringResource(R.string.set_backup_password_title) else stringResource(R.string.enter_backup_password_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isSettingPassword) {
                    Text(
                        text = stringResource(descriptionRes),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text(stringResource(R.string.password_label)) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isSettingPassword) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; error = null },
                        label = { Text(stringResource(R.string.password_confirm_label)) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            val passwordTooShortError = stringResource(R.string.password_too_short_error)
            val passwordsDontMatchError = stringResource(R.string.passwords_dont_match_error)
            TextButton(onClick = {
                when {
                    password.length < 4 -> error = passwordTooShortError
                    isSettingPassword && password != confirmPassword -> error = passwordsDontMatchError
                    else -> onConfirm(password)
                }
            }) {
                Text(stringResource(R.string.confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/** Drive izni alındıktan sonra yapılacak işlem. */
private sealed class DriveOperation {
    data object Connect : DriveOperation()
    data object Backup : DriveOperation()
    /** Geri yükleme için Drive'daki yedekleri listele; kullanıcı birini seçer, gerekirse şifre sorulur. */
    data object ListForRestore : DriveOperation()
}

private const val GOOGLE_ACCOUNT_TYPE = "com.google"

/**
 * Otomatik yedek (ADR-013): günde bir, yalnızca Google Drive'a; en yeni 30 kopya. Yalnızca Drive
 * bağlıyken açılabilir. "Yedeği şifrele" isteğe bağlıdır. Satırların tamamı açma-kapama öğesidir
 * (ekran okuyucu kuralı, bkz. accessibility.md).
 */
@Composable
private fun AutoBackupCard(
    state: AutoBackupState,
    onToggle: (Boolean) -> Unit,
    onEncryptToggle: (Boolean) -> Unit,
    onChangePassword: () -> Unit,
    onBackupNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.auto_backup_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // Kapalıyken açmak için Drive bağlantısı gerekir; açıksa her zaman kapatılabilir.
            val canToggle = state.enabled || state.driveConnected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = state.enabled, enabled = canToggle, role = Role.Switch, onValueChange = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.auto_backup_toggle_title), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(if (state.driveConnected) R.string.auto_backup_toggle_desc else R.string.auto_backup_drive_not_connected),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = state.enabled, onCheckedChange = null, enabled = canToggle)
            }
            if (state.enabled) {
                val dateFormat = remember { java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT) }
                Text(
                    text = if (state.lastSuccessAt > 0) {
                        stringResource(R.string.auto_backup_last, dateFormat.format(Date(state.lastSuccessAt)))
                    } else {
                        stringResource(R.string.auto_backup_never)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                state.lastFailure?.let {
                    Text(
                        stringResource(autoBackupFailureMessage(it)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Button(onClick = onBackupNow, enabled = !state.running, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.auto_backup_now))
                }
            }
            // "Yedeği şifrele": elle Drive yedeğini de etkiler; otomatik yedek kapalıyken de görünür.
            if (state.enabled || state.driveConnected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = state.encryptEnabled, role = Role.Switch, onValueChange = onEncryptToggle),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.auto_backup_encrypt_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(if (state.encryptEnabled) R.string.auto_backup_encrypt_on_desc else R.string.auto_backup_encrypt_off_desc),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = state.encryptEnabled, onCheckedChange = null)
                }
                if (state.encryptEnabled) {
                    OutlinedButton(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.auto_backup_change_password))
                    }
                }
            }
        }
    }
}

/** Drive'daki yedeklerden hangisinin geri yükleneceği; en yeni başta. */
@Composable
private fun DriveBackupChooser(
    backups: List<DriveBackupNames.Info>,
    onDismiss: () -> Unit,
    onSelect: (DriveBackupNames.Info) -> Unit
) {
    val dateFormat = remember { java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.drive_restore_choose_title)) },
        text = {
            LazyColumn {
                items(backups, key = { it.id }) { backup ->
                    val kind = stringResource(
                        when (backup.kind) {
                            DriveBackupNames.Kind.AUTOMATIC -> R.string.drive_backup_kind_auto
                            DriveBackupNames.Kind.MANUAL -> R.string.drive_backup_kind_manual
                            DriveBackupNames.Kind.LEGACY -> R.string.drive_backup_kind_legacy
                        }
                    )
                    val label = backup.takenAt?.let { stringResource(R.string.drive_backup_label, dateFormat.format(Date(it)), kind) } ?: kind
                    TextButton(onClick = { onSelect(backup) }, modifier = Modifier.fillMaxWidth()) {
                        Text(label, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
