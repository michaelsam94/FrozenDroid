package com.michael.frozendroid.framework

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.Keep
import com.michael.frozendroid.IPackageCommands
import java.io.BufferedReader
import java.io.InputStreamReader

class PackageCommandsService() : IPackageCommands.Stub() {
    @Keep
    constructor(context: Context) : this()

    override fun disablePackage(packageName: String): Boolean {
        return runShellCommand("pm disable-user --user 0 $packageName") == 0
    }

    override fun enablePackage(packageName: String): Boolean {
        return runShellCommand("pm enable $packageName") == 0
    }

    override fun forceStop(packageName: String) {
        runShellCommand("am force-stop $packageName")
    }

    override fun getPackageState(packageName: String): Int {
        return try {
            val process = ProcessBuilder("sh", "-c", "pm list packages -d $packageName")
                .redirectErrorStream(true)
                .start()
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            process.waitFor()
            if (output.contains(packageName)) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            }
        } catch (e: Exception) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        }
    }

    override fun executeShellCommand(command: String): String {
        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exitCode = process.waitFor()
            if (exitCode == 0) output else ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun runShellCommand(command: String): Int {
        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            process.waitFor()
        } catch (e: Exception) {
            -1
        }
    }
}
