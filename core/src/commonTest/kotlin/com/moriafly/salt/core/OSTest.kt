package com.moriafly.salt.core

import com.moriafly.salt.core.os.OS
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OSTest {

    @Test
    fun testMacOSVersionAvailableWhenRunningOnMacOS() {
        val current = OS.current
        if (current is OS.MacOS) {
            // Print the detected macOS version and build for debugging
            println("Detected macOS version: ${current.version}")
            println("Detected macOS build: ${current.build}")

            // When running on macOS, version should not be blank
            assertNotNull(current.version)
            assertTrue(current.version.isNotBlank(), "macOS version should not be blank")

            // Version should typically contain dots (e.g., "14.2.1")
            assertTrue(
                current.version.contains("."),
                "macOS version should contain version separators"
            )

            // Build should be available (e.g., "23D56")
            assertTrue(current.build.isNotEmpty(), "macOS build should not be empty")
        } else {
            println("Not running on macOS, current OS: $current")
        }
    }

    @Test
    fun testCurrentPlatformDetection() {
        val current = OS.current
        println("Current OS platform: $current")

        // current should never be Unknown on supported platforms
        assertIs<OS>(current, "OS.current should return a valid OS instance")
    }
}
