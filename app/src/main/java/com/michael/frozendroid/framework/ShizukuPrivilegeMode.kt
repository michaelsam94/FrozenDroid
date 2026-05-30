package com.michael.frozendroid.framework

import android.content.pm.PackageManager

object ShizukuPrivilegeMode {
    const val MODE = "SHIZUKU"

    fun resolve(isBinderRunning: Boolean, permissionResult: Int): String? {
        return if (isBinderRunning && permissionResult == PackageManager.PERMISSION_GRANTED) {
            MODE
        } else {
            null
        }
    }
}
