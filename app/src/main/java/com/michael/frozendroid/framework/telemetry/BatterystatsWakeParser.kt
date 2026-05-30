package com.michael.frozendroid.framework.telemetry

import com.michael.frozendroid.domain.model.CpuWakeEvent

object BatterystatsWakeParser {
    private val uidHeaderPattern = Regex("""^\s*(?:Uid\s+)?(u\d+a\d+|\d+):.*$""")
    private val wakeLockPattern = Regex("""^\s*Wake lock .+?:\s+(.+?)\s+partial\s+\((\d+)\s+times?\).*$""")
    private val durationPartPattern = Regex("""(\d+(?:\.\d+)?)(ms|s|m|h|d)""")

    fun parse(
        output: String,
        uidToPackages: Map<Int, List<String>>,
        timestamp: Long
    ): List<CpuWakeEvent> {
        val eventsByPackage = linkedMapOf<String, WakeTotals>()
        var currentUid: Int? = null

        output.lineSequence().forEach { line ->
            val uidMatch = uidHeaderPattern.matchEntire(line)
            if (uidMatch != null) {
                currentUid = parseUid(uidMatch.groupValues[1])
                return@forEach
            }

            val uid = currentUid ?: return@forEach
            val wakeMatch = wakeLockPattern.matchEntire(line) ?: return@forEach
            val packages = uidToPackages[uid].orEmpty()
            if (packages.isEmpty()) return@forEach

            val durationMs = parseDurationMs(wakeMatch.groupValues[1])
            val count = wakeMatch.groupValues[2].toIntOrNull() ?: return@forEach
            packages.forEach { packageName ->
                val totals = eventsByPackage.getOrPut(packageName) { WakeTotals() }
                totals.wakeCount += count
                totals.durationMs += durationMs
            }
        }

        return eventsByPackage.map { (packageName, totals) ->
            CpuWakeEvent(
                packageName = packageName,
                timestamp = timestamp,
                wakeCount = totals.wakeCount,
                durationMs = totals.durationMs,
                batteryDelta = 0f
            )
        }
    }

    private fun parseUid(value: String): Int? {
        if (value.all { it.isDigit() }) return value.toIntOrNull()

        val match = Regex("""u(\d+)a(\d+)""").matchEntire(value) ?: return null
        val userId = match.groupValues[1].toIntOrNull() ?: return null
        val appId = match.groupValues[2].toIntOrNull() ?: return null
        return userId * PER_USER_RANGE + FIRST_APPLICATION_UID + appId
    }

    private fun parseDurationMs(value: String): Long {
        return durationPartPattern.findAll(value).sumOf { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: 0.0
            when (match.groupValues[2]) {
                "d" -> amount * 24 * 60 * 60 * 1000
                "h" -> amount * 60 * 60 * 1000
                "m" -> amount * 60 * 1000
                "s" -> amount * 1000
                else -> amount
            }.toLong()
        }
    }

    private class WakeTotals {
        var wakeCount: Int = 0
        var durationMs: Long = 0L
    }

    private const val FIRST_APPLICATION_UID = 10_000
    private const val PER_USER_RANGE = 100_000
}
