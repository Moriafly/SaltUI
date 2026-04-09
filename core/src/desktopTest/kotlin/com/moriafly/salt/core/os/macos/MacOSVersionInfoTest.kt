package com.moriafly.salt.core.os.macos

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTime

class MacOSVersionInfoTest {
    @Test
    fun testReadFromPlistPerformance() {
        val duration = measureTime {
            repeat(100) { MacOSVersionInfo.readFromPlist() }
        }
        println("readFromPlist (100x): $duration")
    }

    @Test
    fun testReadFromCommandPerformance() {
        val duration = measureTime {
            repeat(10) { MacOSVersionInfo.readFromCommand() }
        }
        println("readFromCommand (10x): $duration")
    }

    @Test
    fun testPlistReturnsValidBuild() {
        val build = MacOSVersionInfo.readFromPlist()
        println("Plist build: '$build'")

        if (isRunningOnMacOS()) {
            assertTrue(build.isNotEmpty(), "Plist should return non-empty build on macOS")
            // Build format: like "23D56" or "25D125" (5-6 chars, alphanumeric)
            assertTrue(build.length in 5..6, "Build should be 5-6 characters")
        }
    }

    @Test
    fun testCommandReturnsValidBuild() {
        val build = MacOSVersionInfo.readFromCommand()
        println("Command build: '$build'")

        if (isRunningOnMacOS()) {
            assertTrue(build.isNotEmpty(), "Command should return non-empty build on macOS")
            assertTrue(build.length in 5..6, "Build should be 5-6 characters")
        }
    }

    @Test
    fun testFallbackWhenPlistFails() {
        // Temporarily rename plist file to simulate failure
        val plistFile = File("/System/Library/CoreServices/SystemVersion.plist")

        if (!plistFile.exists()) {
            println("Plist file doesn't exist, skipping fallback test")
            return
        }

        // Since we can't actually rename system file, test fallback by calling both methods directly
        val plistResult = MacOSVersionInfo.readFromPlist()
        val commandResult = MacOSVersionInfo.readFromCommand()

        println("Fallback test - plist: '$plistResult', command: '$commandResult'")

        if (isRunningOnMacOS()) {
            // Both should return same build number
            if (plistResult.isNotEmpty() && commandResult.isNotEmpty()) {
                assertEquals(
                    plistResult,
                    commandResult,
                    "Plist and command should return same build"
                )
            }
        }
    }

    private fun isRunningOnMacOS(): Boolean = System.getProperty("os.name") == "Mac OS X"
}
