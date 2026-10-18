/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
 * Licensed under the Apache License, Version 2.0
 */

@file:OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    com.moriafly.salt.ui.UnstableSaltUiApi::class
)

package com.moriafly.salt.ui.platform.linux

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.platform.linux.input.LinuxWindowGestures
import com.moriafly.salt.ui.platform.linux.input.LocalLinuxWindowGestures
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties
import com.moriafly.salt.ui.window.WindowResizeEdge
import java.awt.EventQueue
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.fail
import org.junit.Assume.assumeTrue

internal class LinuxInputWindow : AutoCloseable {
    private var exit: (() -> Unit)? = null
    private val failure = AtomicReference<Throwable>()
    private var applicationThread: Thread? = null
    lateinit var window: ComposeWindow
    lateinit var gestures: LinuxWindowGestures
    var clicks = 0
    var edge = WindowResizeEdge.None
    var caption = Rect.Zero
    var content = Rect.Zero

    fun open() {
        assumeTrue(System.getProperty("os.name").startsWith("Linux"))
        assumeTrue("A desktop display is required", !GraphicsEnvironment.isHeadless())
        assumeTrue(Toolkit.getDefaultToolkit().javaClass.name == "sun.awt.X11.XToolkit")
        applicationThread = thread(name = "SaltUI gesture test application", isDaemon = true) {
            try {
                application(exitProcessOnExit = false) {
                    exit = ::exitApplication
                    Content()
                }
            } catch (error: Throwable) {
                failure.set(error)
            }
        }
        await("window content") { ::window.isInitialized && window.isShowing && content != Rect.Zero }
        onEdt { window.toFront() }
    }

    fun await(
        description: String,
        timeoutMillis: Long = 5_000,
        condition: () -> Boolean
    ) {
        val deadline = System.nanoTime() + timeoutMillis * 1_000_000
        while (!onEdt(condition)) {
            failure.get()?.let { throw AssertionError("Application failed", it) }
            if (System.nanoTime() >= deadline) fail("Timed out waiting for $description")
            Thread.sleep(10)
        }
    }

    fun <T> onEdt(action: () -> T): T {
        if (EventQueue.isDispatchThread()) return action()
        val task = FutureTask(action)
        EventQueue.invokeAndWait(task)
        return task.get()
    }

    override fun close() {
        onEdt { exit?.invoke() }
        applicationThread?.join(5_000)
        check(applicationThread?.isAlive != true) { "Test application did not close" }
        failure.get()?.let { throw AssertionError("Application failed", it) }
    }

    @Composable
    private fun Content() {
        SaltTheme {
            val state = rememberWindowState(
                position = WindowPosition.Absolute(120.dp, 120.dp),
                size = DpSize(560.dp, 380.dp)
            )
            SaltWindow(
                onCloseRequest = {},
                state = state,
                title = "SaltUI window gesture regression",
                decoration = WindowDecoration.Undecorated(),
                properties = SaltWindowProperties.default(
                    captionButtonsVisible = false,
                    extraDisplayScale = 1.25f,
                    onResizeEdgeChange = { _, value -> edge = value }
                ),
                init = { window = it }
            ) {
                val currentGestures = checkNotNull(LocalLinuxWindowGestures.current)
                SideEffect { gestures = currentGestures }
                Column(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(LocalSaltWindowInfo.current.captionBarHeight)
                    ) {
                        CaptionBarHitTest(
                            Modifier.onGloballyPositioned { caption = it.boundsInWindow() }
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { content = it.boundsInWindow() }
                            .clickable { clicks++ }
                    ) {
                        Text("Input target")
                    }
                }
            }
        }
    }

    fun screen(position: Offset): Offset {
        val canvas = checkNotNull(window.findSkiaLayer()).canvas
        val origin = canvas.locationOnScreen
        val scale = window.graphicsConfiguration.defaultTransform
        return position + Offset(
            (origin.x * scale.scaleX).toFloat(),
            (origin.y * scale.scaleY).toFloat()
        )
    }
}
