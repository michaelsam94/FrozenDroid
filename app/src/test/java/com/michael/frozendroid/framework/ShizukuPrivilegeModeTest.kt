package com.michael.frozendroid.framework

import android.content.pm.PackageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShizukuPrivilegeModeTest {
    @Test
    fun `uses Shizuku mode when binder is running and permission is granted`() {
        val mode = ShizukuPrivilegeMode.resolve(
            isBinderRunning = true,
            permissionResult = PackageManager.PERMISSION_GRANTED
        )

        assertEquals("SHIZUKU", mode)
    }

    @Test
    fun `does not use Shizuku mode without permission`() {
        val mode = ShizukuPrivilegeMode.resolve(
            isBinderRunning = true,
            permissionResult = PackageManager.PERMISSION_DENIED
        )

        assertNull(mode)
    }
}
