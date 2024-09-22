package com.moriafly.salt.ui

import MainActivityContent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.moriafly.salt.ui.ext.edgeToEdge
import com.moriafly.salt.ui.util.RomUtil

class MainActivity : ComponentActivity() {

    @OptIn(UnstableSaltApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        edgeToEdge()
        super.onCreate(savedInstanceState)

        Log.d(
            "MainActivity",
            """
                isXiaomiHyperOS: ${RomUtil.isXiaomiHyperOS}
                isMeizuFlymeOS: ${RomUtil.isMeizuFlymeOS}
            """.trimIndent()
        )

        setContent {
            MainActivityContent()
        }
    }

}