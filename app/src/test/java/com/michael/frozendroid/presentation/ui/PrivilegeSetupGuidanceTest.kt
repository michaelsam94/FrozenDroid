package com.michael.frozendroid.presentation.ui

import org.junit.Assert.assertTrue
import org.junit.Test

class PrivilegeSetupGuidanceTest {
    @Test
    fun `permission guidance explains Shizuku and ADB setup`() {
        val guidance = PrivilegeSetupGuidance.message

        assertTrue(guidance.contains("Shizuku"))
        assertTrue(guidance.contains("Install Shizuku"))
        assertTrue(guidance.contains("Developer Options"))
        assertTrue(guidance.contains("Wireless Debugging"))
        assertTrue(guidance.contains("ADB"))
        assertTrue(guidance.contains("adb tcpip 5555"))
        assertTrue(guidance.contains("adb connect"))
    }
}
