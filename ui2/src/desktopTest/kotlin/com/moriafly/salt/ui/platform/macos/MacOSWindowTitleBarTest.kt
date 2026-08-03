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

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.sun.jna.Pointer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(UnstableSaltUiApi::class)
class MacOSWindowTitleBarTest {
    @Test
    fun unavailableWindowDoesNotInstallTitleBar() {
        val backend = FakeTitleBarBackend()
        val titleBar = MacOSWindowTitleBar(
            windowHandle = { 0L },
            backend = backend
        )

        titleBar.update(40f)

        assertEquals(0, backend.installCount)
    }

    @Test
    fun repeatedUpdatesReuseInstallation() {
        val backend = FakeTitleBarBackend()
        val titleBar = titleBar(backend)

        titleBar.update(40f)
        titleBar.update(48f)

        assertEquals(1, backend.installCount)
        assertEquals(1, backend.updateCount)
        assertEquals(48f, backend.lastHeaderHeight)
    }

    @Test
    fun disposeRemovesInstallationOnce() {
        val backend = FakeTitleBarBackend()
        val titleBar = titleBar(backend)

        titleBar.update(40f)
        titleBar.dispose()
        titleBar.dispose()

        assertEquals(1, backend.removeCount)
    }

    @Test
    fun updatesAfterDisposeAreIgnored() {
        val backend = FakeTitleBarBackend()
        val titleBar = titleBar(backend)

        titleBar.update(40f)
        titleBar.dispose()
        titleBar.update(48f)

        assertEquals(1, backend.installCount)
        assertEquals(0, backend.updateCount)
    }

    @Test
    fun failedRemovalCanBeRetried() {
        val backend = FakeTitleBarBackend()
        val titleBar = titleBar(backend)

        titleBar.update(40f)
        backend.failRemove = true
        titleBar.dispose()
        backend.failRemove = false
        titleBar.dispose()

        assertEquals(2, backend.removeCount)
    }

    @Test
    fun invalidHeaderHeightIsIgnored() {
        val backend = FakeTitleBarBackend()
        val titleBar = titleBar(backend)

        titleBar.update(0f)
        titleBar.update(Float.NaN)

        assertEquals(0, backend.installCount)
    }

    @Test
    fun objectiveCStructReturnFollowsArchitectureAbi() {
        assertTrue(requiresObjectiveCStructReturn("x86_64"))
        assertTrue(requiresObjectiveCStructReturn("AMD64"))
        assertFalse(requiresObjectiveCStructReturn("aarch64"))
        assertFalse(requiresObjectiveCStructReturn("arm64"))
    }

    private fun titleBar(backend: FakeTitleBarBackend) = MacOSWindowTitleBar(
        windowHandle = { 42L },
        backend = backend
    )
}

private class FakeTitleBarBackend : MacOSWindowTitleBarBackend {
    var installCount = 0
    var updateCount = 0
    var removeCount = 0
    var lastHeaderHeight = 0f
    var failRemove = false

    override fun install(
        nativeWindow: Pointer,
        customHeaderHeight: Float
    ): MacOSWindowTitleBarInstallation {
        installCount++
        lastHeaderHeight = customHeaderHeight
        return FakeTitleBarInstallation
    }

    override fun update(
        installation: MacOSWindowTitleBarInstallation,
        customHeaderHeight: Float
    ) {
        updateCount++
        lastHeaderHeight = customHeaderHeight
    }

    override fun performWindowDrag(nativeWindow: Pointer): Boolean = true

    override fun remove(installation: MacOSWindowTitleBarInstallation) {
        removeCount++
        if (failRemove) error("remove failed")
    }
}

private data object FakeTitleBarInstallation : MacOSWindowTitleBarInstallation
