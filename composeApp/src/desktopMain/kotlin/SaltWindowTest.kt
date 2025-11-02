import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.ItemButton
import com.moriafly.salt.ui.ItemCheck
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowBackgroundType
import com.moriafly.salt.ui.window.SaltWindowProperties
import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Window
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLayeredPane
import javax.swing.JWindow

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() {
    application {
        var isDarkTheme by remember { mutableStateOf(false) }
        var backgroundType by remember { mutableStateOf(SaltWindowBackgroundType.None) }
        SaltTheme(
            configs = SaltConfigs.default(
                isDarkTheme = isDarkTheme
            )
        ) {
            val state = rememberWindowState()
            SaltWindow(
                onCloseRequest = ::exitApplication,
                state = state,
                // decoration = WindowDecoration.Undecorated(),
                properties = SaltWindowProperties.default(
                    minSize = DpSize(200.dp, 200.dp),
                    backgroundType = backgroundType
                ),
                init = { window ->
                    window.background = java.awt.Color.BLACK
                    window.findSkiaLayer()?.transparency = true
                    window.hackContentPane()
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                    // .background(SaltTheme.colors.background)
                ) {
                    CaptionBarHitTest()

                    val saltWindowInfo = LocalSaltWindowInfo.current
                    Column(
                        modifier = Modifier
                            .padding(top = saltWindowInfo.captionBarHeight)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        RoundedColumn {
                            ItemButton(
                                onClick = {
                                    isDarkTheme = !isDarkTheme
                                },
                                text = "Change IsDarkTheme"
                            )
                        }

                        RoundedColumn {
                            SaltWindowBackgroundType.entries.forEach { type ->
                                ItemCheck(
                                    state = backgroundType == type,
                                    onChange = {
                                        backgroundType = type
                                    },
                                    text = type.name
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Window.hackContentPane() {
    val oldContentPane = contentPane ?: return

    // Create hacked content pane the same way of AWT.
    val newContentPane: JComponent = HackedContentPane()
    newContentPane.name = "$name.contentPane"
    // newContentPane.layout = oldContentPane.layout
    newContentPane.layout = object : BorderLayout() {
        override fun addLayoutComponent(comp: Component, constraints: Any?) {
            super.addLayoutComponent(comp, constraints ?: CENTER)
        }
    }

    newContentPane.background = java.awt.Color(0, 0, 0, 0)
    newContentPane.isOpaque = false
    newContentPane.size = oldContentPane.size
    // newContentPane.removeInputMethodListener(newContentPane.inputMethodListeners.first())
    // newContentPane.addInputMethodListener(oldContentPane.inputMethodListeners.first())

    newContentPane.enableInputMethods(true)
    oldContentPane.components.forEach { component ->
        newContentPane.add(component)
    }

    contentPane = newContentPane
}

// Try hard to get the contentPane
internal var Window.contentPane
    get() = when (this) {
        is JFrame -> contentPane
        is JDialog -> contentPane
        is JWindow -> contentPane
        else -> null
    }
    set(value) = when (this) {
        is JFrame -> contentPane = value
        is JDialog -> contentPane = value
        is JWindow -> contentPane = value
        else -> throw IllegalStateException()
    }

class HackedContentPane : JLayeredPane() {
    override fun paint(g: Graphics) {
        if (background.alpha != 255) {
            val graphics = g.create()
            try {
                if (graphics is Graphics2D) {
                    graphics.color = background
                    graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC)
                    graphics.fillRect(0, 0, width, height)
                }
            } finally {
                graphics.dispose()
            }
        }
        super.paint(g)
    }
}
