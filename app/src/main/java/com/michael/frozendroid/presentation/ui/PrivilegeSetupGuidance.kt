package com.michael.frozendroid.presentation.ui

object PrivilegeSetupGuidance {
    const val title = "Enable a privilege mode first"

    val message = """
        FrozenDroid needs Shizuku or ADB before it can freeze apps.

        Shizuku (recommended):
        1. Install Shizuku from Google Play.
        2. Enable Developer Options: Settings > About phone > tap Build number 7 times.
        3. Open Developer Options and turn on USB Debugging and Wireless Debugging.
        4. Open Shizuku > Pairing > Developer Options > Wireless Debugging > Pair device with pairing code.
        5. Enter the pairing code in Shizuku, tap Start, then allow FrozenDroid when prompted.

        ADB over Wi-Fi:
        1. Connect your phone to your computer with USB, with both on the same Wi-Fi.
        2. In a terminal, run: adb tcpip 5555
        3. Find your phone IP in Settings > About phone > Status.
        4. Unplug USB, then run: adb connect <phone_ip>:5555
        5. Return to FrozenDroid and try Freeze again.
    """.trimIndent()
}
