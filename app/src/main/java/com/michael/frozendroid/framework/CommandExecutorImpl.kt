package com.michael.frozendroid.framework

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import com.michael.frozendroid.BuildConfig
import com.michael.frozendroid.IPackageCommands
import com.michael.frozendroid.data.preferences.UserPreferencesManager
import com.michael.frozendroid.domain.repository.CommandExecutor
import com.michael.frozendroid.domain.repository.CommandResult
import com.michael.frozendroid.domain.usecase.FreezeSafetyPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CommandExecutorImpl @Inject constructor(
    private val context: Context,
    private val preferencesManager: UserPreferencesManager
) : CommandExecutor {

    private val TAG = "CommandExecutor"
    private var shizukuService: IPackageCommands? = null
    private var isAdbConnected = false

    // Shizuku binder connection retry loop
    private suspend fun getShizukuServiceWithRetry(): IPackageCommands? {
        shizukuService?.let { return it }

        var attempts = 3
        while (attempts > 0) {
            try {
                val binder = queryShizukuBinder()
                if (binder != null) {
                    val service = IPackageCommands.Stub.asInterface(binder)
                    binder.linkToDeath({
                        Log.e(TAG, "Shizuku binder has died. Resetting references...")
                        shizukuService = null
                    }, 0)
                    shizukuService = service
                    return service
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed binding Shizuku IPC service. Attempts remaining: ${attempts - 1}", e)
            }
            attempts--
            if (attempts > 0) {
                delay(2000) // 2s delay as requested in Section 3
            }
        }
        return null
    }

    private suspend fun queryShizukuBinder(): IBinder? {
        if (!isShizukuReady()) {
            return null
        }

        val args = Shizuku.UserServiceArgs(ComponentName(context, PackageCommandsService::class.java))
            .processNameSuffix("package_commands")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
            .daemon(false)

        return withTimeoutOrNull(5000) {
            suspendCancellableCoroutine { continuation ->
                val connection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        if (continuation.isActive) {
                            continuation.resume(service)
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        shizukuService = null
                    }
                }

                continuation.invokeOnCancellation {
                    runCatching { Shizuku.unbindUserService(args, connection, true) }
                }

                runCatching {
                    Shizuku.bindUserService(args, connection)
                }.onFailure {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    private fun isShizukuReady(): Boolean {
        return try {
            val mode = ShizukuPrivilegeMode.resolve(
                isBinderRunning = Shizuku.pingBinder(),
                permissionResult = Shizuku.checkSelfPermission()
            )
            mode == ShizukuPrivilegeMode.MODE
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun freeze(packageName: String): CommandResult = withContext(Dispatchers.IO) {
        FreezeSafetyPolicy.blockReason(packageName)?.let { return@withContext CommandResult.Failure(-1, it) }

        val mode = resolvePrivilegeMode()
        Log.d(TAG, "Requested freeze for $packageName using mode $mode")

        when (mode) {
            "SHIZUKU" -> {
                val service = getShizukuServiceWithRetry()
                if (service != null) {
                    try {
                        val success = service.disablePackage(packageName)
                        if (success && isPackageFrozen(packageName)) {
                            CommandResult.Success("Suspended via Shizuku binder.")
                        } else if (success) {
                            CommandResult.Failure(-4, "Freeze command returned success, but Android still reports the package as active.")
                        } else {
                            CommandResult.Failure(-1, "Binder report: package could not be disabled.")
                        }
                    } catch (e: Exception) {
                        CommandResult.Failure(-2, "Binder IPC Exception: ${e.localizedMessage}")
                    }
                } else {
                    CommandResult.Failure(-3, "BinderError: Shizuku binder service connection died after 3 retries.")
                }
            }
            "ADB" -> {
                // ADB over Wi-Fi executes pm disable-user --user 0
                val result = runShellCommand("pm disable-user --user 0 $packageName")
                if (result is CommandResult.Success && !isPackageFrozen(packageName)) {
                    CommandResult.Failure(-4, "Freeze command completed, but Android still reports the package as active.")
                } else {
                    result
                }
            }
            else -> {
                CommandResult.PermissionDenied
            }
        }
    }

    override suspend fun unfreeze(packageName: String): CommandResult = withContext(Dispatchers.IO) {
        FreezeSafetyPolicy.validatePackageName(packageName)?.let { return@withContext CommandResult.Failure(-1, it) }

        val mode = resolvePrivilegeMode()
        Log.d(TAG, "Requested unfreeze for $packageName using mode $mode")

        when (mode) {
            "SHIZUKU" -> {
                val service = getShizukuServiceWithRetry()
                if (service != null) {
                    try {
                        val success = service.enablePackage(packageName)
                        if (success && !isPackageFrozen(packageName)) {
                            CommandResult.Success("Active via Shizuku binder.")
                        } else if (success) {
                            CommandResult.Failure(-4, "Unfreeze command returned success, but Android still reports the package as frozen.")
                        } else {
                            CommandResult.Failure(-1, "Binder report: package could not be enabled.")
                        }
                    } catch (e: Exception) {
                        CommandResult.Failure(-2, "Binder IPC Exception: ${e.localizedMessage}")
                    }
                } else {
                    CommandResult.Failure(-3, "BinderError: Shizuku binder service connection died after 3 retries.")
                }
            }
            "ADB" -> {
                val result = runShellCommand("pm enable $packageName")
                if (result is CommandResult.Success && isPackageFrozen(packageName)) {
                    CommandResult.Failure(-4, "Unfreeze command completed, but Android still reports the package as frozen.")
                } else {
                    result
                }
            }
            else -> {
                CommandResult.PermissionDenied
            }
        }
    }

    override suspend fun forceStop(packageName: String): CommandResult = withContext(Dispatchers.IO) {
        FreezeSafetyPolicy.blockReason(packageName)?.let { return@withContext CommandResult.Failure(-1, it) }

        val mode = resolvePrivilegeMode()
        when (mode) {
            "SHIZUKU" -> {
                val service = getShizukuServiceWithRetry()
                if (service != null) {
                    service.forceStop(packageName)
                    CommandResult.Success("Force stopped via Shizuku binder.")
                } else {
                    CommandResult.Failure(-3, "Shizuku service died.")
                }
            }
            "ADB" -> {
                runShellCommand("am force-stop $packageName")
            }
            else -> CommandResult.PermissionDenied
        }
    }

    override suspend fun getPackageState(packageName: String): Int = withContext(Dispatchers.IO) {
        val mode = resolvePrivilegeMode()
        if (mode == "SHIZUKU") {
            val service = getShizukuServiceWithRetry()
            service?.getPackageState(packageName) ?: 1
        } else {
            if (isPackageFrozen(packageName)) 2 else 1
        }
    }

    private fun runShellCommand(shellCmd: String): CommandResult {
        try {
            val process = ProcessBuilder("sh", "-c", shellCmd)
                .redirectErrorStream(false)
                .start()
            val exitCode = process.waitFor()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            
            if (exitCode == 0) {
                return CommandResult.Success(output)
            } else {
                val errReader = BufferedReader(InputStreamReader(process.errorStream))
                val errText = errReader.readText()
                return CommandResult.Failure(exitCode, errText)
            }
        } catch (e: Exception) {
            return CommandResult.Failure(-5, e.localizedMessage ?: "Failed to execute package command.")
        }
    }

    private fun isPackageFrozen(packageName: String): Boolean {
        return try {
            when (context.packageManager.getApplicationEnabledSetting(packageName)) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> true
                else -> {
                    val info = context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.MATCH_DISABLED_COMPONENTS
                    )
                    !info.enabled
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun resolvePrivilegeMode(): String {
        val storedMode = preferencesManager.privilegeModeFlow.first()
        if (storedMode == ShizukuPrivilegeMode.MODE || storedMode == "ADB") {
            return storedMode
        }

        return if (isShizukuReady()) {
            preferencesManager.setPrivilegeMode(ShizukuPrivilegeMode.MODE)
            ShizukuPrivilegeMode.MODE
        } else {
            storedMode
        }
    }

    // ADB Wi-Fi Keep-alive ping loop runs every 4 minutes as requested
    suspend fun startAdbKeepAlivePing() = withContext(Dispatchers.IO) {
        while (true) {
            val mode = preferencesManager.privilegeModeFlow.first()
            if (mode == "ADB") {
                Log.d(TAG, "ADB WiFi Keep-alive: Sending ping...")
                runShellCommand("echo 'ping'")
            }
            delay(4 * 60 * 1000) // 4 minutes interval
        }
    }
}
