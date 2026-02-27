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

package com.moriafly.salt.ui.platform.linux

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.moriafly.salt.ui.UnstableSaltUiApi
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter

/**
 * Shared caption bar drag handler for Linux window and dialog window frames.
 *
 * Tries native X11 `_NET_WM_MOVERESIZE` first. If it fails,
 * automatically switches to a fallback AWT-based implementation for all
 * subsequent drags.
 *
 * @param window The AWT window to attach mouse listeners to.
 * @param captionBarHeight The height of the caption bar.
 * @param isMoveable Whether the window is moveable.
 * @param isHitTestInCaptionBar Whether the pointer is in the caption bar area.
 * @param canDrag Whether dragging is currently allowed (e.g., not in fullscreen).
 * @param windowState The Compose [WindowState] for placement control.
 */
@UnstableSaltUiApi
@Composable
internal fun LinuxCaptionBarDragHandler(
    window: Window,
    captionBarHeight: Dp,
    isMoveable: Boolean,
    isHitTestInCaptionBar: Boolean,
    canDrag: Boolean = true,
    windowState: WindowState? = null,
) {
    val currentCaptionBarHeight by rememberUpdatedState(captionBarHeight)
    val currentIsMoveable by rememberUpdatedState(isMoveable)
    val currentIsHitTestInCaptionBar by rememberUpdatedState(isHitTestInCaptionBar)
    val currentCanDrag by rememberUpdatedState(canDrag)
    val currentWindowState by rememberUpdatedState(windowState)

    DisposableEffect(window) {
        val fallbackMover = LinuxFallbackWindowMover(window)

        // 首次拖拽尝试原生，失败后切换为 false
        var useNativeDrag = true

        // 非最大化时的窗口宽度，用于从最大化拖出时恢复
        var restoreWidth = window.width

        val componentListener = object : java.awt.event.ComponentAdapter() {
            override fun componentResized(e: java.awt.event.ComponentEvent) {
                if ((window as? java.awt.Frame)?.extendedState != java.awt.Frame.MAXIMIZED_BOTH) {
                    restoreWidth = window.width
                }
            }
        }
        window.addComponentListener(componentListener)

        var pendingDrag = false
        var pendingDragEvent: MouseEvent? = null

        val mouseMotionListener = object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (pendingDrag) {
                    pendingDrag = false
                    val startEvent = pendingDragEvent ?: e
                    pendingDragEvent = null

                    // 如果窗口处于最大化状态，先恢复窗口
                    val state = currentWindowState
                    if (state != null && state.placement == WindowPlacement.Maximized) {
                        val oldWidth = window.width
                        val ratio = startEvent.x.toFloat() / oldWidth
                        state.placement = WindowPlacement.Floating
                        window.setLocation(
                            (startEvent.xOnScreen - restoreWidth * ratio).toInt(),
                            startEvent.yOnScreen - startEvent.y
                        )
                    }

                    if (useNativeDrag) {
                        if (LinuxX11WindowMover.tryMove(window)) {
                            e.consume()
                            return
                        }
                        // 原生方案失败
                        useNativeDrag = false
                    }

                    fallbackMover.startDrag(startEvent)
                }

                if (!fallbackMover.dragging) return
                fallbackMover.updateDrag(e)
                e.consume()
            }
        }

        val mouseListener = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (e.button != MouseEvent.BUTTON1) return
                if (!currentIsHitTestInCaptionBar) return
                if (!currentIsMoveable) return
                if (!currentCanDrag) return
                if (!isCaptionBarHit(e, currentCaptionBarHeight, window)) return

                pendingDrag = true
                pendingDragEvent = e
                e.consume()
            }

            override fun mouseReleased(e: MouseEvent) {
                if (e.button != MouseEvent.BUTTON1) return
                // 单击等情况取消 pending 状态，避免误触发拖动
                pendingDrag = false
                pendingDragEvent = null
                if (fallbackMover.dragging) e.consume()
                fallbackMover.endDrag()
            }

            override fun mouseClicked(e: MouseEvent) {
                if (e.button != MouseEvent.BUTTON1) return
                if (e.clickCount != 2) return
                if (!currentIsHitTestInCaptionBar) return
                if (!isCaptionBarHit(e, currentCaptionBarHeight, window)) return

                val state = currentWindowState ?: return
                if (state.placement == WindowPlacement.Maximized) {
                    state.placement = WindowPlacement.Floating
                } else {
                    state.placement = WindowPlacement.Maximized
                }
                e.consume()
            }
        }

        window.addMouseListener(mouseListener)
        window.addMouseMotionListener(mouseMotionListener)

        onDispose {
            window.removeComponentListener(componentListener)
            window.removeMouseListener(mouseListener)
            window.removeMouseMotionListener(mouseMotionListener)
        }
    }
}

/**
 * Check whether [event] falls within the caption bar region.
 */
private fun isCaptionBarHit(event: MouseEvent, captionBarHeight: Dp, window: Window): Boolean {
    val captionBarHeightPx = (
        captionBarHeight.value *
            window.graphicsConfiguration.defaultTransform.scaleY
    ).toInt()
    return event.y <= captionBarHeightPx
}
