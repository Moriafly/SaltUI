/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moriafly.salt.ui.platform.macos

import com.sun.jna.Pointer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MacOSWindowVibrancyTest {
    @Test
    fun failedInstallFallsBackToNormalBackground() {
        val backend = FakeVibrancyBackend().apply {
            installResult = null
        }
        val vibrancy = vibrancy(backend)

        assertFalse(vibrancy.update(enabled = true, isDarkTheme = false))
        assertEquals(1, backend.installCount)
        assertEquals(0, backend.removeCount)
    }

    @Test
    fun activeInstallationUpdatesAppearanceAndCanBeRemoved() {
        val backend = FakeVibrancyBackend()
        val vibrancy = vibrancy(backend)

        assertTrue(vibrancy.update(enabled = true, isDarkTheme = false))
        assertTrue(vibrancy.update(enabled = true, isDarkTheme = true))
        assertEquals(1, backend.installCount)
        assertEquals(1, backend.appearanceUpdateCount)

        assertFalse(vibrancy.update(enabled = false, isDarkTheme = true))
        assertEquals(1, backend.removeCount)
    }

    @Test
    fun disposeRemovesActiveInstallation() {
        val backend = FakeVibrancyBackend()
        val vibrancy = vibrancy(backend)

        assertTrue(vibrancy.update(enabled = true, isDarkTheme = false))
        assertFalse(vibrancy.dispose())
        assertEquals(1, backend.removeCount)
    }

    @Test
    fun failedRemovalKeepsInstallationActiveForRetry() {
        val backend = FakeVibrancyBackend()
        val vibrancy = vibrancy(backend)

        assertTrue(vibrancy.update(enabled = true, isDarkTheme = false))
        backend.failRemove = true
        assertTrue(vibrancy.update(enabled = false, isDarkTheme = false))

        backend.failRemove = false
        assertFalse(vibrancy.dispose())
        assertEquals(2, backend.removeCount)
    }

    private fun vibrancy(backend: MacOSVibrancyBackend) = MacOSWindowVibrancy(
        windowHandle = { 42L },
        backend = backend
    )
}

private class FakeVibrancyBackend : MacOSVibrancyBackend {
    var installResult: MacOSWindowVibrancy.Installation? = installation()
    var failRemove = false
    var installCount = 0
    var appearanceUpdateCount = 0
    var removeCount = 0

    override fun install(
        nativeWindow: Pointer,
        isDarkTheme: Boolean
    ): MacOSWindowVibrancy.Installation? {
        installCount++
        return installResult
    }

    override fun updateAppearance(effectView: Pointer, isDarkTheme: Boolean) {
        appearanceUpdateCount++
    }

    override fun remove(installation: MacOSWindowVibrancy.Installation) {
        removeCount++
        if (failRemove) error("remove failed")
    }

    private fun installation() = MacOSWindowVibrancy.Installation(
        nativeWindow = Pointer(1L),
        effectView = Pointer(2L),
        constraints = emptyList(),
        contentLayer = Pointer(3L),
        originalContentLayerIsOpaque = true,
        originalBackgroundColor = Pointer(4L),
        originalIsOpaque = true
    )
}
