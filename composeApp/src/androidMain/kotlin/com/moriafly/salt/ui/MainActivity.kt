package com.moriafly.salt.ui

import UiCore
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.moriafly.salt.ui.ext.edgeToEdge

class MainActivity : ComponentActivity() {

    @OptIn(UnstableSaltApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        edgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            UiCore()
        }
    }

}