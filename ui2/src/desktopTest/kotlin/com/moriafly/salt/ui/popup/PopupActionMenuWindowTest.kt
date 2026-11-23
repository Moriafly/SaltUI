/*
 * SaltUI
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

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ripple
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class PopupActionMenuWindowTest {
    @Test
    fun opensCascadesAndSubmitsActionsThroughTheWindow() = exerciseMenuWindow(
        verifyPointerAnchor = false
    )

    @Test
    fun followsTheMouseClickPosition() = exerciseMenuWindow(verifyPointerAnchor = true)

    private fun exerciseMenuWindow(verifyPointerAnchor: Boolean) = runDesktopComposeUiTest {
        lateinit var window: ComposeWindow
        val ordinaryActions = AtomicInteger()
        val childActions = AtomicInteger()
        val disabledActions = AtomicInteger()
        val popupOrigin = AtomicReference(Offset.Zero)
        val triggerFocus = FocusRequester()
        var wideRows by mutableStateOf(false)
        setContent {
            Window(
                onCloseRequest = {},
                title = "SaltUI popup menu test",
                undecorated = true,
                alwaysOnTop = true,
                state = rememberWindowState(
                    position = WindowPosition.Absolute(120.dp, 120.dp),
                    size = DpSize(660.dp, 490.dp)
                )
            ) {
                SideEffect { window = this.window }
                val configs = SaltConfigs.default(indication = ripple(color = Color.Magenta))
                SaltTheme(configs = configs) {
                    PopupMenuBackdrop(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(Modifier.offset(x = 96.dp, y = 84.dp)) {
                            var expanded by remember { mutableStateOf(false) }
                            var radioSelected by remember { mutableStateOf(false) }
                            var checked by remember { mutableStateOf(false) }
                            val anchor = rememberPopupMenuAnchor()
                            Text(
                                text = "Open actions",
                                modifier = Modifier
                                    .popupMenuAnchor(anchor)
                                    .focusRequester(triggerFocus)
                                    .onPreviewKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyDown &&
                                            event.key == Key.F2
                                        ) {
                                            expanded = true
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                    .clickable { anchor.open { expanded = true } }
                                    .padding(12.dp)
                            )
                            PopupActionMenu(
                                expanded = expanded,
                                anchor = anchor,
                                modifier = Modifier.onGloballyPositioned {
                                    popupOrigin.set(it.positionInWindow())
                                },
                                style = if (verifyPointerAnchor) {
                                    PopupMenuDefaults.style()
                                } else {
                                    PopupMenuDefaults.style(
                                        material = PopupMenuMaterial.Solid,
                                        containerColor = Color.White
                                    )
                                },
                                onDismissRequest = { expanded = false }
                            ) {
                                item(
                                    text = { MenuTestLabel("Main action", wide = wideRows) },
                                    onClick = { ordinaryActions.incrementAndGet() }
                                )
                                item(
                                    text = { MenuTestLabel("Disabled") },
                                    onClick = { disabledActions.incrementAndGet() },
                                    enabled = false
                                )
                                divider()
                                submenu(
                                    text = { MenuTestLabel("More actions") }
                                ) {
                                    item(
                                        text = { MenuTestLabel("Child action", wide = wideRows) },
                                        onClick = { childActions.incrementAndGet() }
                                    )
                                }
                                radioItem(
                                    text = { MenuTestLabel("Radio choice") },
                                    selected = radioSelected,
                                    onClick = { radioSelected = true },
                                    closeOnClick = false
                                )
                                toggleItem(
                                    text = { MenuTestLabel("Toggle") },
                                    checked = checked,
                                    onCheckedChange = { checked = it },
                                    closeOnClick = false
                                )
                            }
                        }
                    }
                }
            }
        }

        waitForIdle()
        runOnUiThread {
            window.toFront()
            window.requestFocus()
        }
        waitUntil(timeoutMillis = 5_000) { window.isFocused && window.isShowing }

        val robot = Robot().apply { autoDelay = 50 }
        val transform = window.graphicsConfiguration.defaultTransform

        fun moveTo(position: Offset) {
            robot.mouseMove(
                window.x + (position.x / transform.scaleX).roundToInt(),
                window.y + (position.y / transform.scaleY).roundToInt()
            )
            robot.waitForIdle()
        }

        fun moveToText(text: String) {
            moveTo(onNodeWithText(text).fetchSemanticsNode().boundsInWindow.center)
        }

        fun rowBackgroundColor(label: String): java.awt.Color {
            val row = onNodeWithText(label).fetchSemanticsNode().boundsInWindow
            val text = onNodeWithText(label, useUnmergedTree = true)
                .fetchSemanticsNode().boundsInWindow
            // Both rows have an empty leading selection slot. Sample before the text glyphs.
            val x = (row.left + text.left) / 2f
            return robot.getPixelColor(
                window.x + (x / transform.scaleX).roundToInt(),
                window.y + (row.center.y / transform.scaleY).roundToInt()
            )
        }

        fun click() {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            robot.waitForIdle()
        }

        fun press(key: Int) {
            robot.keyPress(key)
            robot.keyRelease(key)
            robot.waitForIdle()
        }

        fun waitForMenuItem(text: String, visible: Boolean = true) {
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() == visible
            }
            waitForIdle()
        }
        val triggerBounds = onNodeWithText("Open actions").fetchSemanticsNode().boundsInWindow

        fun openMenu(position: Offset = triggerBounds.center): Offset {
            moveTo(position)
            click()
            waitForMenuItem("Main action")
            return popupOrigin.get()
        }

        if (verifyPointerAnchor) {
            val firstClick =
                triggerBounds.topLeft +
                    Offset(triggerBounds.width * 0.75f, triggerBounds.height * 0.75f)
            val secondClick =
                triggerBounds.topLeft +
                    Offset(triggerBounds.width * 0.25f, triggerBounds.height * 0.25f)
            val firstMenuPosition = openMenu(firstClick)
            moveTo(Offset(16f, 16f))
            click()
            waitForMenuItem("Main action", visible = false)

            val secondMenuPosition = openMenu(secondClick)
            val cursorMovement = secondClick - firstClick
            val menuMovement = secondMenuPosition - firstMenuPosition
            assertEquals(cursorMovement.x, menuMovement.x, absoluteTolerance = 4f)
            assertEquals(cursorMovement.y, menuMovement.y, absoluteTolerance = 4f)
            moveToText("Main action")
            click()
            waitUntil(timeoutMillis = 5_000) { ordinaryActions.get() == 1 }
            waitForMenuItem("Main action", visible = false)

            runOnUiThread { triggerFocus.requestFocus() }
            press(KeyEvent.VK_F2)
            waitForMenuItem("Main action")
            val programmaticMenuPosition = popupOrigin.get()
            assertEquals(triggerBounds.left, programmaticMenuPosition.x, absoluteTolerance = 4f)
            assertEquals(triggerBounds.bottom, programmaticMenuPosition.y, absoluteTolerance = 4f)
            press(KeyEvent.VK_ESCAPE)
            waitForMenuItem("Main action", visible = false)
            return@runDesktopComposeUiTest
        }

        openMenu()
        moveToText("Disabled")
        click()
        assertEquals(0, disabledActions.get())
        waitForMenuItem("Main action")

        val idleRowColor = rowBackgroundColor("Main action")
        val idleSubmenuColor = rowBackgroundColor("More actions")
        moveToText("Main action")
        waitForIdle()
        mainClock.advanceTimeBy(64)
        waitForIdle()
        val ordinaryHoverColor = rowBackgroundColor("Main action")
        assertNotEquals(
            idleRowColor,
            ordinaryHoverColor,
            "The configured ripple should draw hover."
        )

        mainClock.autoAdvance = false
        try {
            moveToText("More actions")
            mainClock.advanceTimeBy(96)
            assertTrue(
                onAllNodesWithText("Child action").fetchSemanticsNodes().isNotEmpty(),
                "The first submenu should open without a hover timer."
            )
        } finally {
            mainClock.autoAdvance = true
        }
        waitForMenuItem("Child action")
        mainClock.advanceTimeBy(64)
        waitForIdle()
        assertEquals(
            ordinaryHoverColor,
            rowBackgroundColor("More actions"),
            "An open submenu should use the same configured hover as an ordinary row."
        )
        val triggerRow = onNodeWithText("More actions").fetchSemanticsNode().boundsInWindow
        val childRow = onNodeWithText("Child action").fetchSemanticsNode().boundsInWindow
        val compactWidth = 160 * transform.scaleX
        assertTrue(triggerRow.width < compactWidth, "A short root menu should fit its items.")
        assertTrue(childRow.width < compactWidth, "A short submenu should fit its items.")
        assertEquals(triggerRow.top, childRow.top, absoluteTolerance = 2f)
        // The panels overlap at their padded edges while the action rows meet.
        assertEquals(triggerRow.right, childRow.left, absoluteTolerance = 2f)

        mainClock.autoAdvance = false
        try {
            moveToText("Radio choice")
            mainClock.advanceTimeByFrame()
            moveTo(childRow.center)
            mainClock.advanceTimeBy(128)
            assertTrue(
                onAllNodesWithText("Child action").fetchSemanticsNodes().isNotEmpty(),
                "Crossing a neighboring row briefly should keep the child menu open."
            )
        } finally {
            mainClock.autoAdvance = true
        }

        waitForIdle()
        assertEquals(
            ordinaryHoverColor,
            rowBackgroundColor("More actions"),
            "The parent should keep its configured hover while the pointer is in the child menu."
        )
        moveToText("Main action")
        waitForMenuItem("Child action", visible = false)
        waitUntil(timeoutMillis = 1_000) {
            rowBackgroundColor("More actions") == idleSubmenuColor
        }

        moveToText("More actions")
        waitForMenuItem("Child action")
        moveToText("Child action")
        click()
        waitUntil(timeoutMillis = 5_000) { childActions.get() == 1 }
        waitForMenuItem("Main action", visible = false)

        openMenu()
        press(KeyEvent.VK_DOWN)
        press(KeyEvent.VK_DOWN)
        press(KeyEvent.VK_RIGHT)
        waitForMenuItem("Child action")
        press(KeyEvent.VK_LEFT)
        waitForMenuItem("Child action", visible = false)
        press(KeyEvent.VK_RIGHT)
        waitForMenuItem("Child action")
        press(KeyEvent.VK_ENTER)
        waitUntil(timeoutMillis = 5_000) { childActions.get() == 2 }
        waitForMenuItem("Main action", visible = false)

        openMenu()
        moveToText("Radio choice")
        click()
        onNodeWithText("Radio choice").assertIsSelected()

        val toggle = onNodeWithText("Toggle")
        toggle.assertIsOff()
        moveToText("Toggle")
        click()
        toggle.assertIsOn()
        click()
        toggle.assertIsOff()

        moveTo(Offset(16f, 16f))
        click()
        waitForMenuItem("Main action", visible = false)
        assertEquals(0, ordinaryActions.get())
        assertEquals(0, disabledActions.get())
        assertEquals(2, childActions.get())

        runOnUiThread { wideRows = true }
        openMenu()
        val wideRoot = onNodeWithText("Main action").fetchSemanticsNode().boundsInWindow
        moveToText("More actions")
        waitForMenuItem("Child action")
        val wideChild = onNodeWithText("Child action").fetchSemanticsNode().boundsInWindow
        val windowWidth = window.width * transform.scaleX
        val wideMenuWidth = 500 * transform.scaleX
        for (row in listOf(wideRoot, wideChild)) {
            assertTrue(row.width > wideMenuWidth, "A wide menu should use the available space.")
            assertTrue(row.left >= 0 && row.right <= windowWidth, "A menu should fit the window.")
        }
        moveTo(wideChild.center)
        click()
        waitForMenuItem("Main action", visible = false)
        assertEquals(3, childActions.get())
    }
}

@Composable
private fun MenuTestLabel(text: String, wide: Boolean = false) {
    // Use the same label widths with every platform's default fonts.
    Text(text, modifier = Modifier.width(if (wide) 800.dp else 96.dp), maxLines = 1)
}
