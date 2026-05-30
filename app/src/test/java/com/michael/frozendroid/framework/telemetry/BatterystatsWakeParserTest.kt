package com.michael.frozendroid.framework.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatterystatsWakeParserTest {

    @Test
    fun parsesWakeLockCountsAndDurationsByUidPackage() {
        val output = """
            Battery History (200% used, 1024KB used of 2048KB, 10 strings using 512):
            Statistics since last charge:
              Uid u0a123:
                Wake lock SyncManager: 1m 2s 500ms partial (3 times) realtime
                Wake lock UploadService: 500ms partial (1 times) realtime
              Uid 1000:
                Wake lock system-service: 2s 250ms partial (2 times) realtime
        """.trimIndent()

        val events = BatterystatsWakeParser.parse(
            output = output,
            uidToPackages = mapOf(
                10123 to listOf("com.example.sync"),
                1000 to listOf("android")
            ),
            timestamp = 42L
        )

        assertEquals(2, events.size)
        assertEquals("com.example.sync", events[0].packageName)
        assertEquals(4, events[0].wakeCount)
        assertEquals(63_000L, events[0].durationMs)
        assertEquals(0f, events[0].batteryDelta)
        assertEquals(42L, events[0].timestamp)
        assertEquals("android", events[1].packageName)
        assertEquals(2, events[1].wakeCount)
        assertEquals(2_250L, events[1].durationMs)
    }

    @Test
    fun returnsNoEventsWhenWakeLocksAreUnavailable() {
        val events = BatterystatsWakeParser.parse(
            output = "Estimated power use unavailable",
            uidToPackages = mapOf(10123 to listOf("com.example.sync")),
            timestamp = 42L
        )

        assertTrue(events.isEmpty())
    }
}
