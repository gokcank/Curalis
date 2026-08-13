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
import com.gokcank.curalis.core.notification.AlarmScheduler
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
    lateinit var alarmScheduler: AlarmScheduler

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

        setContent {
            val themeMode by themeController.themeMode.collectAsState()
            var dbHealth by remember { mutableStateOf(DatabaseHealth.CHECKING) }

            // Bu kontrol normalde çok hızlıdır (veritabanı zaten güncel şemadadır);
            // yalnızca DatabaseModule'deki migration zinciri kapsamayan, beklenmedik
            // derecede eski bir veritabanıyla karşılaşılırsa "migration not found"
            // istisnası burada güvenle yakalanır (bkz. DatabaseRecoveryScreen).
            LaunchedEffect(Unit) {
                dbHealth = try {
                    withContext(Dispatchers.IO) { database.openHelper.writableDatabase }
                    DatabaseHealth.OK
                } catch (e: IllegalStateException) {
                    Log.e("MainActivity", "Veritabanı açılamadı, kurtarma ekranı gösteriliyor", e)
                    DatabaseHealth.UNREADABLE
                }
            }

            val isLocked by appLockController.isLocked.collectAsState()
            var hasCompletedOnboarding by remember { mutableStateOf(onboardingPreferences.hasCompletedOnboarding) }

            CuralisTheme(themeMode = themeMode) {
                when {
                    dbHealth == DatabaseHealth.CHECKING -> DatabaseCheckingScreen()
                    dbHealth == DatabaseHealth.UNREADABLE -> DatabaseRecoveryScreen(
                        onResetClick = { resetDatabaseAndRestart() }
                    )
                    // Hesap gerektirmeyen, tek seferlik karşılama; veritabanı zaten
                    // hazır olduğu için bundan sonra gösterilir.
                    !hasCompletedOnboarding -> OnboardingScreen(
                        alarmScheduler = alarmScheduler,
                        onFinished = {
                            onboardingPreferences.hasCompletedOnboarding = true
                            hasCompletedOnboarding = true
                        }
                    )
                    // Kilit ekranı, uygulamanın geri kalanı hiç oluşturulmadan önce gelir;
                    // böylece son görüntülenen ekran kilitliyken arkada görünmez.
                    isLocked -> AppLockScreen()
                    else -> NavGraph()
                }
            }
        }
    }

    private fun resetDatabaseAndRestart() {
        database.close()
        deleteDatabase(CuralisDatabase.DATABASE_NAME)

        val restartIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(restartIntent)
        Runtime.getRuntime().exit(0)
    }
}
