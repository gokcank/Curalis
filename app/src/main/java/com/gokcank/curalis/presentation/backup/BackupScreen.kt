package com.gokcank.curalis.presentation.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.BuildConfig
import com.gokcank.curalis.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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

    // Google Sign-In setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    var signedInAccount by remember { mutableStateOf(GoogleSignIn.getLastSignedInAccount(context)) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            signedInAccount = account
        } catch (e: com.google.android.gms.common.api.ApiException) {
            val errorMsg = if (e.statusCode == 10) {
                String.format(googleSignInErrorOauthTemplate, e.statusCode)
            } else {
                String.format(
                    googleSignInErrorGenericTemplate,
                    e.statusCode,
                    e.localizedMessage ?: unknownErrorText
                )
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorMsg)
            }
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(String.format(signInErrorGenericTemplate, e.localizedMessage))
            }
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

                    if (signedInAccount == null) {
                        Button(
                            onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text(stringResource(R.string.sign_in_with_google_button))
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.connected_account_label, signedInAccount?.email ?: stringResource(R.string.unknown_account)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { pendingPasswordAction = PendingPasswordAction.BackupDrive },
                                modifier = Modifier.weight(1f),
                                enabled = uiState !is BackupUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(stringResource(R.string.backup_to_drive_button))
                            }
                            OutlinedButton(
                                onClick = { pendingPasswordAction = PendingPasswordAction.RestoreDrive },
                                modifier = Modifier.weight(1f),
                                enabled = uiState !is BackupUiState.Loading,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(stringResource(R.string.restore_from_drive_button))
                            }
                        }
                        TextButton(
                            onClick = {
                                googleSignInClient.signOut().addOnCompleteListener {
                                    signedInAccount = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.sign_out_button))
                        }
                    }
                }
            }
        }
    }

    pendingPasswordAction?.let { action ->
        val isSettingPassword = action is PendingPasswordAction.BackupLocal || action is PendingPasswordAction.BackupDrive
        BackupPasswordDialog(
            isSettingPassword = isSettingPassword,
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
                    PendingPasswordAction.BackupDrive -> {
                        signedInAccount?.let { viewModel.uploadToGoogleDrive(it, password) }
                    }
                    PendingPasswordAction.RestoreDrive -> {
                        signedInAccount?.let { viewModel.downloadFromGoogleDrive(it, password) }
                    }
                }
                pendingPasswordAction = null
            }
        )
    }
}

private sealed class PendingPasswordAction {
    data object BackupLocal : PendingPasswordAction()
    data object RestoreLocal : PendingPasswordAction()
    data object BackupDrive : PendingPasswordAction()
    data object RestoreDrive : PendingPasswordAction()
}

@Composable
private fun BackupPasswordDialog(
    isSettingPassword: Boolean,
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
                        text = stringResource(R.string.backup_password_set_desc),
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
