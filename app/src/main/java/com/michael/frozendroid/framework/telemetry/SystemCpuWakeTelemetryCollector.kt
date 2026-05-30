package com.michael.frozendroid.framework.telemetry

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.michael.frozendroid.BuildConfig
import com.michael.frozendroid.IPackageCommands
import com.michael.frozendroid.data.preferences.UserPreferencesManager
import com.michael.frozendroid.domain.model.CpuWakeEvent
import com.michael.frozendroid.domain.repository.CpuWakeTelemetryCollector
import com.michael.frozendroid.framework.PackageCommandsService
import com.michael.frozendroid.framework.ShizukuPrivilegeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class SystemCpuWakeTelemetryCollector @Inject constructor(
    private val context: Context,
    private val preferencesManager: UserPreferencesManager
) : CpuWakeTelemetryCollector {

    override suspend fun collectWakeEvents(): List<CpuWakeEvent> = withContext(Dispatchers.IO) {
        val output = readBatterystats() ?: return@withContext emptyList()
        BatterystatsWakeParser.parse(
            output = output,
            uidToPackages = installedPackagesByUid(),
            timestamp = System.currentTimeMillis()
        )
    }

    private suspend fun readBatterystats(): String? {
        return when (resolvePrivilegeMode()) {
            ShizukuPrivilegeMode.MODE -> runShizukuShellCommand(BATTERYSTATS_COMMAND)
            "ADB" -> runLocalShellCommand(BATTERYSTATS_COMMAND)
            else -> null
        }
    }

    private fun installedPackagesByUid(): Map<Int, List<String>> {
        return context.packageManager.getInstalledApplications(0)
            .groupBy(
                keySelector = { it.uid },
                valueTransform = { it.packageName }
            )
    }

    private suspend fun runShizukuShellCommand(command: String): String? {
        val service = bindShizukuService() ?: return null
        return runCatching { service.executeShellCommand(command) }
            .onFailure { Log.w(TAG, "Unable to read batterystats via Shizuku", it) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private suspend fun bindShizukuService(): IPackageCommands? {
        if (!isShizukuReady()) return null

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
                            continuation.resume(IPackageCommands.Stub.asInterface(service))
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) = Unit
                }

                continuation.invokeOnCancellation {
                    runCatching { Shizuku.unbindUserService(args, connection, true) }
                }

                runCatching { Shizuku.bindUserService(args, connection) }
                    .onFailure {
                        if (continuation.isActive) continuation.resume(null)
                    }
            }
        }
    }

    private fun runLocalShellCommand(command: String): String? {
        return runCatching {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exitCode = process.waitFor()
            output.takeIf { exitCode == 0 && it.isNotBlank() }
        }.onFailure {
            Log.w(TAG, "Unable to read batterystats locally", it)
        }.getOrNull()
    }

    private suspend fun resolvePrivilegeMode(): String? {
        val storedMode = preferencesManager.privilegeModeFlow.first()
        if (storedMode == ShizukuPrivilegeMode.MODE || storedMode == "ADB") return storedMode
        return ShizukuPrivilegeMode.MODE.takeIf { isShizukuReady() }
    }

    private fun isShizukuReady(): Boolean {
        return runCatching {
            ShizukuPrivilegeMode.resolve(
                isBinderRunning = Shizuku.pingBinder(),
                permissionResult = Shizuku.checkSelfPermission()
            ) == ShizukuPrivilegeMode.MODE
        }.getOrDefault(false)
    }

    private companion object {
        const val TAG = "CpuWakeTelemetry"
        const val BATTERYSTATS_COMMAND = "dumpsys batterystats --charged"
    }
}
