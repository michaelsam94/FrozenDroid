package com.michael.frozendroid.domain.usecase

object FreezeSafetyPolicy {
    private val packageNamePattern = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+")

    private val blockedExactPackages = setOf(
        "android",
        "com.android.bluetooth",
        "com.android.carrierconfig",
        "com.android.cellbroadcastreceiver",
        "com.android.certinstaller",
        "com.android.dialer",
        "com.android.emergency",
        "com.android.externalstorage",
        "com.android.inputdevices",
        "com.android.keychain",
        "com.android.launcher",
        "com.android.localtransport",
        "com.android.location.fused",
        "com.android.managedprovisioning",
        "com.android.networkstack",
        "com.android.nfc",
        "com.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.android.phone",
        "com.android.providers.downloads",
        "com.android.providers.media",
        "com.android.providers.settings",
        "com.android.providers.telephony",
        "com.android.proxyhandler",
        "com.android.se",
        "com.android.server.telecom",
        "com.android.settings",
        "com.android.shell",
        "com.android.simappdialog",
        "com.android.systemui",
        "com.android.traceur",
        "com.android.vending",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.google.android.packageinstaller",
        "com.michael.frozendroid"
    )

    private val blockedPrefixes = listOf(
        "com.android.apex.",
        "com.android.providers.",
        "com.google.android.cellbroadcast",
        "com.google.android.networkstack",
        "com.google.android.permissioncontroller",
        "com.qualcomm.qcril",
        "com.samsung.android.providers.",
        "com.samsung.android.server.",
        "com.samsung.android.telecom",
        "com.samsung.android.incallui"
    )

    fun validatePackageName(packageName: String): String? {
        return when {
            !packageNamePattern.matches(packageName) -> "Invalid package name."
            else -> null
        }
    }

    fun blockReason(packageName: String): String? {
        validatePackageName(packageName)?.let { return it }

        return when {
            packageName in blockedExactPackages ->
                "Operation blocked: $packageName is a core Android or Google service required for device stability."
            blockedPrefixes.any { packageName.startsWith(it) } ->
                "Operation blocked: $packageName belongs to a protected system-service family."
            else -> null
        }
    }
}
