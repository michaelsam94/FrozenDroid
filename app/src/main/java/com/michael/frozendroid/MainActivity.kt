package com.michael.frozendroid

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.michael.frozendroid.data.preferences.UserPreferencesManager
import com.michael.frozendroid.framework.ShizukuPrivilegeMode
import com.michael.frozendroid.presentation.ui.FrozenDroidNavHost
import com.michael.frozendroid.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferencesManager: UserPreferencesManager

    private val shizukuPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            lifecycleScope.launch {
                setShizukuModeIfReady(grantResult)
            }
        }

    private val shizukuBinderReceivedListener =
        Shizuku.OnBinderReceivedListener {
            syncShizukuPrivilegeMode()
        }

    private val shizukuBinderDeadListener =
        Shizuku.OnBinderDeadListener {
            lifecycleScope.launch {
                if (preferencesManager.privilegeModeFlow.first() == ShizukuPrivilegeMode.MODE) {
                    preferencesManager.setPrivilegeMode("DEGRADED")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerShizukuListeners()
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    FrozenDroidNavHost(navController = navController)
                }
            }
        }
    }

    override fun onDestroy() {
        runCatching {
            Shizuku.removeRequestPermissionResultListener(shizukuPermissionResultListener)
            Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
            Shizuku.removeBinderDeadListener(shizukuBinderDeadListener)
        }
        super.onDestroy()
    }

    private fun registerShizukuListeners() {
        runCatching {
            Shizuku.addRequestPermissionResultListener(shizukuPermissionResultListener)
            Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
            Shizuku.addBinderDeadListener(shizukuBinderDeadListener)
        }
    }

    private fun syncShizukuPrivilegeMode() {
        try {
            if (Shizuku.isPreV11() || !Shizuku.pingBinder()) {
                return
            }

            val permissionResult = Shizuku.checkSelfPermission()
            lifecycleScope.launch {
                setShizukuModeIfReady(permissionResult)
            }

            if (permissionResult != PackageManager.PERMISSION_GRANTED &&
                !Shizuku.shouldShowRequestPermissionRationale()
            ) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            }
        } catch (e: Exception) {
            // Shizuku is not installed, not running, or not ready yet.
        }
    }

    private suspend fun setShizukuModeIfReady(permissionResult: Int) {
        val mode = ShizukuPrivilegeMode.resolve(
            isBinderRunning = runCatching { Shizuku.pingBinder() }.getOrDefault(false),
            permissionResult = permissionResult
        )

        if (mode != null) {
            preferencesManager.setPrivilegeMode(mode)
        } else if (preferencesManager.privilegeModeFlow.first() == ShizukuPrivilegeMode.MODE) {
            preferencesManager.setPrivilegeMode("DEGRADED")
        }
    }

    private companion object {
        const val SHIZUKU_PERMISSION_REQUEST_CODE = 1001
    }
}
