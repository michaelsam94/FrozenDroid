package com.michael.frozendroid.domain.usecase

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FreezeSafetyPolicyTest {
    @Test
    fun `blocks core android package`() {
        val reason = FreezeSafetyPolicy.blockReason("android")

        assertNotNull(reason)
    }

    @Test
    fun `blocks protected system service families`() {
        val reason = FreezeSafetyPolicy.blockReason("com.android.providers.settings.module")

        assertNotNull(reason)
    }

    @Test
    fun `allows normal third party package names`() {
        val reason = FreezeSafetyPolicy.blockReason("com.example.reader")

        assertNull(reason)
    }

    @Test
    fun `rejects invalid package names before command execution`() {
        val reason = FreezeSafetyPolicy.validatePackageName("com.example.reader;reboot")

        assertNotNull(reason)
    }
}
