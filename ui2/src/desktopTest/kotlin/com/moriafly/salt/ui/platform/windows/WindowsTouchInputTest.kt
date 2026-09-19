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

package com.moriafly.salt.ui.platform.windows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.structure.POINTER_INFO
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.SaltWindow
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.win32.StdCallLibrary
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, InternalComposeUiApi::class, UnstableSaltUiApi::class)
class WindowsTouchInputTest {
    @Test
    fun sceneAdapterSupportsComposeDialogAndDetectsDisposal() {
        assumeTrue(OS.isWindows())
        SwingUtilities.invokeAndWait {
            val dialog = ComposeDialog()
            val target = assertNotNull(WindowsTouchScene.create(dialog))
            try {
                assertTrue(!target.isDisposed)
            } finally {
                dialog.dispose()
            }
            assertTrue(target.isDisposed)
        }
    }

    @Test
    fun nativeTwoFingerInputReachesComposeWithoutMousePromotion() {
        assumeTrue(OS.isWindows())
        val native = Native.load("user32", TouchInjectionUser32::class.java)
        runDesktopComposeUiTest {
            lateinit var window: ComposeWindow
            val events = CopyOnWriteArrayList<TouchEvent>()
            val messages = CopyOnWriteArrayList<Int>()
            lateinit var probe: BasicWindowProc
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(size = DpSize(640.dp, 480.dp)),
                        title = "SaltUI native touch test",
                        init = { window = it }
                    ) {
                        Box(
                            Modifier.fillMaxSize().testTag("touch").pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        events += TouchEvent(
                                            event.type,
                                            event.changes.map {
                                                TouchContact(it.id.value, it.position, it.pressed, it.type)
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
            onNodeWithTag("touch").assertIsDisplayed()
            lateinit var origin: POINT
            lateinit var target: WindowsTouchScene
            runOnUiThread {
                window.isAlwaysOnTop = true
                window.toFront()
                window.requestFocus()
                val canvas = window.findSkiaLayer()!!.canvas
                target = assertNotNull(WindowsTouchScene.create(window))
                probe = object : BasicWindowProc(HWND(Native.getComponentPointer(canvas))) {
                    override fun callback(hwnd: HWND, uMsg: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
                        if (uMsg in 0x0240..0x0250) messages += uMsg
                        return super.callback(hwnd, uMsg, wParam, lParam)
                    }
                }
                origin = POINT(0, 0)
                assertTrue(native.ClientToScreen(HWND(Native.getComponentPointer(canvas)), origin))
            }
            waitForIdle()
            events.clear()

            val buffer = InjectedTouchInfo().toArray(2)
            fun inject(vararg contacts: Triple<Int, Int, Int>) {
                contacts.forEachIndexed { index, (id, flags, deltaX) ->
                    val contact = buffer[index] as InjectedTouchInfo
                    contact.pointerInfo.pointerType = 2
                    contact.pointerInfo.pointerId = id
                    contact.pointerInfo.pointerFlags = flags
                    contact.pointerInfo.ptPixelLocation = POINT(origin.x + 160 * id + deltaX, origin.y + 200)
                    contact.write()
                }
                // Windows requires at least 0.1 ms between frames without explicit timestamps.
                Thread.sleep(16)
                assertTrue(
                    native.InjectTouchInput(contacts.size, buffer[0].pointer),
                    "InjectTouchInput failed: " + Native.getLastError()
                )
            }

            val executor = Executors.newSingleThreadExecutor()
            try {
                val injection = executor.submit {
                    // Injected IDs must be below maxCount; IDs 1 and 2 need at least three slots.
                    check(native.InitializeTouchInjection(3, 3))
                    try {
                        inject(Triple(1, DOWN, 0))
                        repeat(4) { inject(Triple(1, UPDATE, 0)) }
                        inject(Triple(1, UPDATE, 0), Triple(2, DOWN, 0))
                        repeat(8) { inject(Triple(1, UPDATE, 0), Triple(2, UPDATE, 0)) }
                        inject(Triple(1, UP, 0), Triple(2, UPDATE, 0))
                        repeat(8) { inject(Triple(2, UPDATE, it + 1)) }
                        inject(Triple(2, UP or CANCELED, 8))
                        inject(Triple(1, DOWN, 0))
                        repeat(4) { inject(Triple(1, UPDATE, 0)) }
                        inject(Triple(1, UP, 0))
                    } finally {
                        runCatching {
                            inject(Triple(1, UP or CANCELED, 0), Triple(2, UP or CANCELED, 8))
                        }
                    }
                }
                waitUntil(timeoutMillis = 10_000) { injection.isDone }
                injection.get()
                waitUntil(timeoutMillis = 5_000) {
                    events.count { it.type == PointerEventType.Press } >= 3 &&
                        events.lastOrNull()?.type == PointerEventType.Release
                }
                val two = events.first { it.contacts.count { c -> c.pressed } == 2 }
                assertTrue(two.contacts.all { it.type == PointerType.Touch })
                assertEquals(2, two.contacts.map { it.id }.distinct().size)
                assertEquals(160f, two.contacts[1].position.x - two.contacts[0].position.x, 1f)
                assertTrue(events.any {
                    it.type == PointerEventType.Release && it.contacts.count { c -> c.pressed } == 1
                })
                assertTrue(events.any {
                    it.type == PointerEventType.Move && it.contacts.size == 1 && it.contacts[0].pressed
                })
                assertEquals(1, events.last { it.type == PointerEventType.Press }.contacts.size)
                assertTrue(events.filter { it.type == PointerEventType.Press }
                    .flatMap { it.contacts }.all { it.type == PointerType.Touch })
                assertTrue(0x0246 in messages && 0x0247 in messages)
            } finally {
                executor.shutdownNow()
                java.lang.ref.Reference.reachabilityFence(probe)
            }
            runOnUiThread {
                window.dispose()
                assertTrue(target.isDisposed)
            }
        }
    }

    private data class TouchEvent(val type: PointerEventType, val contacts: List<TouchContact>)
    private data class TouchContact(
        val id: Long,
        val position: Offset,
        val pressed: Boolean,
        val type: PointerType
    )

    companion object {
        private const val DOWN = 0x00010000 or 0x00000002 or 0x00000004
        private const val UPDATE = 0x00020000 or 0x00000002 or 0x00000004
        private const val UP = 0x00040000
        private const val CANCELED = 0x00008000
    }
}

@Suppress("FunctionName")
internal interface TouchInjectionUser32 : StdCallLibrary {
    fun InitializeTouchInjection(maxCount: Int, feedbackMode: Int): Boolean
    fun InjectTouchInput(count: Int, contacts: Pointer): Boolean
    fun ClientToScreen(hwnd: HWND, point: POINT): Boolean
}

@Structure.FieldOrder(
    "pointerInfo", "touchFlags", "touchMask", "rcContact", "rcContactRaw", "orientation", "pressure"
)
internal class InjectedTouchInfo : Structure() {
    @JvmField var pointerInfo: POINTER_INFO = POINTER_INFO()
    @JvmField var touchFlags: Int = 0
    @JvmField var touchMask: Int = 0
    @JvmField var rcContact: RECT = RECT()
    @JvmField var rcContactRaw: RECT = RECT()
    @JvmField var orientation: Int = 0
    @JvmField var pressure: Int = 0
}