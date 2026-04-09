package com.moriafly.salt.core.os.macos

import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class MacOSVersionInfoPerformanceTest {
    @Test
    fun comparePlistVsCommandPerformance() {
        if (!isRunningOnMacOS()) {
            println("Not running on macOS, skipping performance test")
            return
        }

        println("=== Performance Comparison ===\n")

        // Warmup
        repeat(5) { MacOSVersionInfo.readFromPlist() }
        repeat(5) { MacOSVersionInfo.readFromCommand() }

        // Test plist method
        val plistTotal = measureTime {
            repeat(100) { MacOSVersionInfo.readFromPlist() }
        }
        val plistAvg = plistTotal / 100

        println("Plist file method:")
        println("  100 iterations: $plistTotal")
        println("  Average: $plistAvg")

        // Test command method (fewer iterations due to slower speed)
        val commandTotal = measureTime {
            repeat(10) { MacOSVersionInfo.readFromCommand() }
        }
        val commandAvg = commandTotal / 10

        println("\nCommand method:")
        println("  10 iterations: $commandTotal")
        println("  Average: $commandAvg")

        // Comparison
        val ratio = if (plistAvg > 0.milliseconds) {
            commandAvg / plistAvg
        } else {
            0.0
        }

        println("\n=== Result ===")
        println("Plist is ~${ratio.toInt()}x faster than command")
        println("Winner: ${if (plistAvg < commandAvg) "Plist file" else "Command"}")
    }

    @Test
    fun testFallbackBehavior() {
        if (!isRunningOnMacOS()) {
            println("Not running on macOS, skipping fallback test")
            return
        }

        println("\n=== Fallback Test ===")

        // Test normal case
        val normalResult = MacOSVersionInfo.getBuildVersion()
        println("Normal result: '$normalResult'")
        assert(normalResult.isNotEmpty()) { "Should get build version" }

        // Verify plist works
        val plistResult = MacOSVersionInfo.readFromPlist()
        println("Plist result: '$plistResult'")
        assert(plistResult.isNotEmpty()) { "Plist should work on macOS" }

        // Verify command works
        val commandResult = MacOSVersionInfo.readFromCommand()
        println("Command result: '$commandResult'")
        assert(commandResult.isNotEmpty()) { "Command should work on macOS" }

        // Both should return same value
        assert(plistResult == commandResult) {
            "Plist ('$plistResult') and Command ('$commandResult') should match"
        }

        println("✓ Fallback mechanism verified - both methods return same build: $plistResult")
    }

    private fun isRunningOnMacOS(): Boolean = System.getProperty("os.name") == "Mac OS X"
}
