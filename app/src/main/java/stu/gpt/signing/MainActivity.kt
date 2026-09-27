package stu.gpt.signing

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import stu.gpt.signing.ui.AdminScreen
import stu.gpt.signing.ui.SignInScreen
import stu.gpt.signing.ui.ScreenSaver
import stu.gpt.signing.ui.theme.SigningTheme
import stu.gpt.signing.data.Repository
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.content.pm.ActivityInfo
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize repository persistence & retention
        Repository.initialize(this)
        // Apply initial orientation based on current setting
        requestedOrientation = if (Repository.settings.isScreenFlipped) {
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        enableEdgeToEdge()
        
        // AUTO-START OPTIMIZATION: Allow app to show over the lock screen and turn the screen on.
        // This ensures the app is visible even if the tablet is at the "swipe to unlock" screen.
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val kgm = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        kgm.requestDismissKeyguard(this, null)

        setContent { AppRoot(onExit = { finish() }) }
    }

    override fun onResume() {
        super.onResume()
        // Re-apply orientation on resume to ensure emulator/devices keep the configured flip
        requestedOrientation = if (Repository.settings.isScreenFlipped) {
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}

private enum class Screen { SignIn, Admin }

@Composable
private fun AppRoot(onExit: () -> Unit) {
    SigningTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            val context = LocalContext.current
            val screen = remember { mutableStateOf(Screen.SignIn) }
            val lastTick = remember { mutableStateOf(System.currentTimeMillis()) }
            val showPermissionDialog = remember { mutableStateOf(!Settings.canDrawOverlays(context)) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(10000) // check every 10 seconds
                    lastTick.value = System.currentTimeMillis()
                    // Periodic "Passive" Date Check: flip to dark mode if show ended at midnight
                    Repository.checkAndApplyPassiveReset()
                }
            }

            val isAutoScreenSaver = remember {
                derivedStateOf {
                    // REQUIREMENT: Only show if enabled and everyone has signed out
                    if (!Repository.settings.screenSaverEnabled) return@derivedStateOf false
                    if (Repository.totalSignedInCount > 0) return@derivedStateOf false
                    
                    val now = lastTick.value
                    val fiveMinutes = 5 * 60 * 1000L
                    (now - Repository.settings.lastSignOutTime) >= fiveMinutes
                }
            }

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (screen.value) {
                    Screen.SignIn -> SignInScreen(
                        onAdminDirectSuccess = { 
                            Repository.updateActivityTime()
                            screen.value = Screen.Admin
                        },
                        onSignInSuccess = { 
                            Repository.updateActivityTime()
                        }
                    )
                    Screen.Admin -> AdminScreen(
                        onSave = { 
                            Repository.updateActivityTime()
                            screen.value = Screen.SignIn 
                        },
                        onExitApp = onExit,
                        onExportHistory = { 
                            Repository.updateActivityTime()
                        }
                    )
                }
            }

            if (Repository.isManualScreenSaver || isAutoScreenSaver.value) {
                ScreenSaver(onDismiss = {
                    Repository.isManualScreenSaver = false
                    Repository.updateActivityTime()
                })
            }

            if (showPermissionDialog.value) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog.value = false },
                    title = { Text("Auto-Start Setup", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            "To ensure the app starts automatically after a tablet reboot, please complete these two setup steps:\n\n" +
                            "1. 'Display over other apps': Enable permission in the next screen (if disabled, tap top-right 3 dots > 'Allow restricted settings').\n\n" +
                            "2. Battery Optimization: Go to Tablet Settings > Apps > Sign-In v4.0 > Battery, and set it to 'Unrestricted' to prevent background termination.",
                            fontSize = 15.sp
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            showPermissionDialog.value = false
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showPermissionDialog.value = false }) {
                            Text("Later")
                        }
                    }
                )
            }
        }
    }
}
