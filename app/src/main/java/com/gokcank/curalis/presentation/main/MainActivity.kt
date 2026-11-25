package com.gokcank.curalis.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gokcank.curalis.core.navigation.NavGraph
import com.gokcank.curalis.core.navigation.OpenDestination
import com.gokcank.curalis.core.navigation.Screen
import com.gokcank.curalis.core.onboarding.OnboardingPreferences
import com.gokcank.curalis.core.security.AppLockController
import com.gokcank.curalis.core.theme.CuralisTheme
import com.gokcank.curalis.core.theme.ThemeController
import com.gokcank.curalis.data.local.CuralisDatabase
import com.gokcank.curalis.presentation.lock.AppLockScreen
import com.gokcank.curalis.presentation.startup.DatabaseCheckingScreen
import com.gokcank.curalis.presentation.startup.DatabaseRecoveryScreen
import com.gokcank.curalis.presentation.startup.OnboardingScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.gokcank.curalis.R
import com.gokcank.curalis.core.notification.StartupReminderRepair
import com.gokcank.curalis.data.local.DatabaseRescue
import com.gokcank.curalis.presentation.startup.DatabaseRescuedDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import javax.inject.Inject

private enum class DatabaseHealth { CHECKING, OK, UNREADABLE }

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var themeController: ThemeController

    @Inject
    lateinit var database: CuralisDatabase

    @Inject
    lateinit var appLockController: AppLockController

    @Inject
    lateinit var onboardingPreferences: OnboardingPreferences

    @Inject
    lateinit var startupReminderRepair: StartupReminderRepair

    /** "Veri dosyasını kaydet" ile dışa aktarılacak dosyalar; seçici açılmadan önce belirlenir. */
    private var filesToExport: List<File> = emptyList()

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> uri?.let { exportTo(it) } }

    /** Bir bildirimin açılmasını istediği ekran; gezinme grafiği hazır olunca bir kez açılır. */
    private var pendingRoute by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        OpenDestination.routeFor(intent)?.let { pendingRoute = it }
    }

    override fun onStart() {
        super.onStart()
        appLockController.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        appLockController.onAppBackgrounded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Modern Android 15+ edge-to-edge support
        enableEdgeToEdge()
        if (savedInstanceState == null) pendingRoute = OpenDestination.routeFor(intent)

        setContent {
            val themeMode by themeController.themeMode.collectAsState()
            var dbHealth by remember { mutableStateOf(DatabaseHealth.CHECKING) }
            var showRescuedNotice by remember { mutableStateOf(false) }

            // Bu kontrol normalde çok hızlıdır (veritabanı zaten güncel şemadadır);
            // yalnızca DatabaseModule'deki migration zinciri kapsamayan, beklenmedik
            // derecede eski bir veritabanıyla karşılaşılırsa "migration not found"
            // istisnası burada güvenle yakalanır (bkz. DatabaseRecoveryScreen).
            LaunchedEffect(Unit) {
                dbHealth = try {
                    withContext(Dispatchers.IO) { database.openHelper.writableDatabase }
                    // Açılırken bozuk bulunup yeniden oluşturulduysa kullanıcıya bir kez söylenir.
                    showRescuedNotice = DatabaseRescue.hasPendingNotice(filesDir)
                    // "Durmaya zorla" alarmları siler; açılışta geri kurulur (süreç başına bir kez).
                    lifecycleScope.launch(Dispatchers.IO) { startupReminderRepair.runOnce() }
                    DatabaseHealth.OK
                } catch (e: IllegalStateException) {
                    Log.e("MainActivity", "Veritabanı açılamadı, kurtarma ekranı gösteriliyor", e)
                    DatabaseHealth.UNREADABLE
                }
            }

            val isLocked by appLockController.isLocked.collectAsState()
            var hasCompletedOnboarding by remember { mutableStateOf(onboardingPreferences.hasCompletedOnboarding) }

            CuralisTheme(themeMode = themeMode) {
                if (showRescuedNotice) {
                    DatabaseRescuedDialog(
                        onExportClick = {
                            exportFiles(DatabaseRescue.latest(filesDir)?.listFiles()?.toList().orEmpty())
                        },
                        onDismiss = {
                            DatabaseRescue.clearPendingNotice(filesDir)
                            showRescuedNotice = false
                        }
                    )
                }
                when {
                    dbHealth == DatabaseHealth.CHECKING -> DatabaseCheckingScreen()
                    dbHealth == DatabaseHealth.UNREADABLE -> DatabaseRecoveryScreen(
                        onResetConfirmed = { resetDatabaseAndRestart() },
                        onExportClick = {
                            exportFiles(DatabaseRescue.databaseFiles(getDatabasePath(CuralisDatabase.DATABASE_NAME)))
                        }
                    )
                    // Hesap gerektirmeyen, tek seferlik karşılama; veritabanı zaten
                    // hazır olduğu için bundan sonra gösterilir.
                    !hasCompletedOnboarding -> OnboardingScreen(
                        onFinished = {
                            onboardingPreferences.hasCompletedOnboarding = true
                            hasCompletedOnboarding = true
                        }
                    )
                    // Kilit ekranı, uygulamanın geri kalanı hiç oluşturulmadan önce gelir;
                    // böylece son görüntülenen ekran kilitliyken arkada görünmez.
                    // PIN unutulup telefon kilidiyle açıldıysa doğrudan yeni PIN belirlemeye gidilir.
                    isLocked -> AppLockScreen(onPinRecovered = { pendingRoute = Screen.AppLockSettings.route })
                    // Kilit açılana kadar gezinme grafiği oluşmadığı için istenen ekran da
                    // kilit açıldıktan sonra açılır.
                    else -> NavGraph(
                        pendingRoute = pendingRoute,
                        onPendingRouteOpened = { pendingRoute = null }
                    )
                }
            }
        }
    }

    private fun exportFiles(files: List<File>) {
        if (files.isEmpty()) {
            Toast.makeText(this, R.string.db_export_failed, Toast.LENGTH_LONG).show()
            return
        }
        filesToExport = files
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        exportLauncher.launch("curalis_veri_dosyasi_$stamp.zip")
    }

    private fun exportTo(uri: Uri) {
        val files = filesToExport
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use { DatabaseRescue.writeZip(files, it) } != null
                } catch (e: Exception) {
                    Log.e("MainActivity", "Veri dosyası dışa aktarılamadı", e)
                    false
                }
            }
            Toast.makeText(
                this@MainActivity,
                if (ok) R.string.db_export_done else R.string.db_export_failed,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun resetDatabaseAndRestart() {
        database.close()
        // Silmeden önce kopya: kullanıcı yanlışlıkla sıfırlasa da dosya kurtarılabilir (ADR-011).
        try {
            DatabaseRescue.copy(this, CuralisDatabase.DATABASE_NAME)
        } catch (e: Exception) {
            Log.e("MainActivity", "Sıfırlamadan önce veritabanının kopyası alınamadı", e)
        }
        deleteDatabase(CuralisDatabase.DATABASE_NAME)

        val restartIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(restartIntent)
        Runtime.getRuntime().exit(0)
    }
}
